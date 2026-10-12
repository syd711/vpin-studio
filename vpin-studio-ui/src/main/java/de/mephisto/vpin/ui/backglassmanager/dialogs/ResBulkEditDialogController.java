package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.directb2s.ScreenResTarget;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.mephisto.vpin.ui.Studio.client;

public class ResBulkEditDialogController implements Initializable, DialogController {
  private final static Pattern RESOLUTION_PATTERN = Pattern.compile("(\\d{3,5})\\s*[xX×]\\s*(\\d{3,5})");
  private final static String FALLBACK_RESOLUTION = "1920x1080";

  @FXML
  private ComboBox<String> resolutionCombo;

  @FXML
  private Label descriptionLabel;

  @FXML
  private RadioButton playfieldRadio;

  @FXML
  private RadioButton backglassRadio;

  @FXML
  private RadioButton dmdRadio;

  @FXML
  private ToggleGroup targetGroup;

  private Stage stage;
  private int emulatorId;
  private boolean applied = false;

  /**
   * The resolutions configured in the global screenres.txt, indexed by target.
   */
  private DirectB2sScreenRes globalScreenRes;

  @FXML
  private void onCancelClick(ActionEvent e) {
    stage.close();
  }

  @FXML
  private void onApplyClick(ActionEvent e) {
    String text = StringUtils.defaultString(resolutionCombo.getEditor().getText()).trim();
    Matcher matcher = RESOLUTION_PATTERN.matcher(text);
    if (!matcher.matches()) {
      WidgetFactory.showAlert(stage, Messages.get("backglass.res_bulk.invalid", text));
      return;
    }
    int width = Integer.parseInt(matcher.group(1));
    int height = Integer.parseInt(matcher.group(2));
    ScreenResTarget target = getTarget();

    int affected = client.getBackglassServiceClient().bulkResizeScreenRes(emulatorId, target, width, height, true);
    if (affected == 0) {
      WidgetFactory.showAlert(stage, Messages.get("backglass.res_bulk.none_affected"));
      return;
    }

    Optional<ButtonType> confirmed = WidgetFactory.showConfirmation(stage,
        Messages.get("backglass.res_bulk.confirm", affected, getTargetLabel(), width, height),
        Messages.get("backglass.res_bulk.confirm_help"), null,
        Messages.get("backglass.res_bulk.confirm_button"));
    if (confirmed.isPresent() && confirmed.get().equals(ButtonType.OK)) {
      int changed = client.getBackglassServiceClient().bulkResizeScreenRes(emulatorId, target, width, height, false);
      applied = true;
      WidgetFactory.showAlert(stage, Messages.get("backglass.res_bulk.done", changed));
      stage.close();
    }
  }

  public boolean isApplied() {
    return applied;
  }

  public void setData(Stage stage, int emulatorId) {
    this.stage = stage;
    this.emulatorId = emulatorId;
  }

  @Override
  public void onDialogCancel() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    resolutionCombo.setItems(FXCollections.observableArrayList(
        "1280x1024", "1280x720", "1366x768", "1600x900", "1920x1080", "2560x1440", "3840x2160"));
    resolutionCombo.setValue(FALLBACK_RESOLUTION);

    targetGroup.selectedToggleProperty().addListener((obs, oldValue, newValue) -> {
      if (newValue == null) {
        // a selection is mandatory
        oldValue.setSelected(true);
        return;
      }
      updateTarget();
    });

    // the default values are taken from the global screenres.txt
    JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getGlobalScreenRes())
        .thenAcceptLater(screenRes -> {
          globalScreenRes = screenRes;
          updateTarget();
        })
        .onErrorLater(ex -> updateTarget());
    updateTarget();
  }

  private ScreenResTarget getTarget() {
    if (playfieldRadio.isSelected()) {
      return ScreenResTarget.PLAYFIELD;
    }
    if (dmdRadio.isSelected()) {
      return ScreenResTarget.DMD;
    }
    return ScreenResTarget.BACKGLASS;
  }

  private String getTargetLabel() {
    return switch (getTarget()) {
      case PLAYFIELD -> Messages.get("backglass.res_bulk.target_playfield");
      case DMD -> Messages.get("backglass.res_bulk.target_dmd");
      default -> Messages.get("backglass.res_bulk.target_backglass");
    };
  }

  /**
   * Updates the description and the default resolution for the selected target.
   */
  private void updateTarget() {
    ScreenResTarget target = getTarget();
    descriptionLabel.setText(Messages.get("backglass.res_bulk.description_" + target.name().toLowerCase()));

    int width = 0;
    int height = 0;
    if (globalScreenRes != null) {
      switch (target) {
        case PLAYFIELD -> {
          width = globalScreenRes.getPlayfieldWidth();
          height = globalScreenRes.getPlayfieldHeight();
        }
        case DMD -> {
          width = globalScreenRes.getDmdWidth();
          height = globalScreenRes.getDmdHeight();
        }
        default -> {
          width = globalScreenRes.getFullBackglassWidth();
          height = globalScreenRes.getFullBackglassHeight();
        }
      }
    }
    String resolution = width > 0 && height > 0 ? width + "x" + height : FALLBACK_RESOLUTION;
    if (!resolutionCombo.getItems().contains(resolution)) {
      resolutionCombo.getItems().add(resolution);
    }
    resolutionCombo.setValue(resolution);
  }
}
