package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class DiscordFAQPreferencesController implements Initializable {


  @FXML
  private void onBotTutorial() {
    Dialogs.openBotTutorial();
  }

  @FXML
  private void onBotServerIdTutorial() {
    Dialogs.openBotServerIdTutorial();
  }

  @FXML
  private void onBotTokenTutorial() {
    Dialogs.openBotTokenTutorial();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
