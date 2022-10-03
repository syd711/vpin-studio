
package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class HighscoreCardsController implements Initializable {
  @FXML
  private ImageView cardPreview;

  @FXML
  private Button generateBtn;

  @FXML
  private Button generateAllBtn;

  @FXML
  private Button openBtn;

  @FXML
  private ComboBox tableCombo;

  // Add a public no-args constructor
  public HighscoreCardsController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle)  {
    try {
      InputStream input = new URL("http://localhost:8089/directb2s/7/cropped/ratio_4x3").openStream();
      cardPreview.setImage(new Image(input));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}