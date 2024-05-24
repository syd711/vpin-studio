package de.mephisto.vpin.server.tournaments;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TournamentTables")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentTableInfo {

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  @CreatedDate
  private Date createdAt;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "highscoreReset", nullable = false, columnDefinition = "boolean default true")
  private boolean highscoreReset;

  private String badge;

  private int gameId;

  private String vpsTableId;

  private String vpsTableVersionId;

  private Date startDate;

  private Date endDate;

  private long tournamentId;

  private boolean started;

  public boolean isStarted() {
    return started;
  }

  public void setStarted(boolean started) {
    this.started = started;
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

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isHighscoreReset() {
    return highscoreReset;
  }

  public void setHighscoreReset(boolean highscoreReset) {
    this.highscoreReset = highscoreReset;
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

  public long getTournamentId() {
    return tournamentId;
  }

  public void setTournamentId(long tournamentId) {
    this.tournamentId = tournamentId;
  }

  public boolean isActive() {
    long now = new Date().getTime();
    long start = getStartDate().getTime();
    long end = getEndDate().getTime();
    return start <= now && end >= now;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TournamentTableInfo that = (TournamentTableInfo) o;

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "TournamentInfo for '" + getTournamentId() + "'/" + getVpsTableId() + "/" + getVpsTableVersionId();
  }
}
