package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import de.mephisto.vpin.restclient.achievements.LevelInfo;
import de.mephisto.vpin.server.achievements.progression.Badge;
import de.mephisto.vpin.server.achievements.progression.BadgeEvaluator;
import de.mephisto.vpin.server.achievements.progression.BadgeStats;
import de.mephisto.vpin.server.achievements.progression.LevelCalculator;
import de.mephisto.vpin.server.achievements.progression.RarityTier;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgressionTest {

  @Test
  void levelsMatchWatcher() throws Exception {
    List<Map<String, Object>> expected = new ObjectMapper().readValue(new File(RomRuleGeneratorGoldenTest.GOLDEN, "levels.json"), new TypeReference<>() {
    });
    for (Map<String, Object> e : expected) {
      int total = ((Number) e.get("total")).intValue();
      if (total >= LevelCalculator.PRESTIGE_THRESHOLD) {
        continue; // the AW restarts the level count after a prestige, we keep VPX Elite (see levelsDoNotCycle)
      }
      LevelInfo lv = LevelCalculator.compute(total);
      assertThat(lv.level).as("level for " + total).isEqualTo(((Number) e.get("level")).intValue());
      assertThat(lv.name).isEqualTo(e.get("label"));
      assertThat(lv.prestige).isEqualTo(((Number) e.get("prestige")).intValue());
      assertThat(lv.prestigeDisplay).isEqualTo(e.get("prestige_display"));
      assertThat(lv.nextAt).as("nextAt for " + total).isEqualTo(((Number) e.get("next_at")).intValue());
      assertThat(lv.prevAt).isEqualTo(((Number) e.get("prev_at")).intValue());
      assertThat(lv.effective).isEqualTo(((Number) e.get("effective")).intValue());
      assertThat(lv.maxLevel).isEqualTo(e.get("max_level"));
      assertThat(lv.fullyMaxed).isEqualTo(e.get("fully_maxed"));
      assertThat(lv.progressPct).as("progress for " + total).isEqualTo(((Number) e.get("progress_pct")).doubleValue());
    }
  }

  @Test
  void levelsDoNotCycle() {
    LevelInfo elite = LevelCalculator.compute(2000);
    assertThat(elite.level).isEqualTo(10);
    assertThat(elite.name).isEqualTo("VPX Elite");
    assertThat(elite.prestige).isEqualTo(1);
    assertThat(elite.prestigeDisplay).isEqualTo("★☆☆☆☆");
    assertThat(elite.effective).isEqualTo(2000);
    assertThat(elite.maxLevel).isTrue();
    assertThat(elite.fullyMaxed).isFalse();
    assertThat(elite.progressPct).isEqualTo(100d);
    assertThat(elite.nextName).isNull();

    LevelInfo mid = LevelCalculator.compute(4500);
    assertThat(mid.level).isEqualTo(10);
    assertThat(mid.prestige).isEqualTo(2);

    LevelInfo maxed = LevelCalculator.compute(10000);
    assertThat(maxed.level).isEqualTo(10);
    assertThat(maxed.prestige).isEqualTo(5);
    assertThat(maxed.fullyMaxed).isTrue();

    assertThat(LevelCalculator.compute(12500).prestige).isEqualTo(5);
  }

  @Test
  void badgesMatchWatcher() throws Exception {
    List<Map<String, String>> expected = new ObjectMapper().readValue(new File(RomRuleGeneratorGoldenTest.GOLDEN, "badges.json"), new TypeReference<>() {
    });
    assertThat(Badge.values()).hasSize(expected.size());
    for (int i = 0; i < expected.size(); i++) {
      Badge b = Badge.values()[i];
      assertThat(b.name()).isEqualTo(expected.get(i).get("id"));
      assertThat(b.getDisplayName()).isEqualTo(expected.get(i).get("name"));
      assertThat(b.getDescription()).isEqualTo(expected.get(i).get("description"));
    }
  }

  @Test
  void badgeProgress() {
    BadgeStats s = new BadgeStats();
    s.totalAchievements = 306;
    s.level = 6;
    s.totalPlaytimeSec = 38 * 3600 + 100;
    s.maxSessionSec = 41 * 60;
    s.maxSessionUnlocks = 4;
    List<BadgeInfo> badges = BadgeEvaluator.evaluate(s, Set.of("cloud_pioneer"), "century");
    Map<String, BadgeInfo> byId = new java.util.HashMap<>();
    badges.forEach(b -> byId.put(b.id, b));
    assertThat(byId.get("century").earned).isTrue();
    assertThat(byId.get("century").selected).isTrue();
    assertThat(byId.get("hoarder").progressText).isEqualTo("306 / 500");
    assertThat(byId.get("thousandaire").progressText).isEqualTo("306 / 1,000");
    assertThat(byId.get("marathon").progressText).isEqualTo("38 / 50 h");
    assertThat(byId.get("dedicated").earned).isTrue();
    assertThat(byId.get("long_session").progressText).isEqualTo("best: 41 min");
    assertThat(byId.get("hot_streak").progressText).isEqualTo("best: 4");
    assertThat(byId.get("level_10").progressText).isEqualTo("Level 6 / 10");
    assertThat(byId.get("cloud_pioneer").earned).isTrue();
  }

  @Test
  void rarity() {
    assertThat(RarityTier.of(60, 100)).isEqualTo(RarityTier.Common);
    assertThat(RarityTier.of(30, 100)).isEqualTo(RarityTier.Uncommon);
    assertThat(RarityTier.of(10, 100)).isEqualTo(RarityTier.Rare);
    assertThat(RarityTier.of(5, 100)).isEqualTo(RarityTier.Epic);
    assertThat(RarityTier.of(4, 100)).isEqualTo(RarityTier.Legendary);
    assertThat(RarityTier.of(1, 0)).isNull();
  }
}
