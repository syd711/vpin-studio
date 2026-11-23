package de.mephisto.vpin.restclient.achievements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A tracked game with highlights, unlocked achievements and audit changes.
 */
public class AchievementSessionDetails {
  public AchievementSessionInfo session;
  /** category (Power, Precision, Fun) -> highlights, best first */
  public Map<String, List<SessionHighlight>> highlights = new LinkedHashMap<>();
  /** achievements the player unlocked in this game */
  public List<AchievementInfo> unlocks = new ArrayList<>();
  /** audit field -> change during the game (only increased fields), largest first */
  public Map<String, Long> deltas = new LinkedHashMap<>();
}
