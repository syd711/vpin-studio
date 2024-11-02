package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabVpxController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabVpxController.class);


  @FXML
  private void onOpen() {
    try {
      Dialogs.openTextEditor(new TextFile(VPinFile.VPinballXIni), "VPinballX.ini");
    }
    catch (Exception e) {
      LOG.error("Failed to open file: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open file: " + e.getMessage());
    }
  }

  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getFrontendService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getInstallationDirectory());
    openFolder(folder);
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.vpinball;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();

    componentUpdateController.setLocalInstallOnly(false);
  }
}
