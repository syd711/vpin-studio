package de.mephisto.vpin.server.competitions;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.validation.ValidationState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "Competitions")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Competition {

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  private Date createdAt;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  private Date updatedAt;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "highscoreReset", nullable = false, columnDefinition = "boolean default true")
  private boolean highscoreReset;

  private String uuid = UUID.randomUUID().toString();

  private String winnerInitials;

  private int gameId;

  @Column(name = "scoreLimit", nullable = false, columnDefinition = "integer default 5")
  private int scoreLimit;

  private String type;

  private String owner;

  private String badge;

  private long discordChannelId;

  private long discordServerId;

  private String url;

  private String vpsTableId;

  private String vpsTableVersionId;

  private Date startDate;

  private Date endDate;

  private boolean started;

  private String name;

  private String mode;

  private String score;

  private String joinMode;

  private String rom;

  private String issues;

  public String getIssues() {
    return issues;
  }

  public void setIssues(String issues) {
    this.issues = issues;
  }

  @Transient
  private ValidationState validationState;

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
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

  public int getScoreLimit() {
    return scoreLimit;
  }

  public void setScoreLimit(int scoreLimit) {
    this.scoreLimit = scoreLimit;
  }

  public ValidationState getValidationState() {
    return validationState;
  }

  public void setValidationState(ValidationState validationState) {
    this.validationState = validationState;
  }

  public boolean isHighscoreReset() {
    return highscoreReset;
  }

  public void setHighscoreReset(boolean highscoreReset) {
    this.highscoreReset = highscoreReset;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

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

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public long getDiscordChannelId() {
    return discordChannelId;
  }

  public void setDiscordChannelId(long discordChannelId) {
    this.discordChannelId = discordChannelId;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getWinnerInitials() {
    return winnerInitials;
  }

  public void setWinnerInitials(String winnerInitials) {
    this.winnerInitials = winnerInitials;
  }

  public String getBadge() {
    return badge;
  }

  /**
   * Set the file base name here, no image suffix required
   * @param badge
   */
  public void setBadge(String badge) {
    this.badge = badge;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
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

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public boolean isActive() {
    if (getType().equals(CompetitionType.WEEKLY.name())) {
      return true;
    }

    if (!StringUtils.isEmpty(getWinnerInitials())) {
      return false;
    }

    if (getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      return true;
    }

    if (getType().equals(CompetitionType.ISCORED.name())) {
      return true;
    }

    long now = new Date().getTime();
    long start = getStartDate().getTime();
    long end = getEndDate().getTime();
    return start <= now && end >= now;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Competition that = (Competition) o;

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return this.getType() + " '" + this.getName() + "'";
  }
}
