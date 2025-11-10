package de.mephisto.vpin.restclient.competitions;

public class CompetitionScore {
  private String userId;
  private String participantId;
  private String participantName;
  private double score;
  private int rank;
  private String avatarUrl;
  private String league;
  private int platform;

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
