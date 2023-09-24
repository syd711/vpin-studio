package de.mephisto.vpin.restclient.players;

public class RankedPlayerRepresentation {
  private int first;
  private int second;
  private int third;
  private int competitionsWon;
  private String name;
  private int rank;
  private String avatarUuid;
  private String avatarUrl;
  private int points;

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public int getCompetitionsWon() {
    return competitionsWon;
  }

  public void setCompetitionsWon(int competitionsWon) {
    this.competitionsWon = competitionsWon;
  }

  public int getFirst() {
    return first;
  }

  public void setFirst(int first) {
    this.first = first;
  }

  public int getSecond() {
    return second;
  }

  public void setSecond(int second) {
    this.second = second;
  }

  public int getThird() {
    return third;
  }

  public void setThird(int third) {
    this.third = third;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  public String getAvatarUuid() {
    return avatarUuid;
  }

  public void setAvatarUuid(String avatarUuid) {
    this.avatarUuid = avatarUuid;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
}
