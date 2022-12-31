package de.mephisto.vpin.commons;

public class POV {

  public static String SSAA = "ssaa";
  public static String POSTPROCAA = "postprocAA";
  public static String INGAMEAO = "ingameAO";
  public static String SCSPREFLECT = "scSpReflect";
  public static String FPSLIMITER = "fpsLimiter";
  public static String OVERWRITEDETAILSLEVEL = "overwriteDetailsLevel";
  public static String DETAILSLEVEL = "detailsLevel";
  public static String BALLREFLECTION = "ballReflection";
  public static String BALLTRAIL = "ballTrail2";
  public static String BALLTRAILSTRENGTH = "ballTrailStrength";
  public static String OVERWRITENIGHTDAY = "overwriteNightDay";
  public static String NIGHTDAYLEVEL = "nightDayLevel";
  public static String GAMEPLAYDIFFICULTY = "gameplayDifficulty";
  public static String PHYSICSSET = "physicsSet";
  public static String INCLUDEFLIPPERPHYSICS = "includeFlipperPhysics";
  public static String SOUNDVOLUME = "soundVolume";
  public static String MUSICVOLUME = "musicVolume";

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
