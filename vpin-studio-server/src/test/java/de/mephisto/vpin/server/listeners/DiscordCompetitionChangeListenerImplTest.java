package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.vps.VpsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscordCompetitionChangeListenerImplTest {

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
  private HighscoreBackupService highscoreBackupService;
  @Mock
  private DiscordChannelMessageFactory discordChannelMessageFactory;
  @Mock
  private VpsService vpsService;

  @InjectMocks
  private DiscordCompetitionChangeListenerImpl listener;

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_registersListenerWithLifecycleService() throws Exception {
    listener.afterPropertiesSet();

    verify(competitionLifecycleService).addCompetitionChangeListener(listener);
  }

  // --- competitionStarted ---

  @Test
  void competitionStarted_skips_whenNotDiscordType() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());

    listener.competitionStarted(competition);

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionStarted_skips_whenDiscordButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());
    competition.setGameId(99);
    when(gameService.getGame(99)).thenReturn(null);

    listener.competitionStarted(competition);

    verifyNoInteractions(discordService);
  }

  // --- competitionCreated ---

  @Test
  void competitionCreated_skips_whenNotDiscordType() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());

    listener.competitionCreated(competition);

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionCreated_sendsNoMessages_whenDiscordButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());
    competition.setGameId(88);
    when(gameService.getGame(88)).thenReturn(null);
    when(discordService.getBot()).thenReturn(null);

    listener.competitionCreated(competition);

    // getBotId/getBot may be called but no messages are sent since game is null
    verify(discordService, never()).sendMessage(anyLong(), anyLong(), anyString());
  }

  // --- competitionFinished ---

  @Test
  void competitionFinished_skips_whenNotDiscordType() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());

    listener.competitionFinished(competition, null, new ScoreSummary());

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionFinished_skips_whenDiscordButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());
    competition.setGameId(77);
    when(gameService.getGame(77)).thenReturn(null);

    listener.competitionFinished(competition, null, new ScoreSummary());

    verifyNoInteractions(discordService);
  }

  // --- competitionDeleted ---

  @Test
  void competitionDeleted_skips_whenNotDiscordType() {
    Competition competition = buildCompetition(CompetitionType.OFFLINE.name());

    listener.competitionDeleted(competition);

    verifyNoInteractions(gameService, discordService);
  }

  @Test
  void competitionDeleted_skips_whenDiscordButGameNotFound() {
    Competition competition = buildCompetition(CompetitionType.DISCORD.name());
    competition.setGameId(66);
    when(gameService.getGame(66)).thenReturn(null);

    listener.competitionDeleted(competition);

    verifyNoInteractions(discordService);
  }

  // --- helpers ---

  private Competition buildCompetition(String type) {
    Competition c = new Competition();
    c.setType(type);
    c.setOwner("bot-owner");
    c.setName("Test Competition");
    return c;
  }
}
