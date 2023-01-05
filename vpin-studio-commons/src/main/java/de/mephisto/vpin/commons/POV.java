package de.mephisto.vpin.commons;

public class POV {

  public static final String SSAA = "ssaa";
  public static final String POST_PROC_AA = "postprocAA";
  public static final String INGAME_AO = "ingameAO";
  public static final String SCSP_REFLECT = "scSpReflect";
  public static final String FPS_LIMITER = "fpsLimiter";
  public static final String OVERWRITE_DETAILS_LEVEL = "overwriteDetailsLevel";
  public static final String DETAILS_LEVEL = "detailsLevel";
  public static final String BALL_REFLECTION = "ballReflection";
  public static final String BALL_TRAIL = "ballTrail";
  public static final String BALL_TRAIL_STRENGTH = "ballTrailStrength";
  public static final String OVERWRITE_NIGHTDAY = "overwriteNightDay";
  public static final String NIGHTDAY_LEVEL = "nightDayLevel";
  public static final String GAMEPLAY_DIFFICULTY = "gameplayDifficulty";
  public static final String PHYSICS_SET = "physicsSet";
  public static final String INCLUDE_FLIPPER_PHYSICS = "includeFlipperPhysics";
  public static final String SOUND_VOLUME = "soundVolume";
  public static final String MUSIC_VOLUME = "musicVolume";
  public static final String FULLSCREEN_ROTATION = "rotationFullscreen";

  private int gameId;

  private int ssaa;
  private int postprocAA;
  private int ingameAO;
  private int scSpReflect;
  private int fpsLimiter;
  private int overwriteDetailsLevel;
  private int detailsLevel;
  private int ballReflection;
  private int ballTrail;
  private double ballTrailStrength;
  private int overwriteNightDay;
  private int nightDayLevel;
  private double gameplayDifficulty;
  private int physicsSet;
  private int includeFlipperPhysics;
  private int soundVolume;
  private int musicVolume;
  private int rotationFullscreen;

  public int getRotationFullscreen() {
    return rotationFullscreen;
  }

  public void setRotationFullscreen(int rotationFullscreen) {
    this.rotationFullscreen = rotationFullscreen;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getSsaa() {
    return ssaa;
  }

  public void setSsaa(int ssaa) {
    this.ssaa = ssaa;
  }

  public int getPostprocAA() {
    return postprocAA;
  }

  public void setPostprocAA(int postprocAA) {
    this.postprocAA = postprocAA;
  }

  public int getIngameAO() {
    return ingameAO;
  }

  public void setIngameAO(int ingameAO) {
    this.ingameAO = ingameAO;
  }

  public int getScSpReflect() {
    return scSpReflect;
  }

  public void setScSpReflect(int scSpReflect) {
    this.scSpReflect = scSpReflect;
  }

  public int getFpsLimiter() {
    return fpsLimiter;
  }

  public void setFpsLimiter(int fpsLimiter) {
    this.fpsLimiter = fpsLimiter;
  }

  public int getOverwriteDetailsLevel() {
    return overwriteDetailsLevel;
  }

  public void setOverwriteDetailsLevel(int overwriteDetailsLevel) {
    this.overwriteDetailsLevel = overwriteDetailsLevel;
  }

  public int getDetailsLevel() {
    return detailsLevel;
  }

  public void setDetailsLevel(int detailsLevel) {
    this.detailsLevel = detailsLevel;
  }

  public int getBallReflection() {
    return ballReflection;
  }

  public void setBallReflection(int ballReflection) {
    this.ballReflection = ballReflection;
  }

  public int getBallTrail() {
    return ballTrail;
  }

  public void setBallTrail(int ballTrail) {
    this.ballTrail = ballTrail;
  }

  public double getBallTrailStrength() {
    return ballTrailStrength;
  }

  public void setBallTrailStrength(double ballTrailStrength) {
    this.ballTrailStrength = ballTrailStrength;
  }

  public int getOverwriteNightDay() {
    return overwriteNightDay;
  }

  public void setOverwriteNightDay(int overwriteNightDay) {
    this.overwriteNightDay = overwriteNightDay;
  }

  public int getNightDayLevel() {
    return nightDayLevel;
  }

  public void setNightDayLevel(int nightDayLevel) {
    this.nightDayLevel = nightDayLevel;
  }

  public double getGameplayDifficulty() {
    return gameplayDifficulty;
  }

  public void setGameplayDifficulty(double gameplayDifficulty) {
    this.gameplayDifficulty = gameplayDifficulty;
  }

  public int getPhysicsSet() {
    return physicsSet;
  }

  public void setPhysicsSet(int physicsSet) {
    this.physicsSet = physicsSet;
  }

  public int getIncludeFlipperPhysics() {
    return includeFlipperPhysics;
  }

  public void setIncludeFlipperPhysics(int includeFlipperPhysics) {
    this.includeFlipperPhysics = includeFlipperPhysics;
  }

  public int getSoundVolume() {
    return soundVolume;
  }

  public void setSoundVolume(int soundVolume) {
    this.soundVolume = soundVolume;
  }

  public int getMusicVolume() {
    return musicVolume;
  }

  public void setMusicVolume(int musicVolume) {
    this.musicVolume = musicVolume;
  }
}
