package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.server.highscores.Score;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DiscordChannelMessageFactoryTest {

  @Test
  void createHighscoreList_includesAllScores() {
    Score s1 = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);
    Score s2 = new Score(Instant.now(), 1, "BBB", null, "raw", 500000L, 2);
    List<Score> scores = Arrays.asList(s1, s2);

    String result = DiscordChannelMessageFactory.createHighscoreList(scores, 2);

    assertThat(result).contains("#1");
    assertThat(result).contains("#2");
    assertThat(result).contains("AAA");
    assertThat(result).contains("BBB");
  }

  @Test
  void createHighscoreList_fillsEmptySlots_whenScoresLessThanLimit() {
    Score s1 = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);
    List<Score> scores = Collections.singletonList(s1);

    String result = DiscordChannelMessageFactory.createHighscoreList(scores, 3);

    assertThat(result).contains("#1");
    assertThat(result).contains("#2");
    assertThat(result).contains("#3");
    assertThat(result).contains("???");
  }

  @Test
  void createHighscoreList_returnsEmptyTable_whenNoScores() {
    String result = DiscordChannelMessageFactory.createHighscoreList(Collections.emptyList(), 0);

    assertThat(result).contains("Pos");
    assertThat(result).contains("Initials");
    assertThat(result).contains("Score");
  }
}
