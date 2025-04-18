package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.commons.utils.FXUtil;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.scene.control.ProgressIndicator;
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

  private VPinScreen screen;

  private final String mimeType;

  private final boolean dialogRendering;

  private FrontendMediaItemRepresentation mediaItem;
  private MediaView mediaView;
  private Media media;

  private double fitWidth = -1;
  private double fitHeight = -1;

  private boolean invertPlayfield;

  public VideoMediaPlayer(@NonNull BorderPane parent, @NonNull String url, @NonNull String screenName,
                          @NonNull String mimeType, boolean invertPlayfield) {
    this(parent, null, screenName, url, mimeType, invertPlayfield, true);
  }

  public VideoMediaPlayer(@NonNull BorderPane parent, @NonNull FrontendMediaItemRepresentation mediaItem, @NonNull String url,
                          @NonNull String mimeType, boolean invertPlayfield, boolean dialogRendering) {
    this(parent, mediaItem, mediaItem.getScreen(), url, mimeType, invertPlayfield, dialogRendering);
  }

  private VideoMediaPlayer(@NonNull BorderPane parent, @NonNull FrontendMediaItemRepresentation mediaItem, @NonNull String screenName,
                           @NonNull String url, @NonNull String mimeType, boolean invertPlayfield, boolean dialogRendering) {
    super(parent, url);
    this.mediaItem = mediaItem;
    this.mimeType = mimeType;
    this.dialogRendering = dialogRendering;
    this.invertPlayfield = invertPlayfield;

    if (screenName.equalsIgnoreCase("PlayField")) {
      screen = VPinScreen.PlayField;
    }
    else if (screenName.equalsIgnoreCase("Loading")) {
      screen = VPinScreen.Loading;
    }
  }

  public void render() {
    String baseType = mimeType.split("/")[0];
    String mediaType = mimeType.split("/")[1];

    if (mediaType.equalsIgnoreCase("quicktime")) {
      parent.setCenter(getEncodingNotSupportedLabel(mediaItem));
      return;
    }

    this.setCenter(new ProgressIndicator());
    parent.setCenter(this);

    media = new Media(url);
    LOG.info("Streaming media: " + url);
    mediaPlayer = new MediaPlayer(media);

    mediaPlayer.setOnError(() -> {
      LOG.warn("Media player error: " + mediaPlayer.getError() + ", URL: " + mediaPlayer.getMedia().getSource());
      disposeMedia();
      parent.setCenter(getErrorLabel(mediaItem));
    });

    mediaPlayer.setOnReady(() -> {
      for (MediaPlayerListener listener : this.listeners) {
        listener.onReady(media);
      }

      mediaPlayer.setAutoPlay(baseType.equals("video"));
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(true);

      mediaView = new MediaView(mediaPlayer);
      mediaView.setUserData(mediaItem);
      mediaView.setPreserveRatio(true);
      mediaView.setVisible(false);
      scaleMediaView();
      mediaView.setVisible(true);

      this.setCenter(mediaView);
    });
  }

  private void scaleMediaView() {
    if (fitWidth > 0 || fitHeight > 0) {
      mediaView.setFitWidth(fitWidth);
      mediaView.setFitHeight(fitWidth);
      return;
    }

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

    if (VPinScreen.PlayField.equals(screen)) {
      if (media.getWidth() > media.getHeight()) {
        mediaView.setRotate(90 + (invertPlayfield ? 180 : 0));
        mediaView.setFitWidth(prefHeight);
        mediaView.setFitHeight(prefWidth);

        if (!dialogRendering) {
          mediaView.setX(0);
          mediaView.setY(0);
          double ratio = (double) media.getWidth() / media.getHeight();
          if (ratio > 1.5) {
            mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 74);
          }
          else {
            mediaView.translateXProperty().set(mediaView.translateXProperty().get() - 12);
          }
        }
      }
      else {
        mediaView.setFitWidth(prefWidth);
        mediaView.setFitHeight(prefHeight);
      }
    }
    else if (VPinScreen.Loading.equals(screen)) {
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

  @Override
  public void setSize(double fitWidth, double fitHeight) {
    this.fitHeight = fitHeight;
    this.fitWidth = fitWidth;
  }

  @Override
  public void setMediaViewSize(double fitWidth, double fitHeight) {
    if (this.mediaView != null) {
      this.mediaView.setFitHeight(fitHeight);
      this.mediaView.setFitWidth(fitWidth);
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
