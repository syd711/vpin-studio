package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
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
  private void onPrefsB2S() {
    PreferencesController.open("backglass");
  }


  @FXML
  private void onRegister() {
    File folder = client.getBackglassServiceClient().getBackglassServerFolder();
    if (folder != null) {
      File exe = new File(folder, "B2SBackglassServerRegisterApp.exe");
      super.openFile(exe);
    }
    else {
      WidgetFactory.showAlert(Studio.stage, "Error", "The server was unable to determine the backglass server installation directory.");
    }

  }

  @FXML
  private void onFolder() {
    File folder = client.getBackglassServiceClient().getBackglassServerFolder();
    if (folder != null) {
      openFolder(folder);
    }
    else {
      WidgetFactory.showAlert(Studio.stage, "Error", "The server was unable to determine the backglass server installation directory.");
    }
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
