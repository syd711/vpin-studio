package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
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

public class TabSerumController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabSerumController.class);

  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getEmulatorService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    openFolder(folder);
  }

  @FXML
  private void onReload() {
    client.getDmdService().clearCache();
    refreshCustomValues();
  }

  private void refreshCustomValues() {
    clearCustomValues();
  }

  @FXML
  private void onDmdDevice() {
    if (client.getSystemService().isLocal()) {
      GameEmulatorRepresentation defaultGameEmulator = client.getEmulatorService().getDefaultGameEmulator();
      File folder = new File(defaultGameEmulator.getMameDirectory());
      File exe = new File(folder, "DmdDevice.ini");
      super.editFile(exe);
    }
    else {
      try {
        boolean b = Dialogs.openTextEditor(new TextFile(VPinFile.DmdDeviceIni), "DmdDevice.ini");
        if (b) {
          client.getMameService().clearCache();
          EventManager.getInstance().notifyTablesChanged();
        }
      } catch (Exception e) {
        LOG.error("Failed to open DmdDeviceIni text file: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open DmdDeviceIni file: " + e.getMessage());
      }
    }
  }


  @Override
  protected ComponentType getComponentType() {
    return ComponentType.serum;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    onReload();
  }
}
