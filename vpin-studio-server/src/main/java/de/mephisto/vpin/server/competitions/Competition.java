package de.mephisto.vpin.server.competitions;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.players.Player;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

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

  private String winnerInitials;

  private int gameId;

  private String type;

  private String badge;

  private boolean customizeMedia;

  private boolean discordNotifications;

  private Date startDate;

  private Date endDate;

  private String name;

  public String getWinnerInitials() {
    return winnerInitials;
  }

  public void setWinnerInitials(String winnerInitials) {
    this.winnerInitials = winnerInitials;
  }

  public String getBadge() {
    return badge;
  }

  public void setBadge(String badge) {
    this.badge = badge;
  }

  public boolean isCustomizeMedia() {
    return customizeMedia;
  }

  public void setCustomizeMedia(boolean customizeMedia) {
    this.customizeMedia = customizeMedia;
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

  public boolean isDiscordNotifications() {
    return discordNotifications;
  }

  public void setDiscordNotifications(boolean discordNotifications) {
    this.discordNotifications = discordNotifications;
  }

  public boolean isActive() {
    return this.getEndDate().after(new Date()) && this.startDate.before(new Date());
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
    return "Competition '" + this.getName() + "'";
  }
}
