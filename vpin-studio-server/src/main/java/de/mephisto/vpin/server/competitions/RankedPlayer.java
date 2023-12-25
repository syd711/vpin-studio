package de.mephisto.vpin.server.competitions;

public class RankedPlayer {
  private int first;
  private int second;
  private int third;
  private int competitionsWon;
  private String name;
  private String domain;
  private String initials;
  private int rank;
  private String avatarUuid;
  private String avatarUrl;
  private int points;

  public void addBy(int id) {
    if(id == 0) {
      first++;
    }
    else if(id == 1) {
      second++;
    }
    else if(id == 2) {
      third++;
    }
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

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

  public String getInitials() {
    return initials;
  }

  public void setInitials(String initials) {
    this.initials = initials;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
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
}
