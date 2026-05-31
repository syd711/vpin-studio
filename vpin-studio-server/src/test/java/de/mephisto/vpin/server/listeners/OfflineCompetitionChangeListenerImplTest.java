package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfflineCompetitionChangeListenerImplTest {

  @Mock
  private CompetitionService competitionService;
  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private PreferencesService preferencesService;
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
  private HighscoreBackupService highscoreBackupService;
  @Mock
  private HighscoreParsingService highscoreParsingService;

  @InjectMocks
  private OfflineCompetitionChangeListenerImpl listener;

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_registersListenerWithLifecycleService() throws Exception {
    listener.afterPropertiesSet();

    verify(competitionLifecycleService).addCompetitionChangeListener(listener);
  }

  // --- competitionStarted ---

  @Test
  void competitionStarted_skips_whenNotOfflineType() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());

    listener.competitionStarted(competition);

    verifyNoInteractions(gameService, discordService, highscoreBackupService);
  }

  @Test
  void competitionStarted_skips_whenOfflineButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());
    competition.setGameId(99);
    when(gameService.getGame(99)).thenReturn(null);

    listener.competitionStarted(competition);

    verifyNoInteractions(discordService, highscoreBackupService);
  }

  // --- competitionFinished ---

  @Test
  void competitionFinished_skips_whenNotOfflineType() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());

    listener.competitionFinished(competition, null, new ScoreSummary());

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionFinished_skips_whenOfflineButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());
    competition.setGameId(99);
    when(gameService.getGame(99)).thenReturn(null);

    listener.competitionFinished(competition, mock(Player.class), new ScoreSummary());

    verifyNoInteractions(discordService);
  }

  // --- competitionDeleted ---

  @Test
  void competitionDeleted_skips_whenNotOfflineType() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());

    listener.competitionDeleted(competition);

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionDeleted_skips_whenOfflineButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());
    competition.setGameId(99);
    when(gameService.getGame(99)).thenReturn(null);

    listener.competitionDeleted(competition);

    verifyNoInteractions(discordService);
  }

  // --- helpers ---

  private Competition buildCompetition(String type) {
    Competition c = new Competition();
    c.setType(type);
    c.setName("Test Competition");
    return c;
  }
}
