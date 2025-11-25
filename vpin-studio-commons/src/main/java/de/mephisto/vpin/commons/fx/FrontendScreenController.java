package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontendScreenController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private ImageView imageView;

  @FXML
  private MediaView mediaView;

  @FXML
  private StackPane root;

  // Add a public no-args constructor
  public FrontendScreenController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    imageView.setPreserveRatio(false);
  }

  public void setMediaAsset(FrontendScreenAsset screenAsset) {
    try {
      root.setPadding(new Insets(0, 0, 0, 0)); //reset of the other options
      root.setRotate(0);//reset of the other options

      if (screenAsset.getDisplay() != null) {
        showOnDisplay(screenAsset);
      }
      else {
        MonitorInfo screen = ServerFX.client.getScreenInfo(-1);
        showCentered(screenAsset, screen);
      }
    }
    catch (IOException e) {
      LOG.error("Failed to media screen: " + e.getMessage(), e);
    }
  }

  private void showCentered(FrontendScreenAsset asset, MonitorInfo screen) throws IOException {
    InputStream in = asset.getInputStream();
    Image image = new Image(in);
    in.close();
    LOG.info("Showing asset centered on playfield (" + image.getWidth() + "/" + image.getHeight() + ")");

    imageView.setImage(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(image.getWidth());
    imageView.setFitHeight(image.getHeight());

    Stage screenStage = asset.getScreenStage();
//    double x = screen.getWidth() / 2 - image.getWidth() / 2;
//    double y = screen.getHeight() / 2 - image.getHeight() / 2;
    screenStage.setX(0);
    screenStage.setY(0);
    screenStage.setHeight(screen.getHeight());
    screenStage.setWidth(screen.getWidth());
//    screenStage.initStyle(StageStyle.UTILITY);

    int targetRotation = asset.getRotation() + (-90);
    root.setRotate(targetRotation);
    root.setPadding(new Insets(0, 0, 0, 0));

    if (targetRotation == -90 || targetRotation == 90) {
      //move the image a bit up for more distance to the wheel selector
      root.setPadding(new Insets(0, 0, image.getHeight() / 2 + image.getHeight() / 2 / 2, 0));
      if (targetRotation == 90) {
        root.setPadding(new Insets(image.getHeight() / 2 + image.getHeight() / 2 / 2, 0, 0, 0));
      }
    }
  }

  private void showOnDisplay(FrontendScreenAsset asset) throws IOException {
    Image image;
    FrontendPlayerDisplay display = asset.getDisplay();
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
    screenStage.setX(display.getX());
    screenStage.setY(display.getY());
    screenStage.setHeight(display.getHeight());
    screenStage.setWidth(display.getWidth());

    root.setRotate(display.getRotation());

    if (display.getRotation() == 90 || display.getRotation() == 270) {
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