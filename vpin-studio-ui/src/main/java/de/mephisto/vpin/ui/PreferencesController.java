package de.mephisto.vpin.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable, StudioFXController {

  // Add a public no-args constructor
  public PreferencesController() {
  }


  @FXML
  private void onClose(ActionEvent event) throws IOException {
    NavigationController.loadScreen(event, "scene-tables.fxml");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }



  @Override
  public void dispose() {

  }
}