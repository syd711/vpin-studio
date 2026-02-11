package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.lang.invoke.MethodHandles;

public class StudioMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final boolean VLC_AVAILABLE;

  static {
    boolean available = false;
    try {
      available = new uk.co.caprica.vlcj.factory.discovery.NativeDiscovery().discover();
      if (available) {
        LOG.info("VLC native libraries discovered, using VLC for video playback");
      }
      else {
        LOG.warn("VLC native libraries not found, falling back to JavaFX MediaPlayer for video playback");
      }
    }
    catch (Exception | NoClassDefFoundError e) {
      LOG.warn("VLC not available, falling back to JavaFX MediaPlayer for video playback: {}", e.getMessage());
    }
    VLC_AVAILABLE = available;
  }

  private MediaPlayerFactory mediaPlayerFactory;
  private EmbeddedMediaPlayer mediaPlayer;
  private MediaPlayer jfxMediaPlayer;

  public Node render(@NonNull FrontendScreenAsset asset) {
    if (VLC_AVAILABLE) {
      return renderVlcMediaPlayer(asset.getUrl().toExternalForm());
    }

    return renderJfxMediaPlayer(asset.getUrl().toExternalForm());
  }

  private Node renderVlcMediaPlayer(String mediaUrl) {
    mediaPlayerFactory = new MediaPlayerFactory("--no-video-title-show");
    mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

    ImageView imageView = new ImageView();
    mediaPlayer.videoSurface().set(ImageViewVideoSurfaceFactory.videoSurfaceForImageView(imageView));
    mediaPlayer.media().play(mediaUrl);
    return imageView;
  }

  private Node renderJfxMediaPlayer(String mediaUrl) {
    MediaView mediaView = new MediaView();
    renderJfxMediaPlayerWithAttempts(mediaView, mediaUrl, 0);
    return mediaView;
  }

  private void renderJfxMediaPlayerWithAttempts(MediaView mediaView, String mediaUrl, int attempt) {
    if (attempt == 100) {
      return;
    }
    int attemptCount = attempt + 1;
    Media media = new Media(mediaUrl);
    jfxMediaPlayer = new MediaPlayer(media);
    jfxMediaPlayer.setCycleCount(-1);
    jfxMediaPlayer.setMute(false);
    jfxMediaPlayer.setOnError(() -> {
      LOG.warn("Pause Menu Media player error:{}/{}, URL: {}", jfxMediaPlayer.getError(), attemptCount, jfxMediaPlayer.getMedia().getSource());
      jfxMediaPlayer.stop();
      jfxMediaPlayer.dispose();
      mediaView.setMediaPlayer(null);
      renderJfxMediaPlayerWithAttempts(mediaView, mediaUrl, attemptCount);
    });
    mediaView.setMediaPlayer(jfxMediaPlayer);

    Platform.runLater(() -> {
      jfxMediaPlayer.play();
    });
  }

  public void dispose() {
    if (mediaPlayer != null) {
      mediaPlayer.controls().stop();
      mediaPlayer.release();
    }
    if (mediaPlayerFactory != null) {
      mediaPlayerFactory.release();
    }
    if (jfxMediaPlayer != null) {
      jfxMediaPlayer.stop();
      jfxMediaPlayer.dispose();
    }
  }
}
