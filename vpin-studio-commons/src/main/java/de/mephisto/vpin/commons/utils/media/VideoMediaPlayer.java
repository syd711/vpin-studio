package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoMediaPlayer extends AssetMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(VideoMediaPlayer.class);

  private final String mimeType;

  private MediaView mediaView;
  private Media media;

  private boolean invertPlayfield;

  public VideoMediaPlayer(@NonNull String mimeType, boolean invertPlayfield) {
    super();
    this.mimeType = mimeType;
    this.invertPlayfield = invertPlayfield;
  }

  public void render(@NonNull String url, @NonNull String screenName) {
    render(null, screenName, url);
  }

  public void render(@NonNull FrontendMediaItemRepresentation mediaItem, @NonNull String url) {
    render(mediaItem, mediaItem.getScreen(), url);
  }
    
  private void render(@NonNull FrontendMediaItemRepresentation mediaItem, @NonNull String screenName, @NonNull String url) {

    String baseType = mimeType.split("/")[0];
    String mediaType = mimeType.split("/")[1];

    if (mediaType.equalsIgnoreCase("quicktime")) {
      setCenter(getEncodingNotSupportedLabel(mediaItem));
      return;
    }

    setLoading();

    media = new Media(url);
    LOG.info("Streaming media: " + url);
    mediaPlayer = new MediaPlayer(media);

    mediaPlayer.setOnError(() -> {
      LOG.warn("Media player error: " + mediaPlayer.getError() + ", URL: " + mediaPlayer.getMedia().getSource());
      disposeMedia();
      setCenter(getErrorLabel(mediaItem));
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

      if (mediaOptions == null || mediaOptions.isAutoRotate()) {
        scaleMediaView(screenName);
      }

      mediaView.setVisible(true);

      setCenter(mediaView);
    });
  }

  private void scaleMediaView(String screenName) {
    if ("PlayField".equalsIgnoreCase(screenName)) {
      if (media.getWidth() > media.getHeight()) {
        mediaView.setRotate(90 + (invertPlayfield ? 180 : 0));
        setRotated(true);
      }
    }
    else if ("Loading".equalsIgnoreCase(screenName)) {
      if (media.getWidth() > media.getHeight()) {
        mediaView.setRotate(90);
        setRotated(true);
      }
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
