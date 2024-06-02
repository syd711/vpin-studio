package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoMediaPlayer extends AssetMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(VideoMediaPlayer.class);
  public static final int MEDIA_SIZE = 280;

  private PopperScreen screen;

  private final String mimeType;

  private final boolean dialogRendering;

  private GameMediaItemRepresentation mediaItem;
  private MediaView mediaView;
  private Media media;

  public VideoMediaPlayer(@NonNull BorderPane parent, @NonNull String url, @NonNull String screenName, @NonNull String mimeType) {
    super(parent, url);
    this.mimeType = mimeType;
    this.dialogRendering = true;

    if (screenName.equalsIgnoreCase("PlayField")) {
      screen = PopperScreen.PlayField;
    }
    else if (screenName.equalsIgnoreCase("Loading")) {
      screen = PopperScreen.Loading;
    }

    this.render();
  }

  public VideoMediaPlayer(@NonNull BorderPane parent, @NonNull GameMediaItemRepresentation mediaItem, @NonNull String url, @NonNull String mimeType, boolean dialogRendering) {
    super(parent, url);
    this.mediaItem = mediaItem;
    this.mimeType = mimeType;
    this.dialogRendering = dialogRendering;

    if (mediaItem.getScreen().equalsIgnoreCase("PlayField")) {
      screen = PopperScreen.PlayField;
    }
    else if (mediaItem.getScreen().equalsIgnoreCase("Loading")) {
      screen = PopperScreen.Loading;
    }

    this.render();
  }

  private void render() {
    String baseType = mimeType.split("/")[0];
    String mediaType = mimeType.split("/")[1];

    if (mediaType.equalsIgnoreCase("quicktime")) {
      parent.setCenter(getEncodingNotSupportedLabel(mediaItem));
      return;
    }

    media = new Media(url);
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(baseType.equals("video"));
    mediaPlayer.setCycleCount(-1);
    mediaPlayer.setMute(true);
    mediaPlayer.setOnError(() -> {
      LOG.error("Media player error: " + mediaPlayer.getError() + ", URL: " + url);
      disposeMedia();
      parent.setCenter(getErrorLabel(mediaItem));
    });

    mediaView = new MediaView(mediaPlayer);
    mediaView.setUserData(mediaItem);
    mediaView.setPreserveRatio(true);
    mediaView.setVisible(false);

    mediaPlayer.setOnReady(() -> {
      scaleMediaView();
      mediaView.setVisible(true);
    });

    this.setCenter(mediaView);
    parent.setCenter(this);
  }

  private void scaleMediaView() {
    double prefWidth = parent.getPrefWidth();
    if (prefWidth <= 0) {
      prefWidth = ((Pane) parent.getParent()).getWidth();
    }
    double prefHeight = parent.getPrefHeight();
    if (prefHeight <= 0) {
      prefHeight = ((Pane) parent.getParent()).getHeight();
    }
    prefWidth = prefWidth - 12;
    prefHeight = prefHeight - 12;

    if (!dialogRendering) {
      prefHeight = prefHeight - 32;
    }

    if (PopperScreen.PlayField.equals(screen)) {
      if (media.getWidth() > media.getHeight()) {
        mediaView.setRotate(90);
        mediaView.setFitWidth(prefHeight);
        mediaView.setFitHeight(prefWidth);

        if (!dialogRendering) {
          mediaView.setX(0);
          mediaView.setY(0);
          Platform.runLater(() -> {
            double ratio = (double) media.getWidth() / media.getHeight();
            if (ratio > 1.5) {
              mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 74);
            }
            else {
              mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 12);
            }
          });
        }
      }
      else {
        mediaView.setFitWidth(prefWidth);
        mediaView.setFitHeight(prefHeight);
      }
    }
    else if (PopperScreen.Loading.equals(screen)) {
      if (media.getWidth() > media.getHeight()) {
        mediaView.setRotate(90);
        mediaView.setFitWidth(prefHeight);
        mediaView.setFitHeight(prefWidth);
      }
      else {
        mediaView.setFitWidth(prefWidth);
        mediaView.setFitHeight(prefHeight);
      }
    }
    else {
      mediaView.setFitWidth(prefWidth);
      mediaView.setFitHeight(prefHeight);
    }
  }

  public void scaleForDialog(String screen) {
    if (PopperScreen.PlayField.name().equals(screen) || PopperScreen.Loading.name().equals(screen)) {
      mediaView.setFitWidth(parent.getPrefWidth() - 300);
      mediaView.setFitHeight(parent.getPrefHeight() - 300);
    }
    else {
      mediaView.setFitWidth(parent.getPrefWidth() - 12);
      mediaView.setFitHeight(parent.getPrefHeight() - 50);
    }
  }

  @Override
  public void disposeMedia() {
    super.disposeMedia();
    if (mediaView != null) {
      this.mediaView.setMediaPlayer(null);
    }
  }
}
