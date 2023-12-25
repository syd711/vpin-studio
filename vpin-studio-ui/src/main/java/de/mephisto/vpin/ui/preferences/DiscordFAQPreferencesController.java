package de.mephisto.vpin.ui.preferences;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class DiscordFAQPreferencesController implements Initializable {


  @FXML
  private void onBotTutorial() {
    PreferencesDialogs.openBotTutorial();
  }

  @FXML
  private void onBotServerIdTutorial() {
    PreferencesDialogs.openBotServerIdTutorial();
  }

  @FXML
  private void onBotTokenTutorial() {
    PreferencesDialogs.openBotTokenTutorial();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
