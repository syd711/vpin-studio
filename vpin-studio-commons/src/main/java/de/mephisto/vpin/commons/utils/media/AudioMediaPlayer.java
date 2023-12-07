package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioMediaPlayer extends AssetMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(VideoMediaPlayer.class);

  public AudioMediaPlayer(@NonNull BorderPane parent, @NonNull String url) {
    this(parent, null, url);
  }

  public AudioMediaPlayer(@NonNull BorderPane parent, @Nullable GameMediaItemRepresentation mediaItem, @NonNull String url) {
    super(parent);

    Media media = new Media(url);
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(false);
    mediaPlayer.setCycleCount(-1);
    mediaPlayer.setMute(true);
    mediaPlayer.setOnError(() -> {
      LOG.error("Media player error for URL {}: {}", url, mediaPlayer.getError() + ", URL: " + url);
      mediaPlayer.stop();
      mediaPlayer.dispose();

      parent.setCenter(getErrorLabel(mediaItem));
    });

    mediaPlayer.setOnEndOfMedia(() -> {
      VBox bottom = (VBox) parent.getBottom();
      Button playButton = (Button) bottom.getChildren().get(0);
      playButton.setVisible(true);
      FontIcon icon = (FontIcon) playButton.getChildrenUnmodifiable().get(0);
      icon.setIconLiteral("bi-play");
    });

    MediaView mediaView = new MediaView(mediaPlayer);
    parent.setCenter(mediaView);
  }
}
