package de.mephisto.vpin.restclient.discord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiscordCompetitionData {
  private String uuid;
  private String owner;
  private String rom;
  private long fileSize;
  private Date startDate;
  private Date endDate;
  private String name;
  private String tableName;
  private String startMessageId;
  private List<DiscordCompetitionScoreEntry> scores = new ArrayList<>();

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

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

  public List<DiscordCompetitionScoreEntry> getScores() {
    return scores;
  }

  public void setScores(List<DiscordCompetitionScoreEntry> scores) {
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
}