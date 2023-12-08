package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

abstract public class AssetMediaPlayer extends BorderPane {
  private final static Logger LOG = LoggerFactory.getLogger(AssetMediaPlayer.class);

  @NonNull
  protected final BorderPane parent;


  protected MediaPlayer mediaPlayer;

  public AssetMediaPlayer(@NonNull BorderPane parent) {
    this.parent = parent;
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
    Node center = this.getCenter();
    if (center != null) {
      if (center instanceof MediaView) {
        MediaView view = (MediaView) center;
        if (view.getMediaPlayer() != null) {
          try {
            view.getMediaPlayer().stop();
            final ExecutorService executor = Executors.newFixedThreadPool(1);
            final Future<?> future = executor.submit(() -> {
              view.getMediaPlayer().dispose();
            });

            future.get(500, TimeUnit.MILLISECONDS);
            executor.shutdownNow();
          } catch (Exception e) {
            LOG.error("Error disposing media view: " + e.getMessage());
          }
        }
      }
      else if (center instanceof ImageView) {
        ImageView view = (ImageView) center;
        view.setImage(null);
      }
    }
  }
}
