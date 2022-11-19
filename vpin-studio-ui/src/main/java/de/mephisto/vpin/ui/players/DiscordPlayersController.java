package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class DiscordPlayersController implements Initializable, StudioFXController {

  // Add a public no-args constructor
  public DiscordPlayersController() {
  }

  @FXML
  private void onReload() {

  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Members"));
  }
}