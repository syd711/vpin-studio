package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.restclient.achievements.SessionHighlight;
import de.mephisto.vpin.server.achievements.progression.SessionHighlights;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Expected values were produced with the Watcher's {@code _build_events_from_deltas} + {@code analyze_session}.
 */
public class SessionHighlightsTest {

  @Test
  void matchesWatcher() {
    Map<String, Long> d = new LinkedHashMap<>();
    d.put("Multiball Starts", 2L);
    d.put("Multiball Total", 3L);
    d.put("Balls Locked", 4L);
    d.put("Jackpots", 5L);
    d.put("Super Jackpots", 1L);
    d.put("Left Ramp", 7L);
    d.put("Right Ramps", 3L);
    d.put("Loops", 2L);
    d.put("Spinner", 40L);
    d.put("Skill Shot", 1L);
    d.put("Super Skill Shot", 1L);
    d.put("Mode Starts", 3L);
    d.put("Modes Completed", 1L);
    d.put("Tilt Warnings", 2L);
    d.put("Extra Balls", 1L);
    d.put("Ball Saves", 2L);
    d.put("Max Combo Count", 9L);
    d.put("Combos", 2L);
    d.put("Drop Targets", 12L);
    d.put("Left Orbit", 4L);
    d.put("Wizard Mode", 1L);
    d.put("Match Awards", 1L);
    d.put("Specials", 1L);
    d.put("Triple Jackpot", 1L);
    d.put("System Ramps", 5L);

    Map<String, List<SessionHighlight>> h = SessionHighlights.analyze(d, null);
    assertThat(lines(h.get("Power"))).containsExactly("Wizard Mode", "Jackpot Hunter 5", "Multiball 3", "Ball Saves 2", "Super Jackpot 1");
    assertThat(lines(h.get("Precision"))).containsExactly("Spinner Madness 40", "Target Slayer 12", "Rampage 10", "Orbit Runner 4", "Modes Started 3");
    assertThat(lines(h.get("Fun"))).containsExactly("Tilt Warnings 2", "Match Lucky 1");
  }

  @Test
  void multiballAndModes() {
    Map<String, Long> d = new LinkedHashMap<>();
    d.put("MB Jackpot", 3L);
    d.put("Multi-Ball Played", 1L);
    d.put("Multiball Lit", 2L);
    d.put("Tilted", 1L);
    d.put("Wave Start", 2L);
    d.put("Wave Completed", 2L);

    Map<String, List<SessionHighlight>> h = SessionHighlights.analyze(d, 1_666_000L);
    assertThat(lines(h.get("Power"))).containsExactly("Jackpot Hunter 3", "Modes Completed 2", "Multiball 1");
    assertThat(lines(h.get("Precision"))).containsExactly("Modes Started 2");
    // the Devil's Number comes from the score
    assertThat(lines(h.get("Fun"))).containsExactly("Devil’s Number", "Tilted 1");
  }

  @Test
  void emptyAndScore() {
    Map<String, List<SessionHighlight>> h = SessionHighlights.analyze(Map.of(), 1234L);
    assertThat(h.keySet()).containsExactly("Power", "Precision", "Fun");
    assertThat(h.values()).allMatch(List::isEmpty);

    assertThat(SessionHighlights.score(Map.of("P1 Score", 4200, "Score", 1))).isEqualTo(4200L);
    assertThat(SessionHighlights.score(Map.of("Score", "17"))).isEqualTo(17L);
    assertThat(SessionHighlights.score(Map.of("Games Started", 3))).isNull();
  }

  private static List<String> lines(List<SessionHighlight> list) {
    return list.stream().map(x -> x.flag ? x.label : x.label + " " + x.value).toList();
  }
}
