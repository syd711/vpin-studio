package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
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

  private FrontendMediaItemRepresentation mediaItem;
  private MediaView mediaView;
  private Media media;

  private boolean invertPlayfield;

  public VideoMediaPlayer(@NonNull String url, @NonNull String screenName,
                          @NonNull String mimeType, boolean invertPlayfield) {
    this(null, screenName, url, mimeType, invertPlayfield);
  }

  public VideoMediaPlayer(@NonNull FrontendMediaItemRepresentation mediaItem, @NonNull String url,
                          @NonNull String mimeType, boolean invertPlayfield) {
    this(mediaItem, mediaItem.getScreen(), url, mimeType, invertPlayfield);
  }

  private VideoMediaPlayer(@NonNull FrontendMediaItemRepresentation mediaItem, @NonNull String screenName,
                           @NonNull String url, @NonNull String mimeType, boolean invertPlayfield) {
    super(url);
    this.mediaItem = mediaItem;
    this.mimeType = mimeType;
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
      scaleMediaView();
      mediaView.setVisible(true);

      setCenter(mediaView);
    });
  }

  private void scaleMediaView() {

    if (VPinScreen.PlayField.equals(screen)) {
      if (media.getWidth() > media.getHeight()) {
        mediaView.setRotate(90 + (invertPlayfield ? 180 : 0));
        setRotated(true);
       }
    }
    else if (VPinScreen.Loading.equals(screen)) {
      if (media.getWidth() > media.getHeight()) {
        mediaView.setRotate(90);
        setRotated(true);
      }
    }
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
