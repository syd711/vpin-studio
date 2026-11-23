package de.mephisto.vpin.restclient.achievements;

import java.util.Date;

/**
 * A tracked game of a player.
 */
public class AchievementSessionInfo {
  public long id;
  public int gameId;
  public String rom;
  public String tableName;
  public Date startedAt;
  public Date endedAt;
  public long durationSec;
  /** number of players of the pinball game, null if the map does not know it */
  public Integer playerCount;
  /** achievements the player unlocked in this game */
  public int unlocks;
  /** final score of player 1 read from the NVRAM, null if unknown */
  public Long score;
}
