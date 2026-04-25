package de.mephisto.vpin.server.highscores;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.games.Game;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "Highscores")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Highscore {

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  private OffsetDateTime lastModified;

  private OffsetDateTime lastScanned;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private int gameId;

  private String displayName;

  private String raw;

  private String filename;

  private String status;

  private String type;

  @Column(length = 1024)
  private String options;

  //--------------------

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

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public OffsetDateTime getLastScanned() {
    return lastScanned;
  }

  public void setLastScanned(OffsetDateTime lastScanned) {
    this.lastScanned = lastScanned;
  }

  public OffsetDateTime getLastModified() {
    return lastModified;
  }

  public void setLastModified(OffsetDateTime lastModified) {
    this.lastModified = lastModified;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public static Highscore forGame(@NonNull Game game, @Nullable HighscoreMetadata metadata) {
    Highscore highscore = new Highscore();
    highscore.setGameId(game.getId());
    highscore.setCreatedAt(OffsetDateTime.now());
    highscore.setDisplayName(game.getGameDisplayName());

    if (metadata != null) {
      highscore.setRaw(metadata.getRaw());
      highscore.setFilename(metadata.getFilename());
      highscore.setType(metadata.getType() != null ? metadata.getType().name() : null);
      highscore.setStatus(metadata.getStatus());
      highscore.setLastScanned(metadata.getScanned());
      highscore.setLastModified(metadata.getModified());
    }
    return highscore;
  }

  public HighscoreVersion toVersion(int changedPosition, String newRaw) {
    HighscoreVersion version = new HighscoreVersion();
    version.setChangedPosition(changedPosition);
    version.setCreatedAt(OffsetDateTime.now());
    version.setOldRaw(this.getRaw());
    version.setDisplayName(this.getDisplayName());
    version.setGameId(this.getGameId());
    version.setNewRaw(newRaw);
    return version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Highscore highscore = (Highscore) o;
    return gameId == highscore.gameId && Objects.equals(id, highscore.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, gameId);
  }

  @Override
  public String toString() {
    return "Highscore [gameId " + this.getGameId() + "]";
  }
}
