package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscordChannelMessageFactoryTest {

  @Mock
  private PlayerService playerService;

  @InjectMocks
  private DiscordChannelMessageFactory factory;

  // --- createDiscordCompetitionCreatedMessage ---

  @Test
  void createDiscordCompetitionCreatedMessage_returnsFormattedString() {
    String msg = factory.createDiscordCompetitionCreatedMessage(12345L, 99L, "uuid-abc");

    assertThat(msg).contains("<@99>");
    assertThat(msg).contains("uuid-abc");
    assertThat(msg).contains(DiscordChannelMessageFactory.START_INDICATOR);
  }

  // --- createCompetitionFinishedMessage ---

  @Test
  void createCompetitionFinishedMessage_returnsIncompleteMessage_whenNoScores() {
    Competition competition = buildCompetition("Test Cup", "uuid-001");
    ScoreSummary summary = new ScoreSummary();

    String msg = factory.createCompetitionFinishedMessage(competition, summary);

    assertThat(msg).contains(DiscordChannelMessageFactory.FINISHED_INDICATOR);
    assertThat(msg).contains("Test Cup");
    assertThat(msg).contains("uuid-001");
  }

  @Test
  void createCompetitionFinishedMessage_returnsFinishedMessage_whenScoresPresent() {
    Competition competition = buildCompetition("Finals", "uuid-002");
    Score score = new Score(new Date(), 1, "AAA", null, "raw", 1000000L, 1);
    ScoreSummary summary = new ScoreSummary(Collections.singletonList(score), new Date(), "raw");

    String msg = factory.createCompetitionFinishedMessage(competition, summary);

    assertThat(msg).contains(DiscordChannelMessageFactory.FINISHED_INDICATOR);
    assertThat(msg).contains("Finals");
    assertThat(msg).contains("uuid-002");
  }

  // --- createCompetitionCancelledMessage ---

  @Test
  void createCompetitionCancelledMessage_withNullPlayer_returnsAnonymousMessage() {
    Competition competition = buildCompetition("Cup A", "uuid-003");

    String msg = factory.createCompetitionCancelledMessage(null, competition);

    assertThat(msg).contains(DiscordChannelMessageFactory.CANCEL_INDICATOR);
    assertThat(msg).contains("Cup A");
    assertThat(msg).doesNotContain("<@");
  }

  @Test
  void createCompetitionCancelledMessage_withPlayer_returnsPlayerMentionMessage() {
    Competition competition = buildCompetition("Cup B", "uuid-004");
    Player player = mock(Player.class);
    when(player.getId()).thenReturn(42L);

    String msg = factory.createCompetitionCancelledMessage(player, competition);

    assertThat(msg).contains(DiscordChannelMessageFactory.CANCEL_INDICATOR);
    assertThat(msg).contains("<@42>");
    assertThat(msg).contains("Cup B");
  }

  // --- createCompetitionJoinedMessage ---

  @Test
  void createCompetitionJoinedMessage_returnsFormattedString() {
    Competition competition = buildCompetition("Join Cup", "uuid-005");
    DiscordMember bot = new DiscordMember();
    bot.setId(77L);

    String msg = factory.createCompetitionJoinedMessage(competition, bot);

    assertThat(msg).contains(DiscordChannelMessageFactory.JOIN_INDICATOR);
    assertThat(msg).contains("<@77>");
    assertThat(msg).contains("Join Cup");
    assertThat(msg).contains("uuid-005");
  }

  // --- createCompetitionHighscoreCreatedMessage ---

  @Test
  void createCompetitionHighscoreCreatedMessage_returnsFormattedString_withNoPlayerObjects() {
    Competition competition = buildCompetition("Highscore Cup", "uuid-006");
    competition.setScoreLimit(2);
    Game game = mock(Game.class);
    when(game.getGameDisplayName()).thenReturn("Funhouse");

    Score oldScore = new Score(new Date(), 1, "BBB", null, "raw", 500000L, 2);
    Score newScore = new Score(new Date(), 1, "AAA", null, "raw", 1000000L, 1);
    List<Score> updated = Arrays.asList(newScore, oldScore);

    String msg = factory.createCompetitionHighscoreCreatedMessage(game, competition, oldScore, newScore, updated);

    assertThat(msg).contains(DiscordChannelMessageFactory.HIGHSCORE_INDICATOR);
    assertThat(msg).contains("uuid-006");
    verify(playerService).validateInitials(newScore);
  }

  // --- static createHighscoreList ---

  @Test
  void createHighscoreList_includesAllScores() {
    Score s1 = new Score(new Date(), 1, "AAA", null, "raw", 1000000L, 1);
    Score s2 = new Score(new Date(), 1, "BBB", null, "raw", 500000L, 2);
    List<Score> scores = Arrays.asList(s1, s2);

    String result = DiscordChannelMessageFactory.createHighscoreList(scores, 2);

    assertThat(result).contains("#1");
    assertThat(result).contains("#2");
    assertThat(result).contains("AAA");
    assertThat(result).contains("BBB");
  }

  @Test
  void createHighscoreList_fillsEmptySlots_whenScoresLessThanLimit() {
    Score s1 = new Score(new Date(), 1, "AAA", null, "raw", 1000000L, 1);
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

  // --- static createInitialHighscoreList ---

  @Test
  void createInitialHighscoreList_firstPositionIsScore_restArePlaceholders() {
    Score score = new Score(new Date(), 1, "ZZZ", null, "raw", 9999999L, 1);

    String result = DiscordChannelMessageFactory.createInitialHighscoreList(score, 3);

    assertThat(result).contains("#1");
    assertThat(result).contains("ZZZ");
    assertThat(result).contains("#2");
    assertThat(result).contains("#3");
    assertThat(result).contains("???");
  }

  // --- helpers ---

  private Competition buildCompetition(String name, String uuid) {
    Competition c = new Competition();
    c.setName(name);
    c.setUuid(uuid);
    return c;
  }
}
