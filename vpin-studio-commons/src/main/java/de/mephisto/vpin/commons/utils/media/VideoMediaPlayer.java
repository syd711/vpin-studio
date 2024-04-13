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

  private final boolean portraitMode;
  private final boolean dialogRendering;

  private GameMediaItemRepresentation mediaItem;
  private MediaView mediaView;

  public VideoMediaPlayer(@NonNull BorderPane parent, @NonNull String url, @NonNull String screenName, @NonNull String mimeType, boolean portraitMode) {
    super(parent, url);
    this.mimeType = mimeType;
    this.portraitMode = portraitMode;
    this.dialogRendering = true;

    if (screenName.equalsIgnoreCase("PlayField")) {
      screen = PopperScreen.PlayField;
    }
    else if (screenName.equalsIgnoreCase("Loading")) {
      screen = PopperScreen.Loading;
    }

    this.render();
  }

  public VideoMediaPlayer(@NonNull BorderPane parent, @NonNull GameMediaItemRepresentation mediaItem, @NonNull String url, @NonNull String mimeType, boolean portraitMode, boolean dialogRendering) {
    super(parent, url);
    this.mediaItem = mediaItem;
    this.mimeType = mimeType;
    this.portraitMode = portraitMode;
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

    Media media = new Media(url);
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(baseType.equals("video"));
    mediaPlayer.setCycleCount(-1);
    mediaPlayer.setMute(true);
    mediaPlayer.setOnError(() -> {
      LOG.error("Media player error: " + mediaPlayer.getError() + ", URL: " + url);

      if (retryCounter < 5) {
        retryCounter++;
        Platform.runLater(() -> {
          super.disposeMedia();
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          render();
        });
      }
      else {
        parent.setCenter(getErrorLabel(mediaItem));
      }
    });

    mediaView = new MediaView(mediaPlayer);
    mediaView.setUserData(mediaItem);
    mediaView.setPreserveRatio(true);

    double prefWidth = parent.getPrefWidth();
    if (prefWidth <= 0) {
      prefWidth = ((Pane) parent.getParent()).getWidth();
    }
    double prefHeight = parent.getPrefHeight();
    if (prefHeight <= 0) {
      prefHeight = ((Pane) parent.getParent()).getHeight();
    }
    prefWidth = prefWidth - 12;
    prefHeight = prefHeight - 50;

    if (PopperScreen.PlayField.equals(screen)) {
      if (dialogRendering) {
        mediaView.setFitWidth(prefWidth);
        mediaView.setFitHeight(prefHeight);
        mediaView.setRotate(90);
      }
      else {
        this.scalePlayfieldForSidebar();
      }
    }
    else if (PopperScreen.Loading.equals(screen)) {
      if (dialogRendering) {
        mediaView.setFitWidth(prefWidth);
        mediaView.setFitHeight(prefHeight);
        mediaView.setRotate(90);
      }
      else {
        this.scaleLoadingForSidebar();
      }
    }
    else {
      mediaView.setFitWidth(prefWidth);
      mediaView.setFitHeight(prefHeight);
    }

    this.setCenter(mediaView);
    parent.setCenter(this);
  }

  private void scalePlayfieldForSidebar() {
    mediaView.setFitWidth(250);
    if (!portraitMode) {
      mediaView.rotateProperty().set(90);
      mediaView.setFitWidth(440);
      mediaView.setX(0);
      mediaView.setY(0);
      mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 96);
    }
  }

  private void scaleLoadingForSidebar() {
    mediaView.setFitWidth(150);
    if (!portraitMode) {
      mediaView.rotateProperty().set(90);
      mediaView.setFitWidth(70);
      mediaView.setX(0);
      mediaView.setY(0);
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
}
