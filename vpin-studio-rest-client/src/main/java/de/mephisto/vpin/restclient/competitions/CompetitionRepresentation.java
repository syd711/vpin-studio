package de.mephisto.vpin.restclient.competitions;

import de.mephisto.vpin.restclient.validation.ValidationState;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CompetitionRepresentation {
  private Long id;

  private String name;

  private int gameId;

  private int scoreLimit;

  private String badge;

  private String type;

  private OffsetDateTime createdAt;

  private OffsetDateTime startDate;

  private OffsetDateTime endDate;

  private String owner;

  private long discordChannelId;

  private long discordServerId;

  private String url;

  private String vpsTableId;

  private String vpsTableVersionId;

  private String winnerInitials;

  private String uuid;

  private String score;

  private String mode;

  private boolean started;

  private boolean highscoreReset;

  private String rom;

  private String issues;

  public String getIssues() {
    return issues;
  }

  public void setIssues(String issues) {
    this.issues = issues;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getVpsTableId() {
    return vpsTableId;
  }

  public void setVpsTableId(String vpsTableId) {
    this.vpsTableId = vpsTableId;
  }

  public String getVpsTableVersionId() {
    return vpsTableVersionId;
  }

  public void setVpsTableVersionId(String vpsTableVersionId) {
    this.vpsTableVersionId = vpsTableVersionId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getScoreLimit() {
    return scoreLimit;
  }

  public void setScoreLimit(int scoreLimit) {
    this.scoreLimit = scoreLimit;
  }

  private ValidationState validationState = new ValidationState();

  public ValidationState getValidationState() {
    return validationState;
  }

  public void setValidationState(ValidationState validationState) {
    this.validationState = validationState;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public boolean isHighscoreReset() {
    return highscoreReset;
  }

  public void setHighscoreReset(boolean highscoreReset) {
    this.highscoreReset = highscoreReset;
  }

  private String joinMode = JoinMode.ROM_ONLY.name();

  public String getJoinMode() {
    return joinMode;
  }

  public void setJoinMode(String joinMode) {
    this.joinMode = joinMode;
  }

  public boolean isStarted() {
    return started;
  }

  public void setStarted(boolean started) {
    this.started = started;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }

  public long getDiscordServerId() {
    return discordServerId;
  }

  public void setDiscordServerId(long discordServerId) {
    this.discordServerId = discordServerId;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
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

  public long getDiscordChannelId() {
    return discordChannelId;
  }

  public void setDiscordChannelId(long discordChannelId) {
    this.discordChannelId = discordChannelId;
  }

  public String getWinnerInitials() {
    return winnerInitials;
  }

  public void setWinnerInitials(String winnerInitials) {
    this.winnerInitials = winnerInitials;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBadge() {
    return badge;
  }

  public void setBadge(String badge) {
    this.badge = badge;
  }

  public OffsetDateTime getStartDate() {
    return startDate;
  }

  public void setStartDate(OffsetDateTime startDate) {
    this.startDate = startDate;
  }

  public OffsetDateTime getEndDate() {
    return endDate;
  }

  public void setEndDate(OffsetDateTime endDate) {
    this.endDate = endDate;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompetitionRepresentation that = (CompetitionRepresentation) o;

    return id != null && that.id != null && id.equals(that.id);
  }

  public CompetitionRepresentation cloneCompetition() {
    CompetitionRepresentation clone = new CompetitionRepresentation();

    LocalDate start = getStartDate().toLocalDate();
    LocalDate end = getEndDate().toLocalDate();
    long diff = Math.abs(ChronoUnit.DAYS.between(end, start));

    OffsetDateTime newStartDate = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS);
    OffsetDateTime newEndDate = newStartDate.plusDays(diff);

    clone.setStartDate(newStartDate);
    clone.setEndDate(newEndDate);

    clone.setName(this.getName() + " (1)");
    clone.setBadge(this.getBadge());
    clone.setType(this.getType());
    clone.setOwner(this.getOwner());
    clone.setScoreLimit(this.getScoreLimit());
    clone.setJoinMode(this.getJoinMode());
    clone.setDiscordServerId(this.getDiscordServerId());
    clone.setDiscordChannelId(this.getDiscordChannelId());
    clone.setUuid(UUID.randomUUID().toString());
    clone.setGameId(this.getGameId());
    return clone;
  }

  public boolean isValid() {
    if (getType() != null && getType().equals(CompetitionType.DISCORD.name())) {
      return discordServerId > 0 && discordChannelId > 0;
    }
    return true;
  }

  public boolean isActive() {
    if (!StringUtils.isEmpty(getWinnerInitials())) {
      return false;
    }

    if (getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      return true;
    }

    if (getType().equals(CompetitionType.ISCORED.name())) {
      return true;
    }

    if (getType().equals(CompetitionType.MANIA.name())) {
      return true;
    }

    OffsetDateTime now = OffsetDateTime.now();
    return (startDate != null && !now.isBefore(startDate)) && (endDate != null && !now.isAfter(endDate));
  }

  public boolean isPlanned() {
    if (getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      return false;
    }
    if (getType().equals(CompetitionType.ISCORED.name())) {
      return false;
    }
    return getStartDate().isAfter(OffsetDateTime.now());
  }

  public int remainingDays() {
    LocalDate start = LocalDate.now();
    LocalDate end = getEndDate().toLocalDate();
    return (int) Math.abs(ChronoUnit.DAYS.between(end, start)) + 1;
  }

  public boolean isFinished() {
    return !StringUtils.isEmpty(this.getWinnerInitials());
  }

  public boolean isOverlappingWith(OffsetDateTime startSelection, OffsetDateTime endSelection) {
    boolean startOverlap = getStartDate().isBefore(endSelection);
    boolean endOverlap = startSelection.isBefore(this.getEndDate());
    return startOverlap && endOverlap;
  }

  @Override
  public String toString() {
    return this.name;
  }

}
