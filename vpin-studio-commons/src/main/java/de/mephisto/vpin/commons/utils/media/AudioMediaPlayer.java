package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
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

    VBox vBox = new VBox();
    vBox.setAlignment(Pos.BASELINE_CENTER);

    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(48);
    fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
    fontIcon.setIconLiteral("bi-play");

    Button playBtn = new Button();
    playBtn.setGraphic(fontIcon);
    vBox.getChildren().add(playBtn);
    this.setBottom(vBox);

    Media media = new Media(url);
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(false);
    mediaPlayer.setCycleCount(-1);
    mediaPlayer.setMute(false);
    mediaPlayer.setOnError(() -> {
      LOG.error("Media player error: " + mediaPlayer.getError());
      mediaPlayer.stop();
      mediaPlayer.dispose();

      parent.setCenter(getErrorLabel(mediaItem));
    });


    MediaView mediaView = new MediaView(mediaPlayer);
    mediaPlayer.setOnEndOfMedia(() -> {
      fontIcon.setIconLiteral("bi-play");
    });

    playBtn.setOnAction(event -> {
      String iconLiteral = fontIcon.getIconLiteral();
      if (iconLiteral.equals("bi-play")) {
        mediaView.getMediaPlayer().setMute(false);
        mediaView.getMediaPlayer().setCycleCount(1);
        mediaView.getMediaPlayer().play();
        fontIcon.setIconLiteral("bi-stop");
      }
      else {
        mediaView.getMediaPlayer().stop();
        fontIcon.setIconLiteral("bi-play");
      }
    });

    this.setCenter(mediaView);
    parent.setCenter(this);
  }

  @Override
  public void disposeMedia() {
    this.setBottom(null);
    super.disposeMedia();
  }
}
