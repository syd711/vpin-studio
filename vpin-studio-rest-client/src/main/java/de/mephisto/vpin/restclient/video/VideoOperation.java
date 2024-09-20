package de.mephisto.vpin.restclient.video;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class VideoOperation {
  private int gameId;
  private VPinScreen screen;
  private VideoConversionCommand command;
  private String name;
  private String result;

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public VideoConversionCommand getCommand() {
    return command;
  }

  public void setCommand(VideoConversionCommand command) {
    this.command = command;
  }
}
