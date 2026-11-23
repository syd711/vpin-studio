package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.achievements.rules.AchievementRuleSet;
import de.mephisto.vpin.server.achievements.rules.PlayerProgress;
import de.mephisto.vpin.server.achievements.rules.RuleCondition;
import de.mephisto.vpin.server.achievements.rules.RuleEvaluator;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class RuleEvaluatorTest {

  private static List<AchievementRule> globalRules() throws Exception {
    try (InputStream in = RuleEvaluatorTest.class.getResourceAsStream("/achievements/global_achievements.json")) {
      return AchievementRuleSet.load(in).getRules();
    }
  }

  private static Map<String, Object> audits(Object... kv) {
    Map<String, Object> m = new LinkedHashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      m.put((String) kv[i], kv[i + 1]);
    }
    return m;
  }

  @Test
  void bundledGlobalRules() throws Exception {
    assertThat(globalRules()).hasSize(122).allMatch(AchievementRule::isGlobal);
  }

  @Test
  void sessionRulesAwardEveryStageReached() {
    RuleEvaluator.Input in = new RuleEvaluator.Input();
    in.rom = "afm_113b";
    in.startAudits = audits("Ramps Made", 100L, "P1 Score", 0L, "Powerups", 1L);
    in.endAudits = audits("Ramps Made", 112L, "P1 Score", 5000L, "Powerups", 4L);
    in.durationSec = 400;
    in.romRules = List.of(
        new AchievementRule("afm_113b – Ramps Made: 5", RuleCondition.nvramDelta("Ramps Made", 5), "session"),
        new AchievementRule("afm_113b – Ramps Made: 10", RuleCondition.nvramDelta("Ramps Made", 10), "session"),
        new AchievementRule("afm_113b – Ramps Made: 15", RuleCondition.nvramDelta("Ramps Made", 15), "session"),
        new AchievementRule("afm_113b – Powerups: 3", RuleCondition.nvramDelta("Powerups", 3), "session"),
        new AchievementRule("afm_113b – Play 5 Minutes", RuleCondition.sessionTime(300), "session"),
        new AchievementRule("afm_113b – Play 10 Minutes", RuleCondition.sessionTime(600), "session"));
    PlayerProgress p = new PlayerProgress();
    p.unlockedTitles.add("afm_113b – Ramps Made: 5");

    RuleEvaluator.Result r = RuleEvaluator.evaluate(in, p);
    assertThat(r.awards).extracting(RuleEvaluator.Award::title).containsExactly(
        "afm_113b – Ramps Made: 10", "afm_113b – Powerups: 3", "afm_113b – Play 5 Minutes");
    assertThat(r.retriggered).containsExactly("afm_113b – Ramps Made: 5");
    assertThat(r.sessionDeltas).containsEntry("Ramps Made", 12L).doesNotContainKey("P1 Score");
    assertThat(p.romsPlayed).containsExactly("afm_113b");
  }

  @Test
  void multiplayerSkipsSessionRulesButCountsTallies() throws Exception {
    RuleEvaluator.Input in = new RuleEvaluator.Input();
    in.rom = "tz_94h";
    in.multiplayer = true;
    in.startAudits = audits("Games Started", 10L);
    in.endAudits = audits("Games Started", 12L);
    in.globalRules = globalRules();
    in.romRules = List.of(new AchievementRule("tz_94h – Play 5 Minutes", RuleCondition.sessionTime(300), "session"));
    in.durationSec = 1000;
    PlayerProgress p = new PlayerProgress();
    p.globalTally.put("Global – Games Started: 50 Total", new PlayerProgress.Tally(49, null));

    RuleEvaluator.Result r = RuleEvaluator.evaluate(in, p);
    assertThat(r.awards).extracting(RuleEvaluator.Award::title).contains("Global – Games Started: 50 Total")
        .doesNotContain("tz_94h – Play 5 Minutes");
    assertThat(p.globalTally.get("Global – Games Started: 50 Total").progress).isEqualTo(51);
  }

  @Test
  void tallyUsesFuzzyPatternsAndRomRules() throws Exception {
    RuleEvaluator.Input in = new RuleEvaluator.Input();
    in.rom = "afm_113b";
    in.startAudits = audits("Ball Saver Cnt", 0L);
    in.endAudits = audits("Ball Saver Cnt", 25L);
    in.globalRules = globalRules();
    in.manufacturerOfRom = r -> "Bally";
    in.installedRoms = m -> "Bally".equals(m) ? Set.of("afm_113b", "mm_109c") : Set.of();
    PlayerProgress p = new PlayerProgress();
    p.romsPlayed.add("mm_109c");
    p.romsPlayed.add("tom_14h");

    RuleEvaluator.Result r = RuleEvaluator.evaluate(in, p);
    assertThat(r.awards).extracting(RuleEvaluator.Award::title)
        .contains("Global – Ball Saves: 20 Total", "Bally Rookie", "Bally Master")
        .doesNotContain("Global – Ball Saves: 50 Total");
    assertThat(p.globalTally.get("Bally Master").installedCount).isEqualTo(2);
  }
}
