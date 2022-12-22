package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.utils.Updater;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ToolbarController implements Initializable {

  @FXML
  private Button updateBtn;

  // Add a public no-args constructor
  public ToolbarController() {
  }

  @FXML
  private void onUpdate() {
    String version = Studio.getVersion();
    String newVersion = Updater.checkForUpdate(version);
    if(!StringUtils.isEmpty(newVersion)) {
      Updater.startUpdater(client.getHost(), version);
    }
  }

  @FXML
  private void onDisconnect() {
    Studio.stage.close();
    Studio.loadLauncher(new Stage());
  }

  @FXML
  private void onClearCache() {
    client.clearCache();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    String s = Updater.checkForUpdate(Studio.getVersion());
    updateBtn.setVisible(!StringUtils.isEmpty(s));
  }
}