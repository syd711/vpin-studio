package de.mephisto.vpin.connectors.wovp.models;

public class ScoreSubmit {
  private String challengeId;
  private String photoTempId;
  private long score;
  private ScoreSubmitMetadata metadata = new ScoreSubmitMetadata();
  private int playingPlatform = 0;

  public ScoreSubmitMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(ScoreSubmitMetadata metadata) {
    this.metadata = metadata;
  }

  public String getChallengeId() {
    return challengeId;
  }

  public void setChallengeId(String challengeId) {
    this.challengeId = challengeId;
  }

  public String getPhotoTempId() {
    return photoTempId;
  }

  public void setPhotoTempId(String photoTempId) {
    this.photoTempId = photoTempId;
  }

  public long getScore() {
    return score;
  }

  public void setScore(long score) {
    this.score = score;
  }

  public int getPlayingPlatform() {
    return playingPlatform;
  }

  public void setPlayingPlatform(int playingPlatform) {
    this.playingPlatform = playingPlatform;
  }
}
