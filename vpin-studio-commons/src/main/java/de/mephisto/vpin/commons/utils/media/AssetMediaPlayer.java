package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.control.Label;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

abstract public class AssetMediaPlayer extends MediaViewPane {
  private final static Logger LOG = LoggerFactory.getLogger(AssetMediaPlayer.class);

  protected MediaPlayer mediaPlayer;

  protected final java.util.List<MediaPlayerListener> listeners = new ArrayList<>();

  public AssetMediaPlayer() {
  }

  public void addListener(MediaPlayerListener listener) {
    this.listeners.add(listener);
  }

  protected Label getErrorLabel(@Nullable FrontendMediaItemRepresentation mediaItem) {
    Label label = new Label("  Media available\n(but not playable)");
    label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
    label.setUserData(mediaItem);
    return label;
  }

  protected Label getEncodingNotSupportedLabel(@Nullable FrontendMediaItemRepresentation mediaItem) {
    Label label = new Label("        Media available\n(encoding not supported)");
    label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
    label.setUserData(mediaItem);
    return label;
  }

  public boolean hasMediaPlayer() {
    return mediaPlayer != null;
  }

  public void disposeMedia() {
    for (MediaPlayerListener listener : this.listeners) {
      listener.onDispose();
    }
    this.listeners.clear();

    if (mediaPlayer != null) {
      try {
        mediaPlayer.stop();
      } catch (Exception e) {
        LOG.info("Stopping media view: " + e.getMessage());
      }

      try {
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final Future<?> future = executor.submit(() -> {
          mediaPlayer.dispose();
        });

        future.get(500, TimeUnit.MILLISECONDS);
        executor.shutdownNow();
      }
      catch (Exception e) {
        LOG.info("Disposing media view: " + e.getMessage());
      }
    }
  }

  public void play() {
    if (mediaPlayer != null) {
      mediaPlayer.play();;
    }
  }

  public void pause() {
    if (mediaPlayer != null) {
      mediaPlayer.pause();;
    }
  }

  public void stopAndDispose() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();;
      mediaPlayer.dispose();
    }
  }

}
