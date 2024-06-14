package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabFlexDMDController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabFlexDMDController.class);

  @FXML
  private Button flexDMDBtn;

  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getFrontendService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    openFolder(folder);
  }

  @FXML
  private void onFlexDMD() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        GameEmulatorRepresentation defaultGameEmulator = client.getFrontendService().getDefaultGameEmulator();
        File file = new File(defaultGameEmulator.getMameDirectory(), "FlexDMDUI.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find FlexDMD UI", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open FlexDMD UI: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void postProcessing(boolean simulate) {
    if (!simulate) {
      onFlexDMD();
    }
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.flexdmd;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    flexDMDBtn.setDisable(!client.getSystemService().isLocal());
  }
}
