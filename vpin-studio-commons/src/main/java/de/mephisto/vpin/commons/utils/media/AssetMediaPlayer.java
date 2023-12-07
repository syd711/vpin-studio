package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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

  public AssetMediaPlayer(@NonNull BorderPane parent) {

    this.parent = parent;
  }

  protected Label getErrorLabel(@Nullable GameMediaItemRepresentation mediaItem) {
    Label label = new Label("  Media available\n(but not playable)");
    label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
    label.setUserData(mediaItem);
    return label;
  }

  public void disposeMedia() {
    if (parent.getCenter() != null) {
      disposeMediaBorderPane(parent);
    }
  }

  public void disposeMediaBorderPane(BorderPane node) {
    Node center = node.getCenter();
    if (center != null) {
      if (center instanceof MediaView) {
        MediaView view = (MediaView) center;
        if (view.getMediaPlayer() != null) {
          String source = view.getMediaPlayer().getMedia().getSource();
          view.getMediaPlayer().stop();
          final ExecutorService executor = Executors.newFixedThreadPool(1);
          final Future<?> future = executor.submit(() -> {
            view.getMediaPlayer().dispose();
          });
          try {
            future.get(500, TimeUnit.MILLISECONDS);
          } catch (Exception e) {
            LOG.error("Error disposing media view (" + source + "): " + e.getMessage());
          }
          executor.shutdownNow();
        }
        node.setCenter(null);
      }
      else if (center instanceof ImageView) {
        ImageView view = (ImageView) center;
        view.setImage(null);
      }
      else {
        node.setCenter(null);
      }
    }
  }
}
