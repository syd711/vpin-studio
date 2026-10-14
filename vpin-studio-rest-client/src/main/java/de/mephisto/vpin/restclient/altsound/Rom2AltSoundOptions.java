package de.mephisto.vpin.restclient.altsound;

/**
 * Options for generating an ALT sound pack from a ROM with rom2altsound.
 * The pack is always installed in the G-Sound format.
 */
public class Rom2AltSoundOptions {
  public static final String VOLUME_FACTORY = "factory";
  public static final String VOLUME_REFERENCE = "reference";

  public static final int DEFAULT_MAX_SECS = 120;
  public static final int DEFAULT_LOOP_MAX_SECS = 240;
  public static final int DEFAULT_INTRO_LOOP_SECS = 300;

  /**
   * replace an existing ALT sound pack of the game
   */
  private boolean overwrite;
  /**
   * switch the ROM's sound mode to ALT sound after the installation
   */
  private boolean enableAltSound = true;
  /**
   * {@link #VOLUME_FACTORY} (the game's own master volume) or {@link #VOLUME_REFERENCE} (the loudest that does not clip)
   */
  private String volume = VOLUME_FACTORY;
  /**
   * --max-secs: longest file of a sound that keeps playing and whose loop is not found
   */
  private int maxSecs = DEFAULT_MAX_SECS;
  /**
   * --loop-max-secs: how long a sound that keeps playing is recorded while looking for its loop, 0: no loop search
   */
  private int loopMaxSecs = DEFAULT_LOOP_MAX_SECS;
  /**
   * --intro-loop-secs: length of the file a loop with an intro of its own plays from, 0: the body alone
   */
  private int introLoopSecs = DEFAULT_INTRO_LOOP_SECS;
  /**
   * --merge-twins: commands playing the same audio share one file
   */
  private boolean mergeTwins;
  /**
   * --dc-block: write DC-blocked audio (10 Hz high-pass) instead of the raw emulated samples
   */
  private boolean dcBlock;
  /**
   * --no-factory: skip the factory settings boot
   */
  private boolean noFactory;
  /**
   * --check-ducking: DCS only, verify the music ducking after the extraction
   */
  private boolean checkDucking;
  /**
   * --no-chip-check: WPCS and System 11, skip the chips pass (faster, but no voice lines, ducking and stops)
   */
  private boolean noChipCheck;
  /**
   * --bsmt-hle: BSMT boards, use PinMAME's older BSMT2000 emulation
   */
  private boolean bsmtHle;
  /**
   * --limit: stop after N commands, 0 for all; mainly for testing
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

  public int getMaxSecs() {
    return maxSecs;
  }

  public void setMaxSecs(int maxSecs) {
    this.maxSecs = maxSecs;
  }

  public int getLoopMaxSecs() {
    return loopMaxSecs;
  }

  public void setLoopMaxSecs(int loopMaxSecs) {
    this.loopMaxSecs = loopMaxSecs;
  }

  public int getIntroLoopSecs() {
    return introLoopSecs;
  }

  public void setIntroLoopSecs(int introLoopSecs) {
    this.introLoopSecs = introLoopSecs;
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

  public boolean isNoFactory() {
    return noFactory;
  }

  public void setNoFactory(boolean noFactory) {
    this.noFactory = noFactory;
  }

  public boolean isCheckDucking() {
    return checkDucking;
  }

  public void setCheckDucking(boolean checkDucking) {
    this.checkDucking = checkDucking;
  }

  public boolean isNoChipCheck() {
    return noChipCheck;
  }

  public void setNoChipCheck(boolean noChipCheck) {
    this.noChipCheck = noChipCheck;
  }

  public boolean isBsmtHle() {
    return bsmtHle;
  }

  public void setBsmtHle(boolean bsmtHle) {
    this.bsmtHle = bsmtHle;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
