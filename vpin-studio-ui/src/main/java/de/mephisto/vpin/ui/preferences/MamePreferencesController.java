package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class MamePreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MamePreferencesController.class);

  @FXML
  private CheckBox skipPinballStartupTest;

  @FXML
  private CheckBox useSound;

  @FXML
  private CheckBox useSamples;

  @FXML
  private CheckBox compactDisplay;

  @FXML
  private CheckBox doubleDisplaySize;

  @FXML
  private CheckBox ignoreRomCrcError;

  @FXML
  private CheckBox cabinetMode;

  @FXML
  private CheckBox showDmd;

  @FXML
  private CheckBox useExternalDmd;

  @FXML
  private CheckBox colorizeDmd;

  @FXML
  private CheckBox soundMode;

  @FXML
  private Button setVersionBtn;

  @FXML
  private Button installBtn;

  @FXML
  private Button refreshBtn;

  @FXML
  private Label installedVersion;

  @FXML
  private Label latestVersion;

  private ComponentRepresentation component;

  @FXML
  private void onInstall() {
    Dialogs.openComponentUpdateDialog(ComponentType.vpinmame, "Installation of \"VPin MAME " + this.latestVersion.getText() + "\"");
  }

  @FXML
  private void onVersionRefresh() {
    refreshBtn.setDisable(true);

    new Thread(() -> {
      client.getComponentService().clearCache();

      Platform.runLater(() -> {
        EventManager.getInstance().notify3rdPartyVersionUpdate();
        this.refreshUpdate();
      });
      refreshBtn.setDisable(false);
    }).start();
  }

  @FXML
  private void onVersionSet() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Set Version", "Apply \"" + component.getLatestReleaseVersion() + "\" as the current version of VPin MAME?", null, "Apply");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        client.getComponentService().setVersion(ComponentType.vpinmame, component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate();
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to apply version: " + e.getMessage());
      }
      refreshUpdate();
    }
  }

  private void saveOptions() {
    MameOptions options = new MameOptions();
    options.setRom(MameOptions.DEFAULT_KEY);

    options.setIgnoreRomCrcError(ignoreRomCrcError.isSelected());
    options.setSkipPinballStartupTest(skipPinballStartupTest.isSelected());
    options.setUseSamples(useSamples.isSelected());
    options.setUseSound(useSound.isSelected());
    options.setCompactDisplay(compactDisplay.isSelected());
    options.setDoubleDisplaySize(doubleDisplaySize.isSelected());
    options.setUseSound(useSound.isSelected());
    options.setShowDmd(showDmd.isSelected());
    options.setUseExternalDmd(useExternalDmd.isSelected());
    options.setCabinetMode(cabinetMode.isSelected());
    options.setColorizeDmd(colorizeDmd.isSelected());
    options.setSoundMode(soundMode.isSelected());

    try {
      client.getMameService().saveOptions(options);
    } catch (Exception e) {
      LOG.error("Failed to save mame settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mame settings: " + e.getMessage());
    }

    PreferencesController.markDirty();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    MameOptions options = client.getMameService().getOptions(MameOptions.DEFAULT_KEY);


    skipPinballStartupTest.setSelected(options.isSkipPinballStartupTest());
    skipPinballStartupTest.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSound.setSelected(options.isUseSound());
    useSound.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useSamples.setSelected(options.isUseSamples());
    useSamples.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    compactDisplay.setSelected(options.isUseSamples());
    compactDisplay.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    doubleDisplaySize.setSelected(options.isUseSamples());
    doubleDisplaySize.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    ignoreRomCrcError.setSelected(options.isIgnoreRomCrcError());
    ignoreRomCrcError.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    cabinetMode.setSelected(options.isCabinetMode());
    cabinetMode.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    showDmd.setSelected(options.isShowDmd());
    showDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    useExternalDmd.setSelected(options.isUseExternalDmd());
    useExternalDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    colorizeDmd.setSelected(options.isColorizeDmd());
    colorizeDmd.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());
    soundMode.setSelected(options.isColorizeDmd());
    soundMode.selectedProperty().addListener((observable, oldValue, newValue) -> saveOptions());

    refreshUpdate();
  }

  private void refreshUpdate() {
    component = client.getComponentService().getComponent(ComponentType.vpinmame);
    if (component != null) {
      installedVersion.setText(component.getInstalledVersion() != null ? component.getInstalledVersion() : "?");
      latestVersion.setText(component.getLatestReleaseVersion() != null ? component.getLatestReleaseVersion() : "?");
    }
  }
}
