package de.mephisto.vpin.server.highscores;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;
import jakarta.persistence.GenerationType;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "HighscoreVersions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HighscoreVersion {

  @Column(nullable = false)
  private Instant createdAt;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
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

    return Objects.equals(id, version.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Highscore Version " + this.id + " for game " + this.gameId;
  }
}
