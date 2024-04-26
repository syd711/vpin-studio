package de.mephisto.vpin.restclient.games;

public class FilterSettings {
  private int emulatorId = -1;
  private boolean missingAssets;
  private boolean otherIssues;
  private boolean vpsUpdates;
  private boolean noVpsMapping;
  private boolean versionUpdates;
  private boolean notPlayed;
  private boolean noHighscoreSettings;
  private boolean noHighscoreSupport;
  private boolean withBackglass;
  private boolean withPupPack;
  private boolean withAltSound;
  private boolean withAltColor;
  private boolean withPovIni;
  private boolean withNVOffset;
  private boolean withAlias;
  private int gameStatus = -1;

  public boolean isWithNVOffset() {
    return withNVOffset;
  }

  public boolean isWithAlias() {
    return withAlias;
  }

  public void setWithAlias(boolean withAlias) {
    this.withAlias = withAlias;
  }

  public void setWithNVOffset(boolean withNVOffset) {
    this.withNVOffset = withNVOffset;
  }

  public boolean isOtherIssues() {
    return otherIssues;
  }

  public void setOtherIssues(boolean otherIssues) {
    this.otherIssues = otherIssues;
  }

  public boolean isNoVpsMapping() {
    return noVpsMapping;
  }

  public void setNoVpsMapping(boolean noVpsMapping) {
    this.noVpsMapping = noVpsMapping;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public int getGameStatus() {
    return gameStatus;
  }

  public void setGameStatus(int gameStatus) {
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

  public boolean isResetted(boolean vpxMode) {
    if(vpxMode) {
      return !this.missingAssets
        && !this.otherIssues
        && !this.noHighscoreSettings
        && !this.noHighscoreSupport
        && !this.notPlayed
        && !this.noVpsMapping
        && !this.vpsUpdates
        && !this.versionUpdates
        && !this.withAltColor
        && !this.withAltSound
        && !this.withBackglass
        && !this.withPovIni
        && !this.withPupPack
        && !this.withNVOffset
        && !this.withAlias
        && this.gameStatus == -1;
    }

    return !this.missingAssets
      && !this.notPlayed
      && this.gameStatus == -1;

  }
}
