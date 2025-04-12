package de.mephisto.vpin.restclient.converter;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class MediaOperation {
  private int gameId;
  private VPinScreen screen;
  private MediaConversionCommand command;
  private String filename;

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
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
