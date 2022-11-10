package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PlayersController implements Initializable, StudioFXController {


  // Add a public no-args constructor
  public PlayersController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Offline Players"));
  }
}