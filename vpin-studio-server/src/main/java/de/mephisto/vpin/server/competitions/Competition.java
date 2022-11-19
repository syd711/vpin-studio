
package de.mephisto.vpin.server.competitions;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.players.Player;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

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

  @OneToMany
  private Set<Highscore> highscores;

  @OneToOne(cascade = CascadeType.ALL)
  private Asset logo;

  @OneToOne(cascade = CascadeType.ALL)
  private Player winner;

  private String winnerInitials;

  private int gameId;

  private String type;

  private String badge;

  private boolean customizeMedia;

  private Date startDate;

  private Date endDate;

  private String name;

  private boolean active;

  public Player getWinner() {
    return winner;
  }

  public void setWinner(Player winner) {
    this.winner = winner;
  }

  public String getWinnerInitials() {
    return winnerInitials;
  }

  public void setWinnerInitials(String winnerInitials) {
    this.winnerInitials = winnerInitials;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
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

  public Asset getLogo() {
    return logo;
  }

  public void setLogo(Asset logo) {
    this.logo = logo;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public Set<Highscore> getHighscores() {
    return highscores;
  }

  public void setHighscores(Set<Highscore> highscores) {
    this.highscores = highscores;
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
}
