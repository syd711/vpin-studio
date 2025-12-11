package de.mephisto.vpin.connectors.wovp.models;

public class ScoreSubmit {
  private String challengeId;
  private String photoTempId;
  private long score;
  private String note;
  private int playingPlatform = 0;

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

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public int getPlayingPlatform() {
    return playingPlatform;
  }

  public void setPlayingPlatform(int playingPlatform) {
    this.playingPlatform = playingPlatform;
  }
}
