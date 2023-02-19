package de.mephisto.vpin.restclient;

public class VpaPackageInfo {
  private boolean directb2s;
  private boolean pupPack;
  private boolean rom;
  private boolean popperMedia;
  private boolean pov;
  private boolean music;
  private boolean altSound;
  private boolean highscore;
  private int highscoreHistoryRecords;
  private boolean flexDMD;
  private boolean ultraDMD;
  private boolean vpx;

  public boolean isVpx() {
    return vpx;
  }

  public void setVpx(boolean vpx) {
    this.vpx = vpx;
  }

  public boolean isDirectb2s() {
    return directb2s;
  }

  public void setDirectb2s(boolean directb2s) {
    this.directb2s = directb2s;
  }

  public boolean isPupPack() {
    return pupPack;
  }

  public void setPupPack(boolean pupPack) {
    this.pupPack = pupPack;
  }

  public boolean isRom() {
    return rom;
  }

  public void setRom(boolean rom) {
    this.rom = rom;
  }

  public boolean isPopperMedia() {
    return popperMedia;
  }

  public void setPopperMedia(boolean popperMedia) {
    this.popperMedia = popperMedia;
  }

  public boolean isPov() {
    return pov;
  }

  public void setPov(boolean pov) {
    this.pov = pov;
  }

  public boolean isMusic() {
    return music;
  }

  public void setMusic(boolean music) {
    this.music = music;
  }

  public boolean isAltSound() {
    return altSound;
  }

  public void setAltSound(boolean altSound) {
    this.altSound = altSound;
  }

  public boolean isHighscore() {
    return highscore;
  }

  public void setHighscore(boolean highscore) {
    this.highscore = highscore;
  }

  public int getHighscoreHistoryRecords() {
    return highscoreHistoryRecords;
  }

  public void setHighscoreHistoryRecords(int highscoreHistoryRecords) {
    this.highscoreHistoryRecords = highscoreHistoryRecords;
  }

  public boolean isFlexDMD() {
    return flexDMD;
  }

  public void setFlexDMD(boolean flexDMD) {
    this.flexDMD = flexDMD;
  }

  public boolean isUltraDMD() {
    return ultraDMD;
  }

  public void setUltraDMD(boolean ultraDMD) {
    this.ultraDMD = ultraDMD;
  }
}
