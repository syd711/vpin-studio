package de.mephisto.vpin.commons.utils.media;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;

import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class VideoMediaPlayer extends AssetMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String mimeType;

  private ImageView imageView;
  private MediaView mediaView;

  private boolean modeVideo = false;
  private Button playBtn;

  private boolean invertPlayfield;


  public VideoMediaPlayer(@NonNull String mimeType, boolean invertPlayfield) {
    super();
    this.mimeType = mimeType;
    this.invertPlayfield = invertPlayfield;
  }

  public void render(@NonNull String url, @Nullable VPinScreen screen, boolean usePreview)  {

    if (StringUtils.endsWithIgnoreCase(mimeType, "/quicktime")) {
      setCenter(getEncodingNotSupportedLabel());
      return;
    }

    setLoading();

    if (usePreview) {
      JFXFuture.supplyAsync(() -> new Image(url + "?preview=true", false))
        .thenAcceptLater(image -> {
          // create an ImageView with a play button
          this.imageView = new ImageView(image);
          imageView.setPreserveRatio(true);
          setCenter(imageView);

          scaleImageView(image, screen);

          // add an image on top 
          FontIcon fontIcon = new FontIcon();
          fontIcon.setIconSize(48);
          fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
          fontIcon.setIconLiteral("bi-play");
          this.playBtn = new Button();
          playBtn.setGraphic(fontIcon);
          playBtn.setVisible(false);
          getChildren().add(playBtn);
          playBtn.managedProperty().bind(playBtn.visibleProperty());

          this.setOnMouseEntered(me -> {
            playBtn.setVisible(true);
          });
          this.setOnMouseExited(me -> {
            playBtn.setVisible(false);
          });

          playBtn.setOnMouseReleased(me -> {
            // in video mode, switch back to the imageView
            if (modeVideo) {
              mediaView.getMediaPlayer().stop();
              setCenter(imageView);            
              fontIcon.setIconLiteral("bi-play");
              modeVideo = false;
            }
            // in audio mode, when mediaPLayer has already been created, just switch to it
            else if (mediaPlayer != null) {
              setCenter(mediaView);
              mediaView.getMediaPlayer().play();
              fontIcon.setIconLiteral("bi-stop");
              modeVideo = true;
            }
            else {
              getChildren().remove(playBtn);
              fontIcon.setIconLiteral("bi-stop");
              renderVideo(url, screen);
              modeVideo = true;
            }
          });
        });
    }
    else {
      renderVideo(url, screen);
    }
  }

  public void renderVideo(@NonNull String url, @Nullable VPinScreen screen) {
    setLoading();

    Media media = new Media(url);
    LOG.info("Streaming media: " + url);
    mediaPlayer = new MediaPlayer(media);

    mediaPlayer.setOnError(() -> {
      LOG.warn("Media player error: " + mediaPlayer.getError() + ", URL: " + mediaPlayer.getMedia().getSource());
      disposeMedia();
      setCenter(getErrorLabel());
    });

    mediaPlayer.setOnReady(() -> {
      installMediaView(screen, media);
    });
  }

  private void installMediaView(VPinScreen screen, Media media) {
    for (MediaPlayerListener listener : this.listeners) {
      listener.onReady(media);
    }

    mediaPlayer.setAutoPlay(true);
    mediaPlayer.setCycleCount(-1);
    mediaPlayer.setMute(true);

    mediaView = new MediaView(mediaPlayer);
    mediaView.setPreserveRatio(true);

    scaleMediaView(media, screen);

    setCenter(mediaView);
    if (playBtn != null) {
      getChildren().add(playBtn);
    }
  }

  private void scaleMediaView(Media media, @Nullable VPinScreen screen) {
    if (mediaOptions == null || mediaOptions.isAutoRotate()) {
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
  }

  private void scaleImageView(Image image,  @Nullable VPinScreen screen) {
    if (mediaOptions == null || mediaOptions.isAutoRotate()) {
      if (VPinScreen.PlayField.equals(screen)) {
        if (image.getWidth() > image.getHeight()) {
          imageView.setRotate(90 + (invertPlayfield ? 180 : 0));
          setRotated(true);
        }
      }
      else if (VPinScreen.Loading.equals(screen)) {
        if (image.getWidth() > image.getHeight()) {
          imageView.setRotate(90);
          setRotated(true);
        }
      }
    }
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();

    if (playBtn != null) {
      double width = getWidth();
      double height = getHeight();
      super.layoutInArea(playBtn, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
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
