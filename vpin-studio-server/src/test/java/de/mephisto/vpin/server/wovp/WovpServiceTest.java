package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.restclient.wovp.ScoreSubmit;
import de.mephisto.vpin.connectors.wovp.models.WovpPlayer;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.competitions.wovp.WOVPCompetitionSynchronizer;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.recorder.ScreenshotService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ServerMessages;
import de.mephisto.vpin.connectors.wovp.Wovp;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class WovpServiceTest {

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private WOVPCompetitionSynchronizer wovpCompetitionSynchronizer;
  @Mock
  private GameService gameService;
  @Mock
  private GameStatusService gameStatusService;
  @Mock
  private ScreenshotService screenshotService;
  @Mock
  private CompetitionService competitionService;
  @Mock
  private SystemService systemService;
  @Mock
  private FrontendService frontendService;
  @Mock
  private FrontendStatusService frontendStatusService;

  @InjectMocks
  private WovpService service;

  private WOVPSettings wovpSettings;

  @BeforeEach
  void injectSettings() throws Exception {
    wovpSettings = new WOVPSettings();
    Field field = WovpService.class.getDeclaredField("wovpSettings");
    field.setAccessible(true);
    field.set(service, wovpSettings);
  }

  // ---- validateKey ----

  @Test
  void validateKey_returnsFailure_whenApiKeyIsNull() {
    ApiKeyValidationResponse response = service.validateKey(null);

    assertNotNull(response);
    assertFalse(response.isSuccess());
  }

  @Test
  void validateKey_returnsFailure_whenApiKeyIsEmpty() {
    ApiKeyValidationResponse response = service.validateKey("");

    assertNotNull(response);
    assertFalse(response.isSuccess());
  }

  // ---- submitScore ----

  @Test
  void submitScore_returnsError_whenWovpNotEnabled() {
    wovpSettings.setEnabled(false);
    WovpPlayer player = new WovpPlayer();

    ScoreSubmit result = service.submitScore(player, null, false);

    assertNotNull(result);
    assertNotNull(result.getErrorMessage());
    assertTrue(result.getErrorMessage().contains("not enabled"));
  }

  @Test
  void submitScore_returnsError_whenScoreSubmitterNotEnabled() {
    wovpSettings.setEnabled(true);
    wovpSettings.setUseScoreSubmitter(false);
    WovpPlayer player = new WovpPlayer();

    ScoreSubmit result = service.submitScore(player, null, false);

    assertNotNull(result);
    assertNotNull(result.getErrorMessage());
    assertTrue(result.getErrorMessage().contains("submitter not enabled"));
  }

  // ---- submitScore with explicit game id (pause menu test) ----

  @AfterEach
  void clearWovpCache() {
    Wovp.clearCache();
  }

  @SuppressWarnings("unchecked")
  private WovpPlayer cachedPlayer() throws Exception {
    WovpPlayer player = new WovpPlayer();
    player.setId("player-1");
    player.setApiKey("api-key");
    Field field = Wovp.class.getDeclaredField("players");
    field.setAccessible(true);
    ((Map<String, WovpPlayer>) field.get(null)).put(player.getId(), player);
    return player;
  }

  private void enableSubmitter() {
    wovpSettings.setEnabled(true);
    wovpSettings.setUseScoreSubmitter(true);
  }

  private GameStatus inactiveStatus() {
    GameStatus status = new GameStatus();
    status.setGameId(-1);
    return status;
  }

  @Test
  void submitScore_simulated_usesGivenGameId_whenNoGameIsActive() throws Exception {
    enableSubmitter();
    WovpPlayer player = cachedPlayer();
    Game game = new Game();
    game.setId(559);
    org.mockito.Mockito.when(gameService.getGame(559)).thenReturn(game);
    org.mockito.Mockito.when(competitionService.getWeeklyCompetitions()).thenReturn(Collections.emptyList());

    ScoreSubmit result = service.submitScore(player, null, true, 559);

    //the game check passed, the submit fails at the next step since there is no challenge for the game
    assertEquals(ServerMessages.msg("wovp.error.no_challenge"), result.getErrorMessage());
    org.mockito.Mockito.verifyNoInteractions(gameStatusService);
  }

  @Test
  void submitScore_real_ignoresGivenGameId_andRequiresActiveGame() throws Exception {
    enableSubmitter();
    WovpPlayer player = cachedPlayer();
    org.mockito.Mockito.when(gameStatusService.getStatus()).thenReturn(inactiveStatus());

    ScoreSubmit result = service.submitScore(player, null, false, 559);

    assertEquals(ServerMessages.msg("wovp.error.no_active_game"), result.getErrorMessage());
  }

  @Test
  void submitScore_simulated_withoutGameId_requiresActiveGame() throws Exception {
    enableSubmitter();
    WovpPlayer player = cachedPlayer();
    org.mockito.Mockito.when(gameStatusService.getStatus()).thenReturn(inactiveStatus());

    ScoreSubmit result = service.submitScore(player, null, true);

    assertEquals(ServerMessages.msg("wovp.error.no_active_game"), result.getErrorMessage());
  }
}
