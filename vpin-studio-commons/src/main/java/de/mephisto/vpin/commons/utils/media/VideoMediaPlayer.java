package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoMediaPlayer extends AssetMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(VideoMediaPlayer.class);
  public static final int MEDIA_SIZE = 280;

  private final String url;

  private PopperScreen screen;

  private final String mimeType;

  private final boolean portraitMode;
  private final boolean dialogRendering;

  private GameMediaItemRepresentation mediaItem;

  public VideoMediaPlayer(@NonNull BorderPane parent, @NonNull String url, @NonNull String screenName, @NonNull String mimeType, boolean portraitMode) {
    super(parent);
    this.url = url;
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
    super(parent);
    this.mediaItem = mediaItem;
    this.url = url;
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
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(baseType.equals("video"));
    mediaPlayer.setCycleCount(-1);
    mediaPlayer.setMute(true);
    mediaPlayer.setOnError(() -> {
      LOG.error("Media player error: " + mediaPlayer.getError() + ", URL: " + url);
      mediaPlayer.stop();
      mediaPlayer.dispose();

      parent.setCenter(getErrorLabel(mediaItem));
    });

    MediaView mediaView = new MediaView(mediaPlayer);
    mediaView.setUserData(mediaItem);
    mediaView.setPreserveRatio(true);

    if (PopperScreen.PlayField.equals(screen)) {
      if (dialogRendering) {
        mediaView.setFitWidth(MEDIA_SIZE);
        mediaView.setFitHeight(MEDIA_SIZE);
        mediaView.setRotate(90);
      }
      else {
        this.scalePlayfieldForSidebar(mediaView);
      }
    }
    else if (PopperScreen.Loading.equals(screen)) {
      if (dialogRendering) {
        mediaView.setFitWidth(MEDIA_SIZE);
        mediaView.setFitHeight(MEDIA_SIZE);
        mediaView.setRotate(90);
      }
      else {
        this.scaleLoadingForSidebar(mediaView);
      }
    }
    else {
      mediaView.setFitWidth(parent.getPrefWidth() - 12);
      mediaView.setFitHeight(parent.getPrefHeight() - 50);
    }

    this.setCenter(mediaView);
    parent.setCenter(this);
  }

  private void scalePlayfieldForSidebar(MediaView mediaView) {
    mediaView.setFitWidth(250);
    if (!portraitMode) {
      mediaView.rotateProperty().set(90);
      mediaView.setFitWidth(440);
      mediaView.setX(0);
      mediaView.setY(0);
      mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 96);
    }
  }

  private void scaleLoadingForSidebar(MediaView mediaView) {
    mediaView.setFitWidth(150);
    if (!portraitMode) {
      mediaView.rotateProperty().set(90);
      mediaView.setFitWidth(70);
      mediaView.setX(0);
      mediaView.setY(0);
    }
  }
}
