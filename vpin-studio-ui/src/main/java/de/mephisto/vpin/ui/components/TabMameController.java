package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabMameController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabMameController.class);

  @FXML
  private Button mameBtn;

  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    openFolder(folder);
  }

  @FXML
  private void onMameSetup() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
        File file = new File(defaultGameEmulator.getMameDirectory(), "Setup64.exe");
        String systemPreset = client.getSystemPreset();
        if (systemPreset.equals(PreferenceNames.SYSTEM_PRESET_32_BIT)) {
          file = new File(defaultGameEmulator.getMameDirectory(), "Setup.exe");
        }

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

  @Override
  public void postProcessing(boolean simulate) {
    if (!simulate) {
      GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
      File file = new File(defaultGameEmulator.getMameDirectory(), "Setup64.exe");
      String systemPreset = client.getSystemPreset();
      if (systemPreset.equals(PreferenceNames.SYSTEM_PRESET_32_BIT)) {
        file = new File(defaultGameEmulator.getMameDirectory(), "Setup.exe");
      }
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList(file.getName()));
      executor.setDir(file.getParentFile());
      executor.executeCommandAsync();
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.vpinmame;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    mameBtn.setDisable(!client.getSystemService().isLocal());
  }
}