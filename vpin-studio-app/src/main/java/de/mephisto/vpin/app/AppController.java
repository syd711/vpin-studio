package de.mephisto.vpin.app;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class AppController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(AppController.class);

  @FXML
  private StackPane rootStack;

  // Add a public no-args constructor
  public AppController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}