package de.mephisto.vpin.restclient.altsound;

/**
 * Options for generating an ALT sound pack from a ROM with rom2altsound.
 */
public class Rom2AltSoundOptions {
  /**
   * replace an existing ALT sound pack of the game
   */
  private boolean overwrite;
  /**
   * switch the ROM's sound mode to ALT sound after the installation
   */
  private boolean enableAltSound = true;
  /**
   * "factory" (the game's own master volume) or "reference" (the loudest that does not clip)
   */
  private String volume = "factory";
  /**
   * commands playing the same audio share one file
   */
  private boolean mergeTwins;
  /**
   * write DC-blocked audio (10 Hz high-pass) instead of the raw emulated samples
   */
  private boolean dcBlock;
  /**
   * stop after N commands, 0 for all; mainly for testing
   */
  private int limit;

  public boolean isOverwrite() {
    return overwrite;
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  public boolean isEnableAltSound() {
    return enableAltSound;
  }

  public void setEnableAltSound(boolean enableAltSound) {
    this.enableAltSound = enableAltSound;
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public boolean isMergeTwins() {
    return mergeTwins;
  }

  public void setMergeTwins(boolean mergeTwins) {
    this.mergeTwins = mergeTwins;
  }

  public boolean isDcBlock() {
    return dcBlock;
  }

  public void setDcBlock(boolean dcBlock) {
    this.dcBlock = dcBlock;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
