package de.mephisto.vpin.restclient.competitions;

import java.util.Date;

public class CompetitionScore {
  private String userId;
  private String participantId;
  private String participantName;
  private String participantCountryCode;
  private String flagUrl;
  private double score;
  private int rank;
  private String avatarUrl;
  private String challengeImageUrl;
  private String league;
  private int platform;
  private Date creationDate;
  private String note;
  private boolean pending = false;
  private boolean myScore = false;

  public boolean isMyScore() {
    return myScore;
  }

  public void setMyScore(boolean myScore) {
    this.myScore = myScore;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public boolean isPending() {
    return pending;
  }

  public void setPending(boolean pending) {
    this.pending = pending;
  }

  public String getFlagUrl() {
    return flagUrl;
  }

  public void setFlagUrl(String flagUrl) {
    this.flagUrl = flagUrl;
  }

  public String getParticipantCountryCode() {
    return participantCountryCode;
  }

  public void setParticipantCountryCode(String participantCountryCode) {
    this.participantCountryCode = participantCountryCode;
  }

  public String getChallengeImageUrl() {
    return challengeImageUrl;
  }

  public void setChallengeImageUrl(String challengeImageUrl) {
    this.challengeImageUrl = challengeImageUrl;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public int getPlatform() {
    return platform;
  }

  public void setPlatform(int platform) {
    this.platform = platform;
  }

  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getParticipantName() {
    return participantName;
  }

  public void setParticipantName(String participantName) {
    this.participantName = participantName;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getLeague() {
    return league;
  }

  public void setLeague(String league) {
    this.league = league;
  }


}
