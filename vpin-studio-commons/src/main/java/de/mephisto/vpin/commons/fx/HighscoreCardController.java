package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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

  @FXML
  private StackPane root;

  // Add a public no-args constructor
  public HighscoreCardController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    imageView.setPreserveRatio(false);
  }

  public void setImage(Stage highscoreCardStage, CardSettings cardSettings, PinUPPlayerDisplay display, File file) {
    try {
      Screen screen = Screen.getPrimary();
      Rectangle2D bounds = screen.getVisualBounds();

      Image image = null;

      LOG.info("Showing card \"" + file.getAbsolutePath() + "\", display \"" + display + "\" (using display: " + cardSettings.isNotificationOnPopperScreen() + ")");
      if (cardSettings.isNotificationOnPopperScreen() && display != null) {
        FileInputStream fileInputStream = new FileInputStream(file);
        image = new Image(fileInputStream, display.getWidth(), display.getHeight(), false, true);
        fileInputStream.close();

        highscoreCardStage.setX(display.getX());
        highscoreCardStage.setY(display.getY());
        highscoreCardStage.setHeight(display.getHeight());
        highscoreCardStage.setWidth(display.getWidth());
        if (display.getRotation() == 90 || display.getRotation() == 270) {
          highscoreCardStage.setHeight(display.getWidth());
        }

        imageView.setPreserveRatio(false);
        imageView.setFitWidth(display.getWidth());
        imageView.setFitHeight(display.getHeight());
        root.setRotate(display.getRotation());
      } else {
        FileInputStream fileInputStream = new FileInputStream(file);
        image = new Image(fileInputStream);
        fileInputStream.close();

        int targetX = (int) (bounds.getHeight() / 2 - image.getWidth() / 2);
        highscoreCardStage.setX(bounds.getMinX() / 2 + image.getWidth() / 2);
        highscoreCardStage.setY(targetX);
        highscoreCardStage.setHeight(image.getWidth() + 12);
        highscoreCardStage.setWidth(image.getWidth() + 12);

        int rotation = 0;
        String rotationValue = cardSettings.getNotificationRotation();
        if (rotationValue != null) {
          try {
            rotation = Integer.parseInt(rotationValue);
          } catch (NumberFormatException e) {
            LOG.info("Error reading card rotation value: " + e.getMessage());
          }
        }
        imageView.setRotate(rotation);
      }

      imageView.setImage(image);
    } catch (IOException e) {
      LOG.error("Failed to show card: " + e.getMessage(), e);
    }
  }

  public Node getRoot() {
    return root;
  }
}