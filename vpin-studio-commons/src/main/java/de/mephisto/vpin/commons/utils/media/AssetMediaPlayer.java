package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

abstract public class AssetMediaPlayer extends BorderPane {
  private final static Logger LOG = LoggerFactory.getLogger(AssetMediaPlayer.class);

  protected int retryCounter = 0;

  @NonNull
  protected final BorderPane parent;

  @NonNull
  protected final String url;


  protected MediaPlayer mediaPlayer;

  public AssetMediaPlayer(@NonNull BorderPane parent, @NonNull String url) {
    this.parent = parent;
    this.url = url;
  }

  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }

  protected Label getErrorLabel(@Nullable GameMediaItemRepresentation mediaItem) {
    Label label = new Label("  Media available\n(but not playable)");
    label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
    label.setUserData(mediaItem);
    return label;
  }

  public void disposeMedia() {
    if (getMediaPlayer() != null) {
      try {
        getMediaPlayer().stop();
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final Future<?> future = executor.submit(() -> {
          getMediaPlayer().dispose();
        });

        future.get(500, TimeUnit.MILLISECONDS);
        executor.shutdownNow();
        LOG.info("Disposed " + this.url);
      } catch (Exception e) {
        LOG.error("Error disposing media view: " + e.getMessage());
      }
    }
    else {
      LOG.error("No mediaplayer found for " + url);
    }
  }
}