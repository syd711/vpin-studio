package de.mephisto.vpin.server.achievements.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Achievement state of a cabinet player. JSON columns: badges (list of badge ids), globalTally (title -> tally),
 * romsPlayed (list), fieldBest (rom -> field -> best delta in one game).
 */
@Entity
@Table(name = "AchievementPlayerStates")
public class AchievementPlayerState {

  @Id
  private Long playerId;

  private Instant trackingSince;

  @Column(columnDefinition = "TEXT")
  private String badges;

  private String selectedBadge;

  @Column(columnDefinition = "TEXT")
  private String globalTally;

  @Column(columnDefinition = "TEXT")
  private String romsPlayed;

  @Column(columnDefinition = "TEXT")
  private String fieldBest;

  private boolean cloudUploadDone;

  private String migratedFirebasePid;

  private Instant migratedAt;

  private Instant lastSync;

  /**
   * true if summary/badge changes still have to be uploaded to VPin Mania
   */
  private boolean summaryDirty;

  public Long getPlayerId() {
    return playerId;
  }

  public void setPlayerId(Long playerId) {
    this.playerId = playerId;
  }

  public Instant getTrackingSince() {
    return trackingSince;
  }

  public void setTrackingSince(Instant trackingSince) {
    this.trackingSince = trackingSince;
  }

  public String getBadges() {
    return badges;
  }

  public void setBadges(String badges) {
    this.badges = badges;
  }

  public String getSelectedBadge() {
    return selectedBadge;
  }

  public void setSelectedBadge(String selectedBadge) {
    this.selectedBadge = selectedBadge;
  }

  public String getGlobalTally() {
    return globalTally;
  }

  public void setGlobalTally(String globalTally) {
    this.globalTally = globalTally;
  }

  public String getRomsPlayed() {
    return romsPlayed;
  }

  public void setRomsPlayed(String romsPlayed) {
    this.romsPlayed = romsPlayed;
  }

  public String getFieldBest() {
    return fieldBest;
  }

  public void setFieldBest(String fieldBest) {
    this.fieldBest = fieldBest;
  }

  public boolean isCloudUploadDone() {
    return cloudUploadDone;
  }

  public void setCloudUploadDone(boolean cloudUploadDone) {
    this.cloudUploadDone = cloudUploadDone;
  }

  public String getMigratedFirebasePid() {
    return migratedFirebasePid;
  }

  public void setMigratedFirebasePid(String migratedFirebasePid) {
    this.migratedFirebasePid = migratedFirebasePid;
  }

  public Instant getMigratedAt() {
    return migratedAt;
  }

  public void setMigratedAt(Instant migratedAt) {
    this.migratedAt = migratedAt;
  }

  public Instant getLastSync() {
    return lastSync;
  }

  public void setLastSync(Instant lastSync) {
    this.lastSync = lastSync;
  }

  public boolean isSummaryDirty() {
    return summaryDirty;
  }

  public void setSummaryDirty(boolean summaryDirty) {
    this.summaryDirty = summaryDirty;
  }
}
