package de.mephisto.vpin.server.highscores;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "HighscoreVersions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HighscoreVersion {

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private int gameId;

  private int changedPosition;

  private String displayName;

  private String oldRaw;

  private String newRaw;

  //------------------------------

  public int getChangedPosition() {
    return changedPosition;
  }

  public void setChangedPosition(int changedPosition) {
    this.changedPosition = changedPosition;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNewRaw() {
    return newRaw;
  }

  public void setNewRaw(String newRaw) {
    this.newRaw = newRaw;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public String getOldRaw() {
    return oldRaw;
  }

  public void setOldRaw(String oldRaw) {
    this.oldRaw = oldRaw;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HighscoreVersion version = (HighscoreVersion) o;

    return id.equals(version.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "Highscore Version " + this.id + " for game " + this.gameId;
  }
}
