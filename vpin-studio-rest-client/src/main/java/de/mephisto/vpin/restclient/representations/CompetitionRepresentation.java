package de.mephisto.vpin.restclient.representations;

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

  private Date startDate;

  private Date endDate;

  private String owner;

  private long discordChannelId;

  private String winnerInitials;

  private String uuid;

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

    Calendar c = Calendar.getInstance();
    c.setTime(getEndDate());
    c.add(Calendar.DATE, diff);
    Date newEndDate = c.getTime();

    clone.setStartDate(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    clone.setEndDate(newEndDate);

    clone.setName(this.getName() + "(1)");
    clone.setBadge(this.getBadge());
    clone.setType(this.getType());
    clone.setOwner(this.getOwner());
    clone.setDiscordChannelId(this.getDiscordChannelId());
    clone.setUuid(UUID.randomUUID().toString());
    clone.setGameId(this.getGameId());
    return clone;
  }

  public boolean isActive() {
    LocalDate end = getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return LocalDate.now().isBefore(end);
  }

  public int remainingDays() {
    LocalDate start = LocalDate.now();
    LocalDate end = getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return (int) Math.abs(ChronoUnit.DAYS.between(end, start));
  }
}
