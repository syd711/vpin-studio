package de.mephisto.vpin.restclient.representations;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class CompetitionRepresentation {
  private Long id;

  private String name;

  private int gameId;

  private String badge;

  private String type;

  private Date startDate;

  private Date endDate;

  private boolean customizeMedia;

  private boolean discordNotifications;

  private String winnerInitials;

  private PlayerRepresentation winner;

  public String getWinnerInitials() {
    return winnerInitials;
  }

  public void setWinnerInitials(String winnerInitials) {
    this.winnerInitials = winnerInitials;
  }

  public PlayerRepresentation getWinner() {
    return winner;
  }

  public void setWinner(PlayerRepresentation winner) {
    this.winner = winner;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isCustomizeMedia() {
    return customizeMedia;
  }

  public void setCustomizeMedia(boolean customizeMedia) {
    this.customizeMedia = customizeMedia;
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

  public boolean isDiscordNotifications() {
    return discordNotifications;
  }

  public void setDiscordNotifications(boolean discordNotifications) {
    this.discordNotifications = discordNotifications;
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

    LocalDate start = LocalDate.now();
    LocalDate end = getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    long diff = Math.abs(ChronoUnit.DAYS.between(end, start));

    LocalDate newEndDate = ChronoUnit.DAYS.addTo(end, diff);
    clone.setStartDate(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    clone.setEndDate(Date.from(newEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

    clone.setName(this.getName() + "(1)");
    clone.setBadge(this.getBadge());
    clone.setType(this.getType());
    clone.setCustomizeMedia(this.isCustomizeMedia());
    clone.setGameId(this.getGameId());
    return clone;
  }

  public boolean isActive() {
    LocalDate end = getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return LocalDate.now().isBefore(end);
  }
}
