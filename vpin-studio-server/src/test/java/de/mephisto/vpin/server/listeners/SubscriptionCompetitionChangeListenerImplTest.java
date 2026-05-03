package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.discord.DiscordSubscriptionMessageFactory;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionCompetitionChangeListenerImplTest {

  @Mock
  private CompetitionService competitionService;
  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private DiscordService discordService;
  @Mock
  private GameService gameService;
  @Mock
  private FrontendStatusService frontendStatusService;
  @Mock
  private AssetService assetService;
  @Mock
  private DiscordSubscriptionMessageFactory discordSubscriptionMessageFactory;
  @Mock
  private HighscoreParsingService highscoreParser;
  @Mock
  private HighscoreBackupService highscoreBackupService;

  @InjectMocks
  private SubscriptionCompetitionChangeListenerImpl listener;

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_registersListenerWithLifecycleService() throws Exception {
    listener.afterPropertiesSet();

    verify(competitionLifecycleService).addCompetitionChangeListener(listener);
  }

  // --- competitionCreated ---

  @Test
  void competitionCreated_skips_whenNotSubscriptionOrIscoredType() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());

    listener.competitionCreated(competition);

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionCreated_withIscored_skips_whenGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.ISCORED.name());
    competition.setGameId(99);
    when(gameService.getGame(99)).thenReturn(null);
    when(gameService.getGameByVpsTable(any(), any())).thenReturn(null);

    listener.competitionCreated(competition);

    verifyNoInteractions(discordService, highscoreBackupService);
  }

  // --- competitionDeleted ---

  @Test
  void competitionDeleted_skips_whenNotSubscriptionType() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());

    listener.competitionDeleted(competition);

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionDeleted_skips_whenSubscriptionButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.SUBSCRIPTION.name());
    competition.setGameId(88);
    competition.setOwner("bot-owner");
    when(gameService.getGame(88)).thenReturn(null);

    listener.competitionDeleted(competition);

    verifyNoInteractions(discordService);
  }

  // --- helpers ---

  private Competition buildCompetition(String type) {
    Competition c = new Competition();
    c.setType(type);
    c.setName("Test Competition");
    c.setOwner("owner");
    return c;
  }
}
