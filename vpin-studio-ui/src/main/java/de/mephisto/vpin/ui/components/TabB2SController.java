package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.tables.GameEmulatorRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TabB2SController extends AbstractComponentTab implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TabB2SController.class);

  @FXML
  private Button registerBtn;

  @FXML
  private void onRegister() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getTablesDirectory());
    File exe = new File(folder, "B2SBackglassServerRegisterApp.exe");
    super.openFile(exe);
  }

  @FXML
  private void onFolder() {
    GameEmulatorRepresentation defaultGameEmulator = client.getPinUPPopperService().getDefaultGameEmulator();
    File folder = new File(defaultGameEmulator.getTablesDirectory());
    openFolder(folder);
  }


  @Override
  protected ComponentType getComponentType() {
    return ComponentType.b2sbackglass;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    registerBtn.setDisable(!client.getSystemService().isLocal());
  }
}
