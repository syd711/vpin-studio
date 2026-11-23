package de.mephisto.vpin.server.achievements.model;

import de.mephisto.vpin.server.util.IncrementGenerated;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * An unlocked achievement of a cabinet player. The title is the identity (Achievement Watcher compatible),
 * {@code achKey} = sha1(title)[:12] is the key used in VPin Mania and for rarity.
 */
@Entity
@Table(name = "AchievementUnlocks", uniqueConstraints = @UniqueConstraint(columnNames = {"playerId", "title"}))
public class AchievementUnlock {
  public static final String SOURCE_STUDIO = "studio";
  public static final String SOURCE_FIREBASE = "firebase";
  public static final String SOURCE_LOCAL_IMPORT = "local-import";
  public static final String SOURCE_MANIA = "mania";

  @Id
  @IncrementGenerated
  private Long id;

  @Column(nullable = false)
  private long playerId;

  private String rom;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, length = 12)
  private String achKey;

  /**
   * "global" or "rom"
   */
  @Column(nullable = false)
  private String scope;

  private Integer gameId;

  private String vpsTableId;

  private String vpsVersionId;

  @Column(nullable = false)
  private Instant unlockedAt;

  private Long sessionId;

  @Column(nullable = false)
  private String source = SOURCE_STUDIO;

  private Instant syncedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getPlayerId() {
    return playerId;
  }

  public void setPlayerId(long playerId) {
    this.playerId = playerId;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAchKey() {
    return achKey;
  }

  public void setAchKey(String achKey) {
    this.achKey = achKey;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public Integer getGameId() {
    return gameId;
  }

  public void setGameId(Integer gameId) {
    this.gameId = gameId;
  }

  public String getVpsTableId() {
    return vpsTableId;
  }

  public void setVpsTableId(String vpsTableId) {
    this.vpsTableId = vpsTableId;
  }

  public String getVpsVersionId() {
    return vpsVersionId;
  }

  public void setVpsVersionId(String vpsVersionId) {
    this.vpsVersionId = vpsVersionId;
  }

  public Instant getUnlockedAt() {
    return unlockedAt;
  }

  public void setUnlockedAt(Instant unlockedAt) {
    this.unlockedAt = unlockedAt;
  }

  public Long getSessionId() {
    return sessionId;
  }

  public void setSessionId(Long sessionId) {
    this.sessionId = sessionId;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public Instant getSyncedAt() {
    return syncedAt;
  }

  public void setSyncedAt(Instant syncedAt) {
    this.syncedAt = syncedAt;
  }

  public boolean isUploadable() {
    return !SOURCE_LOCAL_IMPORT.equals(source);
  }
}
