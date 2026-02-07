package de.mephisto.vpin.restclient.vpxz;

public class StudioVPXZDescriptor {
  private boolean preferences;
  private boolean players;
  private boolean vpsComments;
  private boolean games;
  private boolean gameComments;
  private boolean gameVpsMapping;
  private boolean gameCardSettings;
  private boolean gameVersion;

  public boolean isGames() {
    return games;
  }

  public void setGames(boolean games) {
    this.games = games;
  }

  public boolean isGameVpsMapping() {
    return gameVpsMapping;
  }

  public void setGameVpsMapping(boolean gameVpsMapping) {
    this.gameVpsMapping = gameVpsMapping;
  }

  public boolean isGameCardSettings() {
    return gameCardSettings;
  }

  public void setGameCardSettings(boolean gameCardSettings) {
    this.gameCardSettings = gameCardSettings;
  }

  public boolean isGameVersion() {
    return gameVersion;
  }

  public void setGameVersion(boolean gameVersion) {
    this.gameVersion = gameVersion;
  }

  public boolean isPreferences() {
    return preferences;
  }

  public void setPreferences(boolean preferences) {
    this.preferences = preferences;
  }

  public boolean isPlayers() {
    return players;
  }

  public void setPlayers(boolean players) {
    this.players = players;
  }

  public boolean isGameComments() {
    return gameComments;
  }

  public void setGameComments(boolean gameComments) {
    this.gameComments = gameComments;
  }

  public boolean isVpsComments() {
    return vpsComments;
  }

  public void setVpsComments(boolean vpsComments) {
    this.vpsComments = vpsComments;
  }
}
