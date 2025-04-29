package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class FilterSettings extends JsonSettings {
  private int emulatorId = -1;
  private boolean missingAssets;
  private boolean otherIssues;
  private boolean vpsUpdates;
  private boolean noVpsTableMapping;
  private boolean noVpsVersionMapping;
  private boolean iScored;
  private boolean versionUpdates;
  private boolean notPlayed;
  private boolean noHighscoreSettings;
  private boolean noHighscoreSupport;
  private boolean withBackglass;
  private boolean withPupPack;
  private boolean withAltSound;
  private boolean withAltColor;
  private boolean withPov;
  private boolean withRes;
  private boolean withIni;
  private boolean withNVOffset;
  private boolean withAlias;
  private int gameStatus = -1;
  private CommentType noteType;

  public boolean isIScored() {
    return iScored;
  }

  public void setIScored(boolean iScored) {
    this.iScored = iScored;
  }

  public boolean isWithRes() {
    return withRes;
  }

  public void setWithRes(boolean withRes) {
    this.withRes = withRes;
  }

  public boolean isWithPov() {
    return withPov;
  }

  public void setWithPov(boolean withPov) {
    this.withPov = withPov;
  }

  public boolean isWithIni() {
    return withIni;
  }

  public void setWithIni(boolean withIni) {
    this.withIni = withIni;
  }

  public CommentType getNoteType() {
    return noteType;
  }

  public void setNoteType(CommentType noteType) {
    this.noteType = noteType;
  }

  public boolean isNoVpsVersionMapping() {
    return noVpsVersionMapping;
  }

  public void setNoVpsVersionMapping(boolean noVpsVersionMapping) {
    this.noVpsVersionMapping = noVpsVersionMapping;
  }

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

  public boolean isNoVpsTableMapping() {
    return noVpsTableMapping;
  }

  public void setNoVpsTableMapping(boolean noVpsTableMapping) {
    this.noVpsTableMapping = noVpsTableMapping;
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
        && !this.noVpsTableMapping
        && !this.noVpsVersionMapping
        && !this.vpsUpdates
        && !this.versionUpdates
        && !this.withAltColor
        && !this.withAltSound
        && !this.withBackglass
        && !this.withIni
        && !this.withPov
        && !this.withRes
        && !this.withPupPack
        && !this.withNVOffset
        && !this.withAlias
        && !this.iScored
        && this.noteType == null
        && this.gameStatus == -1;
    }

    return !this.missingAssets
      && !this.notPlayed
      && this.noteType == null
      && this.gameStatus == -1;

  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.FILTER_SETTINGS;
  }
}
