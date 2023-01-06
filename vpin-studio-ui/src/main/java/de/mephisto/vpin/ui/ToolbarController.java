package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.Optional;
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
    String newVersion = Updater.checkForUpdate(Studio.getVersion());
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Update " + newVersion, "A new update has been found. Download and install update for server and client?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Dialogs.openUpdateDialog();
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