package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.vpx.TableScriptOption;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class TableDataTabScriptOptionsController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(TableDataTabScriptOptionsController.class);

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    // ── FXML fields ───────────────────────────────────────────────────────────

    @FXML private Label statusLabel;
    @FXML private Button resetButton;
    @FXML private StackPane placeholderPane;
    @FXML private Label placeholderLabel;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private ScrollPane optionsScrollPane;
    @FXML private VBox optionsVBox;

    // ── State ─────────────────────────────────────────────────────────────────

    /** Game currently shown in this tab (−1 = none). */
    private int currentGameId = -1;

    /**
     * Live option list.  Each item keeps its currentValue in sync with the
     * matching UI control via listeners.
     */
    private List<TableScriptOption> currentOptions = Collections.emptyList();

    /** Background thread pool for async loading. */
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "script-options-loader");
        t.setDaemon(true);
        return t;
    });

    // ── Initializable ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showPlaceholder("Select a VPX table to view its script options.", false);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Called by the parent controller when the user selects a different table.
     *
     * @param gameId VPin Studio internal game ID, or −1 to clear the tab.
     */
    public void setGame(int gameId) {
        if (gameId == currentGameId) return;
        currentGameId = gameId;

        if (gameId < 0) {
            Platform.runLater(() -> showPlaceholder("Select a VPX table to view its script options.", false));
            return;
        }

        loadOptionsAsync(gameId);
    }

    // ── Called by TableDataController.doSave() ────────────────────────────────

    /**
     * Persists current option values. Called by the parent controller when the
     * user clicks Save or Save & Close — same pattern as other tabs.
     */
    public boolean save() {
        if (currentGameId < 0 || currentOptions.isEmpty()) return true;
        return Studio.client.getScriptOptionsService().saveOptions(currentGameId, currentOptions);
    }

    @FXML
    private void onResetClicked() {
        if (currentGameId < 0 || currentOptions.isEmpty()) return;

        Optional<ButtonType> confirm = WidgetFactory.showConfirmation(
                Studio.stage,
                "Reset Script Options",
                "Reset all options for this table to their script-declared defaults?"
        );
        if (confirm.isEmpty() || confirm.get() != ButtonType.OK) return;

        resetButton.setDisable(true);
        executor.submit(() -> {
            boolean ok = Studio.client.getScriptOptionsService().resetOptions(currentGameId);
            if (ok) {
                // Reload so controls show the written default values
                loadOptionsAsync(currentGameId);
            }
            Platform.runLater(() -> {
                resetButton.setDisable(false);
                if (!ok) {
                    WidgetFactory.showAlert(Studio.stage, "Error", "Reset failed. Check the server log for details.");
                }
            });
        });
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Loads options on a background thread and updates the UI on the FX thread. */
    private void loadOptionsAsync(int gameId) {
        Platform.runLater(() -> showPlaceholder("Loading script options…", true));

        executor.submit(() -> {
            List<TableScriptOption> options = Collections.emptyList();
            try {
                options = Studio.client.getScriptOptionsService().getOptions(gameId);
            } catch (Exception e) {
                LOG.error("loadOptionsAsync: failed for game {}: {}", gameId, e.getMessage(), e);
            }

            final List<TableScriptOption> finalOptions = options;
            Platform.runLater(() -> {
                // Guard against a rapid game-switch while loading
                if (gameId != currentGameId) return;
                applyOptions(finalOptions);
            });
        });
    }

    /** Pushes a freshly-loaded option list into the UI. */
    private void applyOptions(List<TableScriptOption> options) {
        currentOptions = options;
        optionsVBox.getChildren().clear();

        if (options.isEmpty()) {
            showPlaceholder("No Option declarations found in this table's script.\n"
                    + "Options become available when the script uses VPX 10.8 Option() calls.", false);
            resetButton.setDisable(true);
            return;
        }

        hideOverlays();
        for (TableScriptOption option : options) {
            optionsVBox.getChildren().add(buildOptionRow(option));
        }

        int count = options.size();
        statusLabel.setText(count + " option" + (count == 1 ? "" : "s"));
        resetButton.setDisable(false);
    }

    // ── Row builder ───────────────────────────────────────────────────────────

    /**
     * Builds a UI row for one option.
     *
     * Layout per row:
     *   ┌─────────────────────────────────────────────────────────┐
     *   │ Option Name                              [current value] │
     *   │ [═══════════════slider/combo════════════]   unit suffix  │
     *   │ (min label)                         (max label)         │
     *   └─────────────────────────────────────────────────────────┘
     */
    private Node buildOptionRow(TableScriptOption option) {
        VBox row = new VBox(4);
        row.getStyleClass().add("script-option-row");

        // ── Name + value label ──────────────────────────────────────────────
        HBox nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(option.getName());
        nameLabel.getStyleClass().add("script-option-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label valueLabel = new Label();
        valueLabel.getStyleClass().add("script-option-value");
        valueLabel.setMinWidth(60);
        valueLabel.setAlignment(Pos.CENTER_RIGHT);

        nameRow.getChildren().addAll(nameLabel, valueLabel);

        // ── Control (slider or combo) ───────────────────────────────────────
        HBox controlRow = new HBox(8);
        controlRow.setAlignment(Pos.CENTER_LEFT);

        if (option.hasLiteralOptions()) {
            buildComboRow(option, controlRow, valueLabel);
        } else {
            buildSliderRow(option, controlRow, valueLabel);
        }

        row.getChildren().addAll(nameRow, controlRow);
        return row;
    }

    /**
     * Populates {@code controlRow} with a {@link ComboBox} for options that
     * have literal string labels.
     */
    private void buildComboRow(TableScriptOption option, HBox controlRow, Label valueLabel) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(combo, Priority.ALWAYS);
        combo.getItems().addAll(option.getLiteralOptions());

        // Select the item matching currentValue
        int selectedIndex = (int) Math.round((option.getCurrentValue() - option.getMinValue()) / option.getStep());
        selectedIndex = Math.max(0, Math.min(selectedIndex, option.getLiteralOptions().size() - 1));
        combo.getSelectionModel().select(selectedIndex);
        valueLabel.setText(option.getLiteralOptions().get(selectedIndex));

        combo.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (newIdx == null) return;
            double value = option.getMinValue() + newIdx.doubleValue() * option.getStep();
            option.setCurrentValue(value);
            String label = option.getLiteralOptions().get(newIdx.intValue());
            valueLabel.setText(label);
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
        minLabel.getStyleClass().add("script-option-endpoint");

        Label maxLabel = new Label(formatEndpoint(option.getMaxValue(), option.getUnit()));
        maxLabel.getStyleClass().add("script-option-endpoint");

        controlRow.getChildren().addAll(minLabel, slider, maxLabel);
    }

    // ── Utility helpers ───────────────────────────────────────────────────────

    private void updateValueLabel(Label label, double value, int unit, String suffix) {
        if (unit == 1) {
            // Percent: multiply by 100 for display
            label.setText(DECIMAL_FORMAT.format(value * 100) + "%");
        } else {
            label.setText(DECIMAL_FORMAT.format(value) + suffix);
        }
    }

    private String formatEndpoint(double value, int unit) {
        if (unit == 1) return DECIMAL_FORMAT.format(value * 100) + "%";
        return DECIMAL_FORMAT.format(value);
    }

    /** Snaps {@code value} to the nearest multiple of {@code step} from {@code min}. */
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
        statusLabel.setText("");
        resetButton.setDisable(true);
    }

    private void hideOverlays() {
        placeholderPane.setVisible(false);
        optionsScrollPane.setVisible(true);
    }
}