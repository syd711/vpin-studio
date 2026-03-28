package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.restclient.vr.VRFilesInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class VRPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VRPreferencesController.class);

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  @FXML
  private Label dmdDeviceIniText;

  @FXML
  private Label dmdDeviceIniTextVr;

  @FXML
  private Label vPinballXIniText;

  @FXML
  private Label vPinballXIniTextVr;

  @FXML
  private void onButtonRecord() {
    PreferencesDialogs.openButtonRecorder();
  }

  @FXML
  private void onDMDDeviceIniUpload() {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select DMDDevice.ini File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(".ini", "DMDDevice.ini"));

    File file = fileChooser.showOpenDialog(Studio.stage);
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        ProgressDialog.createProgressDialog(new VRFileUploadProgressModel("Uploading " + file.getName(), file, emulatorCombo.getValue().getId()));
        refreshFiles(emulatorCombo.getValue());
      });
    }
  }

  @FXML
  private void onVPinballXIniUpload() {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select VPinballX.ini File");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(".ini", "VPinballX.ini"));

    File file = fileChooser.showOpenDialog(Studio.stage);
    if (file != null && file.exists()) {
      Platform.runLater(() -> {
        ProgressDialog.createProgressDialog(new VRFileUploadProgressModel("Uploading " + file.getName(), file, emulatorCombo.getValue().getId()));
        refreshFiles(emulatorCombo.getValue());
      });
    }
  }

  @FXML
  private void onEditDmdDeviceIni() {

  }

  @FXML
  private void onEditVPinballXIni() {

  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    VRSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.VR_SETTINGS, VRSettings.class);

    enabledCheckbox.setSelected(settings.isEnabled());
    enabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        settings.setEnabled(newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
    });

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getEmulatorService().getGameEmulatorsUncached());
    List<GameEmulatorRepresentation> filtered = emulators.stream()
        .filter(e -> e.isEnabled())
        .filter(e -> e.isVpxEmulator())
        .filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId())))
        .sorted(Comparator.comparing(GameEmulatorRepresentation::getName))
        .collect(Collectors.toList());

    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.emulatorCombo.setDisable(filtered.isEmpty());
    this.emulatorCombo.valueProperty().addListener(new ChangeListener<GameEmulatorRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameEmulatorRepresentation> observable, GameEmulatorRepresentation oldValue, GameEmulatorRepresentation newValue) {
        refreshFiles(newValue);
      }
    });

    if (!filtered.isEmpty()) {
      this.emulatorCombo.getSelectionModel().selectFirst();
    }
  }

  private void refreshFiles(GameEmulatorRepresentation newValue) {
    VRFilesInfo vrFiles = client.getVRService().getVRFiles(newValue.getId());

    dmdDeviceIniText.setText("-");
    dmdDeviceIniText.setTooltip(null);

    dmdDeviceIniTextVr.setText("-");
    dmdDeviceIniTextVr.setTooltip(null);

    vPinballXIniText.setText("-");
    vPinballXIniText.setTooltip(null);

    vPinballXIniTextVr.setText("-");
    vPinballXIniTextVr.setTooltip(null);

    if (vrFiles.getDmdDeviceIni() != null) {
      dmdDeviceIniText.setText(vrFiles.getDmdDeviceIni());
      dmdDeviceIniText.setTooltip(new Tooltip(vrFiles.getDmdDeviceIni()));
    }
    if (vrFiles.getDmdDeviceIniVr() != null) {
      dmdDeviceIniTextVr.setText(vrFiles.getDmdDeviceIniVr());
      dmdDeviceIniTextVr.setTooltip(new Tooltip(vrFiles.getDmdDeviceIniVr()));
    }

    if (vrFiles.getvPinballXIni() != null) {
      vPinballXIniText.setText(vrFiles.getvPinballXIni());
      vPinballXIniText.setTooltip(new Tooltip(vrFiles.getvPinballXIni()));
    }
    if (vrFiles.getvPinballXIniVr() != null) {
      vPinballXIniTextVr.setText(vrFiles.getvPinballXIniVr());
      vPinballXIniTextVr.setTooltip(new Tooltip(vrFiles.getvPinballXIniVr()));
    }
  }
}
