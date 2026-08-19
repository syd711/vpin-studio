package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
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
import de.mephisto.vpin.commons.utils.i18n.Messages;

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
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.the_server_was_unable_to_determine_the_2"));
    }

  }

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.b2sbackglass;
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
  }

  @Override
  protected void refreshTab(ComponentRepresentation component) {
    openFolderButton.setDisable(!component.isInstalled());
    registerBtn.setDisable(!component.isInstalled() || !client.getSystemService().isLocal());
  }
}
