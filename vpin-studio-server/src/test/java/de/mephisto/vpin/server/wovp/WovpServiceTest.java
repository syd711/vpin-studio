package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.restclient.wovp.ScoreSubmitResult;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    ScoreSubmitResult result = service.submitScore(player, false);

    assertNotNull(result);
    assertNotNull(result.getErrorMessage());
    assertTrue(result.getErrorMessage().contains("not enabled"));
  }

  @Test
  void submitScore_returnsError_whenScoreSubmitterNotEnabled() {
    wovpSettings.setEnabled(true);
    wovpSettings.setUseScoreSubmitter(false);
    WovpPlayer player = new WovpPlayer();

    ScoreSubmitResult result = service.submitScore(player, false);

    assertNotNull(result);
    assertNotNull(result.getErrorMessage());
    assertTrue(result.getErrorMessage().contains("submitter not enabled"));
  }
}
