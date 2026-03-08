package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.vpx.TableScriptOption;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Controller for the "Script Options" tab on the TableData screen.
 * <p>
 * Lifecycle:
 * 1. {@link #initialize} wires up FXML fields and sets an empty state.
 * 2. The parent TableDataController calls {@link #setGame(int)} when the
 * selected table changes.
 * 3. Options are loaded asynchronously so the UI stays responsive.
 * 4. Each option is rendered as a labelled row containing either a
 * {@link Slider} (numeric) or a {@link ComboBox} (literal strings).
 * 5. "Save" calls the REST endpoint; "Reset to Defaults" restores all
 * controls to the script-declared default value.
 * <p>
 */
public class TableDataTabScriptOptionsController implements Initializable {

  private static final Logger LOG = LoggerFactory.getLogger(TableDataTabScriptOptionsController.class);

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

  @FXML
  private Button resetButton;

  @FXML
  private StackPane placeholderPane;

  @FXML
  private Label placeholderLabel;

  @FXML
  private ProgressIndicator loadingSpinner;

  @FXML
  private ScrollPane optionsScrollPane;

  @FXML
  private VBox optionsVBox;

  private Stage stage;

  /**
   * Game currently shown in this tab (−1 = none).
   */
  private int currentGameId = -1;

  /**
   * Live option list.  Each item keeps its currentValue in sync with the
   * matching UI control via listeners.
   */
  private List<TableScriptOption> currentOptions = Collections.emptyList();


  // ── Initializable ─────────────────────────────────────────────────────────

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    placeholderPane.managedProperty().bindBidirectional(placeholderPane.visibleProperty());
    showPlaceholder("Select a VPX table to view its script options.", false);
  }

  // ── Public API ────────────────────────────────────────────────────────────
  public void setStage(Stage stage) {
    this.stage = stage;
  }

  /**
   * Called by the parent controller when the user selects a different table.
   *
   * @param gameId VPin Studio internal game ID, or −1 to clear the tab.
   */
  public void setGame(int gameId) {
    if (gameId == currentGameId) {
      return;
    }
    currentGameId = gameId;

    if (gameId < 0) {
      Platform.runLater(() -> showPlaceholder("Select a VPX table to view its script options.", false));
      return;
    }

    loadOptionsAsync(gameId);
  }

  @FXML
  private void onResetClicked() {
    if (currentGameId < 0 || currentOptions.isEmpty()) return;

    Optional<ButtonType> confirm = WidgetFactory.showConfirmation(stage, "Reset Script Options",
        "Reset all options for this table to their script-declared defaults?"
    );

    if (confirm.isEmpty() || confirm.get() != ButtonType.OK) {
      return;
    }

    resetButton.setDisable(true);
    optionsVBox.getChildren().clear();

    showPlaceholder("Loading script options…", true);

    JFXFuture.supplyAsync(() -> {
      boolean ok = client.getScriptOptionsService().resetOptions(currentGameId);
      if (ok) {
        // Reload so controls show the written default values
        loadOptionsAsync(currentGameId);
      }
      return ok;
    }).thenAcceptLater(b -> {
      resetButton.setDisable(false);
      if (!b) {
        WidgetFactory.showAlert(stage, "Error", "Reset failed. Check the server log for details.");
      }
    });
  }


  public boolean save() {
    if (currentGameId < 0 || currentOptions.isEmpty()) {
      return true;
    }

    return client.getScriptOptionsService().saveOptions(currentGameId, currentOptions);
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  /**
   * Loads options on a background thread and updates the UI on the FX thread.
   */
  private void loadOptionsAsync(int gameId) {
    this.optionsVBox.isVisible();
    Platform.runLater(() -> {
      showPlaceholder("Loading script options…", true);
      JFXFuture.supplyAsync(() -> {
        List<TableScriptOption> options = Collections.emptyList();
        try {
          options = client.getScriptOptionsService().getOptions(gameId);
        }
        catch (Exception e) {
          LOG.error("loadOptionsAsync: failed for game {}: {}", gameId, e.getMessage(), e);
        }

        return options;
      }).thenAcceptLater(options -> {
        // Guard against a rapid game-switch while loading
        if (gameId != currentGameId) {
          return;
        }
        applyOptions(options);
      });
    });
  }

  /**
   * Pushes a freshly-loaded option list into the UI.
   */
  private void applyOptions(List<TableScriptOption> options) {
    currentOptions = options;
    optionsVBox.getChildren().clear();

    if (options.isEmpty()) {
      showPlaceholder("No Table1.Option declarations found in this table's script.\n"
          + "Options become available when the script uses VPX 10.8 Table1.Option() calls.", false);
      resetButton.setDisable(true);
      return;
    }

    hideOverlays();
    for (TableScriptOption option : options) {
      optionsVBox.getChildren().add(buildOptionRow(option));
    }

    int count = options.size();
    resetButton.setDisable(false);
    stage.toFront();
  }

  // ── Row builder ───────────────────────────────────────────────────────────

  /**
   * Builds a UI row for one option.
   * <p>
   * Layout per row:
   * ┌─────────────────────────────────────────────────────────┐
   * │ Option Name                              [current value] │
   * │ [═══════════════slider/combo════════════]   unit suffix  │
   * │ (min label)                         (max label)         │
   * └─────────────────────────────────────────────────────────┘
   */
  private Node buildOptionRow(TableScriptOption option) {
    VBox row = new VBox(3);

    // ── Name + value label ──────────────────────────────────────────────
    HBox nameRow = new HBox(8);
    nameRow.setAlignment(Pos.CENTER_LEFT);

    Label nameLabel = new Label(option.getName());
    nameLabel.getStyleClass().add("default-text");
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    Label valueLabel = new Label();
    valueLabel.getStyleClass().add("default-text");
    valueLabel.setMinWidth(60);
    valueLabel.setAlignment(Pos.CENTER_RIGHT);

    nameRow.getChildren().addAll(nameLabel, valueLabel);

    // ── Control (slider or combo) ───────────────────────────────────────
    HBox controlRow = new HBox(8);
    controlRow.setAlignment(Pos.CENTER_LEFT);

    if (option.hasLiteralOptions()) {
      buildComboRow(option, controlRow);
    }
    else {
      buildSliderRow(option, controlRow, valueLabel);
    }

    row.getChildren().addAll(nameRow, controlRow);
    return row;
  }

  /**
   * Populates {@code controlRow} with a {@link ComboBox} for options that
   * have literal string labels.
   */
  private void buildComboRow(TableScriptOption option, HBox controlRow) {
    ComboBox<String> combo = new ComboBox<>();
    combo.setMaxWidth(Double.MAX_VALUE);
//    HBox.setHgrow(combo, Priority.ALWAYS);
    combo.getItems().addAll(option.getLiteralOptions());

    // Select the item matching currentValue
    int selectedIndex = (int) Math.round((option.getCurrentValue() - option.getMinValue()) / option.getStep());
    selectedIndex = Math.max(0, Math.min(selectedIndex, option.getLiteralOptions().size() - 1));
    combo.getSelectionModel().select(selectedIndex);

    combo.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
      if (newIdx == null) return;
      double value = option.getMinValue() + newIdx.doubleValue() * option.getStep();
      option.setCurrentValue(value);
    });

    controlRow.getChildren().add(combo);
  }

  /**
   * Populates {@code controlRow} with a {@link Slider} for numeric options.
   */
  private void buildSliderRow(TableScriptOption option, HBox controlRow, Label valueLabel) {
    Slider slider = new Slider(option.getMinValue(), option.getMaxValue(), option.getCurrentValue());
    slider.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(slider, Priority.ALWAYS);
    slider.setShowTickMarks(true);
    slider.setShowTickLabels(false);
    slider.setSnapToTicks(option.getStep() > 0);
    if (option.getStep() > 0) {
      slider.setMajorTickUnit(option.getStep() * Math.max(1, option.getStepCount() / 5));
      slider.setMinorTickCount(0);
      slider.setBlockIncrement(option.getStep());
    }

    String suffix = option.getUnitSuffix();
    updateValueLabel(valueLabel, option.getCurrentValue(), option.getUnit(), suffix);

    slider.valueProperty().addListener((obs, oldVal, newVal) -> {
      // Snap to nearest valid step
      double snapped = snapToStep(newVal.doubleValue(), option.getMinValue(), option.getStep());
      option.setCurrentValue(snapped);
      updateValueLabel(valueLabel, snapped, option.getUnit(), suffix);
    });

    // Min / max endpoint labels
    Label minLabel = new Label(formatEndpoint(option.getMinValue(), option.getUnit()));
    minLabel.getStyleClass().add("default-text");

    Label maxLabel = new Label(formatEndpoint(option.getMaxValue(), option.getUnit()));
    maxLabel.getStyleClass().add("default-text");

    controlRow.getChildren().addAll(minLabel, slider, maxLabel);
  }

  // ── Utility helpers ───────────────────────────────────────────────────────

  private void updateValueLabel(Label label, double value, int unit, String suffix) {
    if (unit == 1) {
      // Percent: multiply by 100 for display
      label.setText(DECIMAL_FORMAT.format(value * 100) + "%");
    }
    else {
      label.setText(DECIMAL_FORMAT.format(value) + suffix);
    }
  }

  private String formatEndpoint(double value, int unit) {
    if (unit == 1) return DECIMAL_FORMAT.format(value * 100) + "%";
    return DECIMAL_FORMAT.format(value);
  }

  /**
   * Snaps {@code value} to the nearest multiple of {@code step} from {@code min}.
   */
  private double snapToStep(double value, double min, double step) {
    if (step <= 0) return value;
    double steps = Math.round((value - min) / step);
    return min + steps * step;
  }

  // ── Overlay helpers ───────────────────────────────────────────────────────

  private void showPlaceholder(String message, boolean showSpinner) {
    placeholderLabel.setText(message);
    loadingSpinner.setVisible(showSpinner);
    placeholderPane.setVisible(true);
    optionsScrollPane.setVisible(false);
    resetButton.setDisable(true);
  }

  private void hideOverlays() {
    placeholderPane.setVisible(false);
    optionsScrollPane.setVisible(true);
  }
}
