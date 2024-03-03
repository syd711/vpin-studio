package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.PopperScreensManager;
import de.mephisto.vpin.commons.fx.pausemenu.model.PopperScreenAsset;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopperScreenController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PopperScreenController.class);

  @FXML
  private ImageView imageView;

  @FXML
  private MediaView mediaView;

  @FXML
  private StackPane root;

  // Add a public no-args constructor
  public PopperScreenController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    imageView.setPreserveRatio(false);
  }

  public void setMediaAsset(PopperScreenAsset screenAsset) {
    try {
      Screen screen = Screen.getPrimary();
      Rectangle2D bounds = screen.getVisualBounds();
      root.setPadding(new Insets(0, 0, 0, 0)); //reset of the other options
      root.setRotate(0);//reset of the other options

      if (screenAsset.getDisplay() != null) {
        showOnDisplay(screenAsset);
      }
      else {
        showCentered(screenAsset, bounds);
      }
    } catch (IOException e) {
      LOG.error("Failed to media screen: " + e.getMessage(), e);
    }
  }

  private void showCentered(PopperScreenAsset asset, Rectangle2D bounds) throws IOException {
    InputStream in = asset.getInputStream();
    Image image = new Image(in);
    in.close();
    LOG.info("Showing asset centered on playfield (" + image.getWidth() + "/" + image.getHeight() + ")");

    imageView.setImage(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(image.getWidth());
    imageView.setFitHeight(image.getHeight());

    Stage screenStage = asset.getScreenStage();
    screenStage.setX(bounds.getWidth() / 2 - image.getWidth() / 2);
    screenStage.setY(bounds.getHeight() / 2 - image.getHeight() / 2);
    screenStage.setHeight(image.getHeight() + 12);
    screenStage.setWidth(image.getWidth() + 12);

    int targetRotation = asset.getRotation() + (-90);
    root.setRotate(targetRotation);
    root.setPadding(new Insets(3, 0, 0, 0));

    if (targetRotation == -90 || targetRotation == 90) {
      screenStage.setX(bounds.getWidth() / 2 - image.getHeight() / 2);
      screenStage.setY(bounds.getHeight() / 2 - image.getWidth() / 2);
      screenStage.setWidth(image.getHeight() + 12);
      screenStage.setHeight(image.getWidth() + 12);

      root.setPadding(new Insets(0, 0, image.getHeight() / 2 + image.getHeight() / 2 / 2 + 6, 0));
      if (targetRotation == 90) {
        root.setPadding(new Insets(image.getHeight() / 2 + image.getHeight() / 2 / 2 + 6, 0, 0, 0));
      }
    }
  }

  private void showOnDisplay(PopperScreenAsset asset) throws IOException {
    Image image;
    PinUPPlayerDisplay display = asset.getDisplay();
    String mimeType = asset.getMimeType();

    LOG.info("Showing asset on display \"" + display + "\"");
    InputStream inputStream = asset.getInputStream();
    image = new Image(inputStream, display.getWidth(), display.getHeight(), false, true);
    inputStream.close();

    if (mimeType.startsWith("image")) {
      mediaView.setVisible(false);
      imageView.setPreserveRatio(false);
      imageView.setFitWidth(display.getWidth());
      imageView.setFitHeight(display.getHeight());
      imageView.setImage(image);
    }
    else if (mimeType.startsWith("video")) {
      imageView.setVisible(false);

      Media media = new Media(asset.getUrl());
      MediaPlayer mediaPlayer = new MediaPlayer(media);
      mediaPlayer.setAutoPlay(true);
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(false);

      mediaView.setPreserveRatio(false);
      mediaView.setFitWidth(display.getWidth());
      mediaView.setFitHeight(display.getHeight());
      mediaView.setMediaPlayer(mediaPlayer);

      asset.setMediaPlayer(mediaPlayer);
    }
    else {
      LOG.error("Unsupported mime type for screen asset: " + mimeType);
      throw new UnsupportedEncodingException("Unsupported mime type for screen asset: " + mimeType);
    }

    Stage screenStage = asset.getScreenStage();
    screenStage.setX(display.getX() + 6);
    screenStage.setY(display.getY() + 6);
    screenStage.setHeight(display.getHeight() + 12);
    screenStage.setWidth(display.getWidth() + 12);

    root.setRotate(display.getRotation());

    if (display.getRotation() == 90 || display.getRotation() == 270) {
      screenStage.setWidth(display.getHeight() + 12);
      screenStage.setHeight(display.getWidth() + 12);
      root.setPadding(new Insets(display.getHeight() / 2 + display.getHeight() / 2 / 2, 0, 0, 0));
      if (display.getRotation() == 270) {
        root.setPadding(new Insets(0, 0, display.getHeight() / 2 + display.getHeight() / 2 / 2, 0));
      }
    }
  }

  public Node getRoot() {
    return root;
  }
}