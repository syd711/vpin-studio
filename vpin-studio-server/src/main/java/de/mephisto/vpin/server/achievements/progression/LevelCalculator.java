package de.mephisto.vpin.server.achievements.progression;

import de.mephisto.vpin.restclient.achievements.LevelDefinition;
import de.mephisto.vpin.restclient.achievements.LevelInfo;

import java.util.List;

/**
 * Port of the Achievement Watcher's {@code compute_player_level}: 10 levels, one prestige star per 2000
 * achievements (max 5), the level count starts again after each prestige.
 */
public final class LevelCalculator {

  public static final int PRESTIGE_THRESHOLD = 2000;
  public static final int MAX_PRESTIGE = 5;

  public static final List<LevelDefinition> LEVELS = List.of(
      new LevelDefinition(1, "Rookie", 0),
      new LevelDefinition(2, "Apprentice", 10),
      new LevelDefinition(3, "Veteran", 25),
      new LevelDefinition(4, "Expert", 50),
      new LevelDefinition(5, "Master", 100),
      new LevelDefinition(6, "Grand Master", 200),
      new LevelDefinition(7, "Pinball Legend", 400),
      new LevelDefinition(8, "Pinball God", 750),
      new LevelDefinition(9, "Multiball King", 1200),
      new LevelDefinition(10, "VPX Elite", 2000)
  );

  private LevelCalculator() {
  }

  public static LevelInfo compute(int total) {
    LevelInfo info = new LevelInfo();
    info.total = total;
    int prestige = Math.min(total / PRESTIGE_THRESHOLD, MAX_PRESTIGE);
    info.prestige = prestige;
    info.prestigeDisplay = "★".repeat(prestige) + "☆".repeat(MAX_PRESTIGE - prestige);
    int effective = total - prestige * PRESTIGE_THRESHOLD;
    info.effective = effective;

    LevelDefinition current = LEVELS.get(0);
    int prevAt = 0;
    int nextAt = LEVELS.size() > 1 ? LEVELS.get(1).from : effective + 1;
    LevelDefinition next = LEVELS.size() > 1 ? LEVELS.get(1) : null;
    boolean exhausted = true;
    for (LevelDefinition lv : LEVELS) {
      if (effective >= lv.from) {
        current = lv;
        prevAt = lv.from;
      }
      else {
        nextAt = lv.from;
        next = lv;
        exhausted = false;
        break;
      }
    }
    if (exhausted) {
      nextAt = prevAt;
      next = null;
    }
    info.level = current.level;
    info.name = current.name;
    info.prevAt = prevAt;
    info.nextAt = nextAt;
    info.nextName = next != null ? next.name : null;
    info.progressPct = nextAt > prevAt ? Math.round((effective - prevAt) * 1000d / (nextAt - prevAt)) / 10d : 100d;
    info.maxLevel = current.level == LEVELS.get(LEVELS.size() - 1).level;
    info.fullyMaxed = prestige >= MAX_PRESTIGE && info.maxLevel;
    return info;
  }
}
