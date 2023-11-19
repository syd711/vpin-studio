package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.dmd.FreezySummary;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabFreezyDMDController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabFreezyDMDController.class);


  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    openFolder(folder);
  }

  @FXML
  private void onReload() {
    client.getDmdService().clearCache();
    refreshCustomValues();
  }

  @FXML
  private void onDmdDevice() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getMameDirectory());
    File exe = new File(folder, "DmdDevice.ini");
    super.editFile(exe);
  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.freezy;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    refreshCustomValues();
  }

  private void refreshCustomValues() {
    clearCustomValues();
    if (client.getSystemService().isLocal()) {
      super.addCustomValue("DMDDEVICE_CONFIG Value", System.getenv("DMDDEVICE_CONFIG"));
    }

    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    FreezySummary freezySummary = client.getDmdService().getFreezySummary(defaultGameEmulator.getId());

    super.addCustomValue("Plugins:", freezySummary.getPlugins().isEmpty() ? "-" : String.join(", ", freezySummary.getPlugins()));
    super.addCustomValue("Status:", freezySummary.getStatus() == null ? "OK" : freezySummary.getStatus());
  }
}
