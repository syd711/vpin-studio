package de.mephisto.vpin.commons.fx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HighscoreCardController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreCardController.class);

  @FXML
  private ImageView imageView;

  // Add a public no-args constructor
  public HighscoreCardController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    imageView.setPreserveRatio(false);
  }

  public void setImage(Stage highscoreCardStage, File file) {
    try {

      Screen screen = Screen.getPrimary();
      Rectangle2D bounds = screen.getVisualBounds();

      FileInputStream fileInputStream = new FileInputStream(file);
      Image image = new Image(fileInputStream);
      fileInputStream.close();

      highscoreCardStage.setX(bounds.getMinX() / 2);
      highscoreCardStage.setY(bounds.getMinY() / 2);
      highscoreCardStage.setHeight(image.getWidth() + 12);
      highscoreCardStage.setWidth(image.getWidth() + 12);

      imageView.setImage(image);


    } catch (IOException e) {
      LOG.error("Failed to show card: " + e.getMessage(), e);
    }
  }
}