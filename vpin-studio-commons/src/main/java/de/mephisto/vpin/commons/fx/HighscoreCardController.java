package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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
      root.setPadding(new Insets(0, 0, 0, 0)); //reset of the other options
      root.setRotate(0);//reset of the other options

      Image image = null;
      if (cardSettings.isNotificationOnPopperScreen() && display != null) {
        LOG.info("Showing card \"" + file.getAbsolutePath() + "\", display \"" + display + "\" (using display: " + cardSettings.isNotificationOnPopperScreen() + ")");
        FileInputStream fileInputStream = new FileInputStream(file);
        image = new Image(fileInputStream, display.getWidth(), display.getHeight(), false, true);
        fileInputStream.close();

        imageView.setPreserveRatio(false);
        imageView.setFitWidth(display.getWidth());
        imageView.setFitHeight(display.getHeight());
        imageView.setImage(image);

        highscoreCardStage.setX(display.getX() + 6);
        highscoreCardStage.setY(display.getY() + 6);
        highscoreCardStage.setHeight(display.getHeight() + 12);
        highscoreCardStage.setWidth(display.getWidth() + 12);

        root.setRotate(display.getRotation());

        if (display.getRotation() == 90 || display.getRotation() == 270) {
          highscoreCardStage.setWidth(display.getHeight() + 12);
          highscoreCardStage.setHeight(display.getWidth() + 12);
          root.setPadding(new Insets( display.getHeight() / 2 + display.getHeight() / 2 / 2, 0, 0, 0));
          if (display.getRotation() == 270) {
            root.setPadding(new Insets(0, 0, display.getHeight() / 2 + display.getHeight() / 2 / 2, 0));
          }
        }
      }
      else {
        FileInputStream fileInputStream = new FileInputStream(file);
        image = new Image(fileInputStream);
        fileInputStream.close();
        LOG.info("Showing card \"" + file.getAbsolutePath() + "\" centered on playfield (" + image.getWidth() + "/" + image.getHeight() + ")");

        imageView.setImage(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(image.getWidth());
        imageView.setFitHeight(image.getHeight());

        highscoreCardStage.setX(bounds.getWidth() / 2 - image.getWidth() / 2);
        highscoreCardStage.setY(bounds.getHeight() / 2 - image.getHeight() / 2);
        highscoreCardStage.setHeight(image.getHeight() + 12);
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

        int targetRotation = rotation + (-90);
        root.setRotate(targetRotation);
        root.setPadding(new Insets(3, 0, 0, 0));

        if (targetRotation == -90 || targetRotation == 90) {
          highscoreCardStage.setX(bounds.getWidth() / 2 - image.getHeight() / 2);
          highscoreCardStage.setY(bounds.getHeight() / 2 - image.getWidth() / 2);
          highscoreCardStage.setWidth(image.getHeight() + 12);
          highscoreCardStage.setHeight(image.getWidth() + 12);

          root.setPadding(new Insets(0, 0, image.getHeight() / 2 + image.getHeight() / 2 / 2 + 6, 0));
          if (targetRotation == 90) {
            root.setPadding(new Insets(image.getHeight() / 2 + image.getHeight() / 2 / 2 + 6, 0, 0, 0));
          }
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to show card: " + e.getMessage(), e);
    }
  }

  public Node getRoot() {
    return root;
  }
}