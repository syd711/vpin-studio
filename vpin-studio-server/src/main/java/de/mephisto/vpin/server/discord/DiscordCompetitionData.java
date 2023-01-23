package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.server.highscores.Score;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiscordCompetitionData {
  private String uuid;
  private String owner;
  private String rom;
  private long fileSize;
  private Date createdAt;
  private String name;
  private String startMessageId;

  public String getStartMessageId() {
    return startMessageId;
  }

  public void setStartMessageId(String startMessageId) {
    this.startMessageId = startMessageId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  private List<ScoreEntry> scores = new ArrayList<>();

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public List<ScoreEntry> getScores() {
    return scores;
  }

  public void setScores(List<ScoreEntry> scores) {
    this.scores = scores;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }


  static class ScoreEntry {
    private int position;
    private String initials;
    private double numericScore;
    private String score;

    public ScoreEntry() {

    }

    public ScoreEntry(Score score) {
      this.position = score.getPosition();
      this.initials = score.getPlayerInitials();
      this.numericScore = score.getNumericScore();
      this.score = score.getScore();
    }

    public int getPosition() {
      return position;
    }

    public void setPosition(int position) {
      this.position = position;
    }

    public String getInitials() {
      return initials;
    }

    public void setInitials(String initials) {
      this.initials = initials;
    }

    public double getNumericScore() {
      return numericScore;
    }

    public void setNumericScore(double numericScore) {
      this.numericScore = numericScore;
    }

    public String getScore() {
      return score;
    }

    public void setScore(String score) {
      this.score = score;
    }
  }
}