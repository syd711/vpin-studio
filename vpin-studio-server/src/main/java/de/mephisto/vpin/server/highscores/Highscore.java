package de.mephisto.vpin.server.highscores;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Highscores")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Highscore {

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

  private int gameId;

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

  private String displayName;

  private String raw;

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

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public static Highscore forGame(@NonNull Game game, @Nullable String rawValue) {
    Highscore highscore = new Highscore();
    highscore.setRaw(rawValue);
    highscore.setGameId(game.getId());
    highscore.setCreatedAt(new Date());
    highscore.setUpdatedAt(new Date());
    highscore.setDisplayName(game.getGameDisplayName());
    return highscore;
  }

  public HighscoreVersion toVersion() {
    HighscoreVersion version = new HighscoreVersion();
    version.setCreatedAt(this.getCreatedAt());
    version.setRaw(this.getRaw());
    version.setDisplayName(this.getDisplayName());
    version.setGameId(this.getGameId());
    return version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Highscore highscore = (Highscore) o;

    return id.equals(highscore.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return "Highscore [gameId " + this.getGameId() + "]";
  }
}
