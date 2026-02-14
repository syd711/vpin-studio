package de.mephisto.vpin.restclient.converter;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class MediaOperation {
  private int objectId;
  private boolean playlistMode;
  private VPinScreen screen;
  private MediaConversionCommand command;
  private String filename;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public int getObjectId() {
    return objectId;
  }

  public void setObjectId(int objectId) {
    this.objectId = objectId;
  }

  public boolean isPlaylistMode() {
    return playlistMode;
  }

  public void setPlaylistMode(boolean playlistMode) {
    this.playlistMode = playlistMode;
  }

  public VPinScreen getScreen() {
    return screen;
  }

  public void setScreen(VPinScreen screen) {
    this.screen = screen;
  }

  public MediaConversionCommand getCommand() {
    return command;
  }

  public void setCommand(MediaConversionCommand command) {
    this.command = command;
  }
}
