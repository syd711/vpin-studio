package de.mephisto.vpin.connectors.iscored;

import java.time.Instant;

public class Score {
  private Instant date;
  private String game;
  private int losses;
  private String name;
  private long score;
  private int wins;

  public Instant getDate() {
    return date;
  }

  public void setDate(Instant date) {
    this.date = date;
  }

  public String getGame() {
    return game;
  }

  public void setGame(String game) {
    this.game = game;
  }

  public int getLosses() {
    return losses;
  }

  public void setLosses(int losses) {
    this.losses = losses;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getScore() {
    return score;
  }

  public void setScore(long score) {
    this.score = score;
  }

  public int getWins() {
    return wins;
  }

  public void setWins(int wins) {
    this.wins = wins;
  }

  @Override
  public String toString() {
    return "iScored score " + this.getName() + " - " + this.score + " / " + this.game;
  }
}
