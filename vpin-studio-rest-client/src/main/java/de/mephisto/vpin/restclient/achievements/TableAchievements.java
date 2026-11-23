package de.mephisto.vpin.restclient.achievements;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * Achievement progress of a player for one table (games with the same ROM share the progress).
 */
public class TableAchievements {
  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  public static final String STATUS_COMPLETED = "COMPLETED";
  public static final String STATUS_NOT_STARTED = "NOT_STARTED";
  public static final String STATUS_NO_MAP = "NO_MAP";

  public int gameId;
  public String gameName;
  public String rom;
  public String mapRom;
  public String vpsTableId;
  public String vpsVersionId;
  public boolean hasMap;
  public int unlocked;
  public int total;
  public Date lastPlayed;
  public String status;

  @JsonIgnore
  public double getProgressPct() {
    return total <= 0 ? 0 : unlocked * 100d / total;
  }
}
