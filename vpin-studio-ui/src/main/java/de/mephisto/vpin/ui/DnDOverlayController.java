package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DnDOverlayController implements Initializable {

  @FXML
  private Label messageLabel;

  @FXML
  private BorderPane root;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setViewParams(double width, double height) {
    root.setPrefWidth(width);
    root.setPrefHeight(height);

  }
}
