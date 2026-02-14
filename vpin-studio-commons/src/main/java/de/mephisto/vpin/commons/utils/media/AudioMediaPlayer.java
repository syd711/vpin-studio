package de.mephisto.vpin.commons.utils.media;

import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class AudioMediaPlayer extends AssetMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static AudioMediaPlayer activePlayer = null;

  protected int retryCounter = 0;

  private ProgressBar progressBar;
  private DoubleBinding binding;
  private MediaView mediaView;
  private FontIcon fontIcon;

  public AudioMediaPlayer() {
    super();
  }

  public void render(@NonNull String url) {
    setLoading();

    Media media = new Media(url);
    mediaPlayer = new MediaPlayer(media);

    mediaPlayer.setOnError(() -> {
      LOG.warn("Media player error: " + mediaPlayer.getError() + ", URL: " + mediaPlayer.getMedia().getSource());

      if (retryCounter < 1) {
        retryCounter++;
        Platform.runLater(() -> {
          super.disposeMedia();
          try {
            Thread.sleep(500);
          }
          catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          render(url);
        });
      }
      else {
        setCenter(getErrorLabel());
      }
    });

    mediaPlayer.setOnReady(() -> {
      for (MediaPlayerListener listener : this.listeners) {
        listener.onReady(media);
      }

      fontIcon = new FontIcon();
      fontIcon.setIconSize(48);
      fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
      fontIcon.setIconLiteral("bi-play");

      VBox box = new VBox();
      box.setSpacing(6);
      box.setAlignment(Pos.CENTER);
      Button playBtn = new Button();
      playBtn.setGraphic(fontIcon);
      box.getChildren().add(playBtn);

      progressBar = new ProgressBar();
      progressBar.setVisible(false);
      progressBar.setMaxWidth(100);
      progressBar.setPrefHeight(12);
      box.getChildren().add(progressBar);

      this.setCenter(box);

      mediaPlayer.setAutoPlay(false);
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(false);

      mediaView = new MediaView(mediaPlayer);
      mediaPlayer.setOnEndOfMedia(() -> {
        fontIcon.setIconLiteral("bi-play");
        mediaView.getMediaPlayer().stop();
        progressBar.setVisible(false);

        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        mediaView.getMediaPlayer().seek(Duration.ZERO);
        bindProgress(mediaPlayer, progressBar);
      });

      playBtn.setOnAction(event -> {
        String iconLiteral = fontIcon.getIconLiteral();
        if (iconLiteral.equals("bi-play")) {
          progressBar.setVisible(true);
          mediaView.getMediaPlayer().setMute(false);
          mediaView.getMediaPlayer().setCycleCount(1);
          mediaView.getMediaPlayer().play();
          fontIcon.setIconLiteral("bi-stop");

          if (activePlayer != null && !activePlayer.equals(this)) {
            try {
              activePlayer.mediaView.getMediaPlayer().pause();
              activePlayer.fontIcon.setIconLiteral("bi-play");
            }
            catch (Exception e) {
              //ignore
            }
          }
          activePlayer = this;
        }
        else {
          progressBar.setVisible(false);
          mediaView.getMediaPlayer().stop();
          fontIcon.setIconLiteral("bi-play");
        }
      });

      bindProgress(mediaPlayer, progressBar);
    });
  }

  private void bindProgress(MediaPlayer player, ProgressBar bar) {
    binding = Bindings.createDoubleBinding(
        () -> {
          var currentTime = player.getCurrentTime();
          var duration = player.getMedia().getDuration();
          if (isValidDuration(currentTime) && isValidDuration(duration)) {
            return currentTime.toMillis() / duration.toMillis();
          }
          return ProgressBar.INDETERMINATE_PROGRESS;
        },
        player.currentTimeProperty(),
        player.getMedia().durationProperty());
    bar.progressProperty().bind(binding);
  }

  private boolean isValidDuration(Duration d) {
    return d != null && !d.isIndefinite() && !d.isUnknown();
  }
}
