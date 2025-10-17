package de.mephisto.vpin.restclient.emulators;

public class EmulatorValidation {
  private String errorTitle;
  private String errorText;
  private GameEmulatorRepresentation gameEmulator;
  private int gameCount;

  public int getGameCount() {
    return gameCount;
  }

  public void setGameCount(int gameCount) {
    this.gameCount = gameCount;
  }

  public String getErrorText() {
    return errorText;
  }

  public void setErrorText(String errorText) {
    this.errorText = errorText;
  }

  public String getErrorTitle() {
    return errorTitle;
  }

  public void setErrorTitle(String errorTitle) {
    this.errorTitle = errorTitle;
  }

  public GameEmulatorRepresentation getGameEmulator() {
    return gameEmulator;
  }

  public void setGameEmulator(GameEmulatorRepresentation gameEmulator) {
    this.gameEmulator = gameEmulator;
  }
}
