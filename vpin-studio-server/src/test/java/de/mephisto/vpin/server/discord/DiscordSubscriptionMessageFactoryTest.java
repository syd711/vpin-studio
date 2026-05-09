package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscordSubscriptionMessageFactoryTest {

  @Mock
  private PlayerService playerService;

  @InjectMocks
  private DiscordSubscriptionMessageFactory factory;

  // --- createSubscriptionCreatedMessage ---

  @Test
  void createSubscriptionCreatedMessage_returnsFormattedString() {
    String msg = factory.createSubscriptionCreatedMessage(12345L, 99L, "sub-uuid-001");

    assertThat(msg).contains("<@99>");
    assertThat(msg).contains("sub-uuid-001");
    assertThat(msg).contains("subscription");
  }

  // --- createSubscriptionJoinedMessage ---

  @Test
  void createSubscriptionJoinedMessage_returnsFormattedString() {
    Competition competition = buildCompetition("Sub Channel", "sub-uuid-002");
    DiscordMember bot = new DiscordMember();
    bot.setId(55L);

    String msg = factory.createSubscriptionJoinedMessage(competition, bot);

    assertThat(msg).contains(DiscordChannelMessageFactory.JOIN_INDICATOR);
    assertThat(msg).contains("<@55>");
  }

  // --- createFirstSubscriptionHighscoreMessage ---

  @Test
  void createFirstSubscriptionHighscoreMessage_containsHighscoreIndicator() {
    Competition competition = buildCompetition("Sub Cup", "sub-uuid-003");
    competition.setScoreLimit(3);
    Game game = mock(Game.class);
    Score newScore = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);

    String msg = factory.createFirstSubscriptionHighscoreMessage(game, competition, newScore, 3);

    assertThat(msg).contains(DiscordChannelMessageFactory.HIGHSCORE_INDICATOR);
    assertThat(msg).contains("sub-uuid-003");
    verify(playerService).validateInitials(newScore);
  }

  // --- createSubscriptionHighscoreCreatedMessage ---

  @Test
  void createSubscriptionHighscoreCreatedMessage_containsHighscoreIndicator_withNoPlayerObjects() {
    Competition competition = buildCompetition("Score Sub", "sub-uuid-004");
    competition.setDiscordServerId(12345L);
    competition.setScoreLimit(2);
    Game game = mock(Game.class);
    Score oldScore = new Score(Instant.now(), 1, "BBB", null, "raw", 500000L, 2);
    Score newScore = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);
    List<Score> updatedScores = Arrays.asList(newScore, oldScore);

    String msg = factory.createSubscriptionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores);

    assertThat(msg).contains(DiscordChannelMessageFactory.HIGHSCORE_INDICATOR);
    assertThat(msg).contains("sub-uuid-004");
    verify(playerService).validateInitials(newScore);
  }

  @Test
  void createSubscriptionHighscoreCreatedMessage_includesBeatenMessage_whenDifferentPlayers() {
    Competition competition = buildCompetition("Score Sub", "sub-uuid-005");
    competition.setDiscordServerId(0L);
    competition.setScoreLimit(2);
    Game game = mock(Game.class);
    Score oldScore = new Score(Instant.now(), 1, "BBB", null, "raw", 500000L, 2);
    Score newScore = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);
    List<Score> updatedScores = Collections.singletonList(newScore);

    String msg = factory.createSubscriptionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores);

    assertThat(msg).contains("beaten");
  }

  @Test
  void createSubscriptionHighscoreCreatedMessage_notesOwnRecord_whenSamePlayer() {
    Competition competition = buildCompetition("Self Sub", "sub-uuid-006");
    competition.setDiscordServerId(0L);
    competition.setScoreLimit(1);
    Game game = mock(Game.class);
    Score oldScore = new Score(Instant.now(), 1, "AAA", null, "raw", 500000L, 1);
    Score newScore = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);
    List<Score> updatedScores = Collections.singletonList(newScore);

    String msg = factory.createSubscriptionHighscoreCreatedMessage(game, competition, oldScore, newScore, updatedScores);

    assertThat(msg).contains("own");
  }

  // --- helpers ---

  private Competition buildCompetition(String name, String uuid) {
    Competition c = new Competition();
    c.setName(name);
    c.setUuid(uuid);
    return c;
  }
}
