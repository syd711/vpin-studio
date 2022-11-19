package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class BuildInPlayersController implements Initializable, StudioFXController {

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  // Add a public no-args constructor
  public BuildInPlayersController() {
  }

  @FXML
  private void onReload() {

  }

  @FXML
  private void onAdd() {

  }

  @FXML
  private void onEdit() {

  }

  @FXML
  private void onDelete() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
  }
}