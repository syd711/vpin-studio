package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.freezy;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();

    if (client.getSystemService().isLocal()) {
      super.addCustomValue("DMDDEVICE_CONFIG", System.getenv("DMDDEVICE_CONFIG"));
    }
  }
}