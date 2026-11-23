package de.mephisto.vpin.restclient.achievements;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

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
  /** final score of player 1 (score-server plugin, otherwise read from the NVRAM), null if unknown */
  public Long score;
  /** points of the best ball of player 1, null if unknown (score-server plugin only) */
  public Long bestBall;
  public Integer bestBallNo;
  /** player (1 based) -> final score, empty if unknown (score-server plugin only) */
  public Map<Integer, Long> playerScores = new TreeMap<>();
  /** one pinball game tracked with the score-server plugin */
  public boolean liveTracked;
}
