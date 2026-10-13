package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.StudioMediaPlayer;
import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
  private StackPane root;

  private StudioMediaPlayer mediaPlayer;

  // Add a public no-args constructor
  public FrontendScreenController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    imageView.setPreserveRatio(false);
  }

  public void setMediaAsset(FrontendScreenAsset screenAsset) {
    screenAsset.setFrontendScreenController(this);
    try {
      root.setPadding(new Insets(0, 0, 0, 0)); //reset of the other options

      if (screenAsset.getDisplay() != null) {
        showOnDisplay(screenAsset);
      }
      else {
        MonitorInfo screen = ServerFX.client.getSystemService().getScreenInfo(-1);
        showCentered(screenAsset, screen);
      }
    }
    catch (IOException e) {
      LOG.error("Failed to media screen: " + e.getMessage(), e);
    }
  }

  private void showCentered(FrontendScreenAsset asset, MonitorInfo screen) throws IOException {
    URL url = asset.getUrl();
    Image image = new Image(url.openStream());
    LOG.info("Showing asset centered on playfield (" + image.getWidth() + "/" + image.getHeight() + ")");

    // the monitor info is in physical pixels, the stage uses JavaFX logical units, so apply the Windows display scaling
    double scaling = screen.getScaling() > 0 ? screen.getScaling() : 1;
    double imageWidth = image.getWidth() / scaling;
    double imageHeight = image.getHeight() / scaling;

    imageView.setImage(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(imageWidth);
    imageView.setFitHeight(imageHeight);

    Stage screenStage = asset.getScreenStage();
    screenStage.setX(0);
    screenStage.setY(0);
    screenStage.setHeight(screen.getScaledHeight());
    screenStage.setWidth(screen.getScaledWidth());

    int targetRotation = asset.getRotation() + (-90);
    root.setRotate(targetRotation);
    root.setPadding(new Insets(0, 0, 0, 0));
    if (targetRotation == -90 || targetRotation == 90) {
      //move the image a bit up for more distance to the wheel selector
      root.setPadding(new Insets(0, 0, imageHeight / 2 + imageHeight / 2 / 2, 0));
      if (targetRotation == 90) {
        root.setPadding(new Insets(imageHeight / 2 + imageHeight / 2 / 2, 0, 0, 0));
      }
    }

    LOG.info("Centered asset layout: screen=" + screen.getWidth() + "x" + screen.getHeight() + " (scaling " + scaling + ")"
        + ", image=" + image.getWidth() + "x" + image.getHeight()
        + ", assetRotation=" + asset.getRotation() + ", rootRotation=" + targetRotation
        + ", padding=" + root.getPadding());

    Platform.runLater(() -> {
      try {
        Bounds b = imageView.localToScreen(imageView.getBoundsInLocal());
        if (b == null) {
          LOG.info("Centered asset bounds: not available (image view not on screen)");
          return;
        }
        double imgCenterX = b.getMinX() + b.getWidth() / 2;
        double imgCenterY = b.getMinY() + b.getHeight() / 2;
        double screenCenterX = screenStage.getX() + screenStage.getWidth() / 2;
        double screenCenterY = screenStage.getY() + screenStage.getHeight() / 2;
        LOG.info("Centered asset bounds: stage=(" + screenStage.getX() + "/" + screenStage.getY() + " "
            + screenStage.getWidth() + "x" + screenStage.getHeight() + ")"
            + ", image on screen=(" + b.getMinX() + "/" + b.getMinY() + " " + b.getWidth() + "x" + b.getHeight() + ")"
            + ", center offset=(" + (imgCenterX - screenCenterX) + "/" + (imgCenterY - screenCenterY) + ")");
      }
      catch (Exception e) {
        LOG.warn("Failed to log centered asset bounds: " + e.getMessage());
      }
    });
  }

  private void showOnDisplay(FrontendScreenAsset asset) throws IOException {
    Image image;
    FrontendPlayerDisplay display = asset.getDisplay();
    String mimeType = asset.getMimeType();

    LOG.info("Showing asset on display \"" + display + "\"");
    if (mimeType.startsWith("image")) {
      InputStream inputStream = asset.getUrl().openStream();
      image = new Image(inputStream, display.getWidth(), display.getHeight(), false, true);
      inputStream.close();

      imageView.setPreserveRatio(false);
      imageView.setFitWidth(display.getWidth());
      imageView.setFitHeight(display.getHeight());
      imageView.setImage(image);
    }
    else if (mimeType.startsWith("video")) {
      imageView.setVisible(false);

      mediaPlayer = new StudioMediaPlayer();
      Node node = mediaPlayer.render(display, asset);
      if (node != null) {
        root.getChildren().add(node);
      }
      else {
        return;
      }
    }
    else {
      LOG.error("Unsupported mime type for screen asset: " + mimeType);
      throw new UnsupportedEncodingException("Unsupported mime type for screen asset: " + mimeType);
    }

    showStage(asset, display);
  }

  private void showStage(FrontendScreenAsset asset, FrontendPlayerDisplay display) {
    Stage screenStage = asset.getScreenStage();
    screenStage.setTitle("VPin UI");
    screenStage.setX(display.getX() + asset.getOffsetX());
    screenStage.setY(display.getY() + asset.getOffsetY());
    screenStage.setHeight(display.getHeight());
    screenStage.setWidth(display.getWidth());

    if (asset.getRotation() == 90 || asset.getRotation() == 270) {
      screenStage.setX(screenStage.getX() + ((double) display.getWidth() / 2) - ((double) display.getHeight() / 2));
      screenStage.setY(screenStage.getY() - ((double) display.getHeight() / 2) + ((double) display.getHeight() / 2));
      screenStage.setHeight(display.getWidth());
      screenStage.setWidth(display.getHeight());
    }
    else if (asset.getRotation() == 180) {
      screenStage.setY(screenStage.getY() + display.getHeight());
    }

    root.setRotate(asset.getRotation());

    if (asset.getRotation() == 90 || asset.getRotation() == 270) {
      root.translateXProperty().setValue(-(display.getHeight() + (display.getHeight() / 2)));
    }
  }

  public void dispose() {
    root.getChildren().removeAll(root.getChildren());
    if (mediaPlayer != null) {
      mediaPlayer.dispose();
    }
  }
}