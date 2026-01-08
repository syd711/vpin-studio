package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.net.URL;

public class FrontendScreenAsset {
  private Stage screenStage;
  private int rotation;
  private FrontendPlayerDisplay display;
  private String mimeType;
  private URL url;
  private int duration;
  private String name;

  private MediaPlayer mediaPlayer;

  public void dispose() {
    if(mediaPlayer != null) {
      new Thread(() -> {
        mediaPlayer.dispose();
      }).start();
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
  }

  public void setMediaPlayer(MediaPlayer mediaPlayer) {
    this.mediaPlayer = mediaPlayer;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getRotation() {
    return rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  public Stage getScreenStage() {
    return screenStage;
  }

  public void setScreenStage(Stage screenStage) {
    this.screenStage = screenStage;
  }

  public FrontendPlayerDisplay getDisplay() {
    return display;
  }

  public void setDisplay(FrontendPlayerDisplay display) {
    this.display = display;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }
}
