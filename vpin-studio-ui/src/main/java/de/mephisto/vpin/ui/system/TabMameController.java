package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabMameController extends SystemTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabMameController.class);

  @FXML
  private Button mameInstallBtn;

  @FXML
  private Button mameSetVersionBtn;

  @FXML
  private Button mameCheckBtn;

  @FXML
  private Button mameBtn;

  @FXML
  private Label mameInstalledVersionLabel;

  @FXML
  private Label mameLatestVersionLabel;

  @FXML
  private Label mameLastModifiedLabel;


  @FXML
  private void onCheck() {

  }


  @FXML
  private void onInstall() {
    Dialogs.openComponentUpdateDialog(ComponentType.vpinmame, "Installation of \"VPin MAME " + this.mameLatestVersionLabel.getText() + "\"");
  }


  @FXML
  private void onMameSetup() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
        File file = new File(defaultGameEmulator.getMameDirectory(), "Setup64.exe");
//        if (presetCombo.getValue().equals(PreferenceNames.SYSTEM_PRESET_32_BIT)) {
//          file = new File(defaultGameEmulator.getMameDirectory(), "Setup.exe");
//        }

        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find Setup.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open Mame Setup: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onVersionSet() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Set Version", "Apply \"" + mameLatestVersionLabel.getText() + "\" as the current version of VPin MAME?", null, "Apply");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      try {
        ComponentRepresentation component = client.getComponentService().getComponent(ComponentType.vpinmame);
        client.getComponentService().setVersion(component.getType(), component.getLatestReleaseVersion());
        EventManager.getInstance().notify3rdPartyVersionUpdate();
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to apply version: " + e.getMessage());
      }
      refreshUpdate(ComponentType.vpinmame, mameInstalledVersionLabel, mameLatestVersionLabel);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    mameBtn.setDisable(!client.getSystemService().isLocal());
  }
}
