package de.mephisto.vpin.server.competitions.wovp;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.server.competitions.CompetitionIdUpdater;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WOVPCompetitionSynchronizerTest {

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private CompetitionService competitionService;
  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private FrontendService frontendService;
  @Mock
  private HighscoreBackupService highscoreBackupService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private GameService gameService;
  @Mock
  private FrontendStatusService frontendStatusService;
  @Mock
  private CompetitionIdUpdater competitionIdUpdater;

  @InjectMocks
  private WOVPCompetitionSynchronizer synchronizer;

  private WOVPSettings buildSettings(boolean enabled) {
    WOVPSettings settings = mock(WOVPSettings.class);
    lenient().when(settings.isEnabled()).thenReturn(enabled);
    when(preferencesService.getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class)).thenReturn(settings);
    return settings;
  }

  // --- synchronizeWovp early-exit paths ---

  @Test
  void synchronizeWovp_returnsFalse_whenApiKeyIsEmpty() {
    buildSettings(true);

    boolean result = synchronizer.synchronizeWovp("", false);

    assertFalse(result);
    verifyNoInteractions(competitionService);
  }

  @Test
  void synchronizeWovp_returnsFalse_whenApiKeyIsNull() {
    buildSettings(true);

    boolean result = synchronizer.synchronizeWovp(null, false);

    assertFalse(result);
    verifyNoInteractions(competitionService);
  }

  @Test
  void synchronizeWovp_returnsFalse_whenSettingsDisabled() {
    buildSettings(false);

    boolean result = synchronizer.synchronizeWovp("valid-api-key", false);

    assertFalse(result);
    verifyNoInteractions(competitionService);
  }

  @Test
  void synchronizeWovp_returnsFalse_whenBothEmptyKeyAndDisabled() {
    buildSettings(false);

    boolean result = synchronizer.synchronizeWovp("", false);

    assertFalse(result);
  }

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_completesWithoutException() {
    assertDoesNotThrow(() -> synchronizer.afterPropertiesSet());
  }

  // --- preferenceChanged ---

  @Test
  void preferenceChanged_triggersSync_whenWovpSettingsChange() throws Exception {
    WOVPSettings settings = buildSettings(true);
    when(settings.getAnyApiKey()).thenReturn("");

    synchronizer.preferenceChanged(PreferenceNames.WOVP_SETTINGS, null, null);

    // sync was called but returned false due to empty key — competition service never touched
    verifyNoInteractions(competitionService);
  }

  @Test
  void preferenceChanged_ignoresUnrelatedProperties() throws Exception {
    synchronizer.preferenceChanged("some.other.property", null, null);

    verifyNoInteractions(preferencesService, competitionService);
  }
}
