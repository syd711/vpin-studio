package de.mephisto.vpin.connectors.wovp.models;

import java.util.Date;

public class Challenge {
  private String id;
  private String pinballTableId;
  private String seasonChallengeId;
  private String name;
  private String description;
  private ChallengeTypeCode challengeTypeCode;
  private Date endDateUTC;
  private Date startDateUTC;
  private PinballTable pinballTable;
  private ScoreBoard scoreBoard;

  public String getPinballTableId() {
    return pinballTableId;
  }

  public void setPinballTableId(String pinballTableId) {
    this.pinballTableId = pinballTableId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ChallengeTypeCode getChallengeTypeCode() {
    return challengeTypeCode;
  }

  public void setChallengeTypeCode(ChallengeTypeCode challengeTypeCode) {
    this.challengeTypeCode = challengeTypeCode;
  }

  public Date getEndDateUTC() {
    return endDateUTC;
  }

  public void setEndDateUTC(Date endDateUTC) {
    this.endDateUTC = endDateUTC;
  }

  public Date getStartDateUTC() {
    return startDateUTC;
  }

  public void setStartDateUTC(Date startDateUTC) {
    this.startDateUTC = startDateUTC;
  }

  public PinballTable getPinballTable() {
    return pinballTable;
  }

  public void setPinballTable(PinballTable pinballTable) {
    this.pinballTable = pinballTable;
  }

  public String getSeasonChallengeId() {
    return seasonChallengeId;
  }

  public void setSeasonChallengeId(String seasonChallengeId) {
    this.seasonChallengeId = seasonChallengeId;
  }

  public ScoreBoard getScoreBoard() {
    return scoreBoard;
  }

  public void setScoreBoard(ScoreBoard scoreBoard) {
    this.scoreBoard = scoreBoard;
  }
}
