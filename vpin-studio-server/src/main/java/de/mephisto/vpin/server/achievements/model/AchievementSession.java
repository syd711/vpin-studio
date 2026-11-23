package de.mephisto.vpin.server.achievements.model;

import de.mephisto.vpin.server.util.IncrementGenerated;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * A tracked game. One session per game; all tracked players are credited ({@code playerUnlocks} is a JSON map
 * playerId -> number of unlocks of that player in this game).
 */
@Entity
@Table(name = "AchievementSessions")
public class AchievementSession {

  @Id
  @IncrementGenerated
  private Long id;

  private int gameId;

  private String rom;

  @Column(nullable = false)
  private Instant startedAt;

  private Instant endedAt;

  private long durationSec;

  private Integer playerCount;

  @Column(columnDefinition = "TEXT")
  private String startAudits;

  @Column(columnDefinition = "TEXT")
  private String endAudits;

  @Column(columnDefinition = "TEXT")
  private String deltas;

  @Column(columnDefinition = "TEXT")
  private String playerUnlocks;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Instant endedAt) {
    this.endedAt = endedAt;
  }

  public long getDurationSec() {
    return durationSec;
  }

  public void setDurationSec(long durationSec) {
    this.durationSec = durationSec;
  }

  public Integer getPlayerCount() {
    return playerCount;
  }

  public void setPlayerCount(Integer playerCount) {
    this.playerCount = playerCount;
  }

  public String getStartAudits() {
    return startAudits;
  }

  public void setStartAudits(String startAudits) {
    this.startAudits = startAudits;
  }

  public String getEndAudits() {
    return endAudits;
  }

  public void setEndAudits(String endAudits) {
    this.endAudits = endAudits;
  }

  public String getDeltas() {
    return deltas;
  }

  public void setDeltas(String deltas) {
    this.deltas = deltas;
  }

  public String getPlayerUnlocks() {
    return playerUnlocks;
  }

  public void setPlayerUnlocks(String playerUnlocks) {
    this.playerUnlocks = playerUnlocks;
  }
}
