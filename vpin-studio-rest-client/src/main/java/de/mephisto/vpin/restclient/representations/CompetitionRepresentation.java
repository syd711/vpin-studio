package de.mephisto.vpin.restclient.representations;

import de.mephisto.vpin.restclient.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CompetitionRepresentation {
  private Long id;

  private String name;

  private int gameId;

  private String badge;

  private String type;

  private Date createdAt;

  private Date startDate;

  private Date endDate;

  private String owner;

  private long discordChannelId;

  private long discordServerId;

  private String winnerInitials;

  private String uuid;

  private String score;

  private boolean started;

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

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompetitionRepresentation that = (CompetitionRepresentation) o;

    return id != null && that.id != null && id.equals(that.id);
  }

  public CompetitionRepresentation cloneCompetition() {
    CompetitionRepresentation clone = new CompetitionRepresentation();

    LocalDate start = getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate end = getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    int diff = (int) Math.abs(ChronoUnit.DAYS.between(end, start));

    Calendar startDateCal = Calendar.getInstance();
    startDateCal.setTime(getEndDate());
    Date newStartDate = startDateCal.getTime();

    Calendar endDateCal = Calendar.getInstance();
    endDateCal.setTime(newStartDate);
    endDateCal.add(Calendar.DATE, diff);
    Date newEndDate = endDateCal.getTime();

    clone.setStartDate(newStartDate);
    clone.setEndDate(newEndDate);

    clone.setName(this.getName() + " (1)");
    clone.setBadge(this.getBadge());
    clone.setType(this.getType());
    clone.setOwner(this.getOwner());
    clone.setDiscordChannelId(this.getDiscordChannelId());
    clone.setUuid(UUID.randomUUID().toString());
    clone.setGameId(this.getGameId());
    return clone;
  }

  public boolean isActive() {
    if (!StringUtils.isEmpty(getWinnerInitials())) {
      return false;
    }

    long now = DateUtil.today().getTime();
    long start = getStartDate().getTime();
    long end = getEndDate().getTime();
    return start <= now && end >= now;
  }

  public boolean isPlanned() {
    Date now = DateUtil.today();
    return getStartDate().after(now);
  }

  public int remainingDays() {
    LocalDate start = LocalDate.now();
    LocalDate end = getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return (int) Math.abs(ChronoUnit.DAYS.between(end, start)) + 1;
  }

  public boolean isOverlappingWith(Date startSelection, Date endSelection) {
    boolean startOverlap = getStartDate().before(endSelection);
    boolean endOverlap = startSelection.before(this.getEndDate());
    return startOverlap && endOverlap;
  }

  public boolean isFinished() {
    return !StringUtils.isEmpty(this.getWinnerInitials());
  }

  @Override
  public String toString() {
    return this.name;
  }
}
