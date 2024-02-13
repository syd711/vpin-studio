package de.mephisto.vpin.restclient.games;

public class FilterSettings {
  private boolean missingAssets;
  private boolean vpsUpdates;
  private boolean versionUpdates;
  private boolean notPlayed;
  private boolean noHighscoreSettings;
  private boolean noHighscoreSupport;
  private boolean withBackglass;
  private boolean withPupPack;
  private boolean withAltSound;
  private boolean withAltColor;
  private boolean withPovIni;
  private GameStatus gameStatus;

  public GameStatus getGameStatus() {
    return gameStatus;
  }

  public void setGameStatus(GameStatus gameStatus) {
    this.gameStatus = gameStatus;
  }

  public boolean isNoHighscoreSettings() {
    return noHighscoreSettings;
  }

  public void setNoHighscoreSettings(boolean noHighscoreSettings) {
    this.noHighscoreSettings = noHighscoreSettings;
  }

  public boolean isNoHighscoreSupport() {
    return noHighscoreSupport;
  }

  public void setNoHighscoreSupport(boolean noHighscoreSupport) {
    this.noHighscoreSupport = noHighscoreSupport;
  }

  public boolean isWithBackglass() {
    return withBackglass;
  }

  public void setWithBackglass(boolean withBackglass) {
    this.withBackglass = withBackglass;
  }

  public boolean isWithPupPack() {
    return withPupPack;
  }

  public void setWithPupPack(boolean withPupPack) {
    this.withPupPack = withPupPack;
  }

  public boolean isWithAltSound() {
    return withAltSound;
  }

  public void setWithAltSound(boolean withAltSound) {
    this.withAltSound = withAltSound;
  }

  public boolean isWithAltColor() {
    return withAltColor;
  }

  public void setWithAltColor(boolean withAltColor) {
    this.withAltColor = withAltColor;
  }

  public boolean isWithPovIni() {
    return withPovIni;
  }

  public void setWithPovIni(boolean withPovIni) {
    this.withPovIni = withPovIni;
  }

  public boolean isNotPlayed() {
    return notPlayed;
  }

  public void setNotPlayed(boolean notPlayed) {
    this.notPlayed = notPlayed;
  }

  public boolean isMissingAssets() {
    return missingAssets;
  }

  public void setMissingAssets(boolean missingAssets) {
    this.missingAssets = missingAssets;
  }

  public boolean isVpsUpdates() {
    return vpsUpdates;
  }

  public void setVpsUpdates(boolean vpsUpdates) {
    this.vpsUpdates = vpsUpdates;
  }

  public boolean isVersionUpdates() {
    return versionUpdates;
  }

  public void setVersionUpdates(boolean versionUpdates) {
    this.versionUpdates = versionUpdates;
  }
}
