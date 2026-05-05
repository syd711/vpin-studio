package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameRomAliasService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import de.mephisto.vpin.server.vps.VpsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameCachingServiceTest {

  @Mock
  private FrontendService frontendService;
  @Mock
  private EmulatorService emulatorService;
  @Mock
  private GameDetailsRepositoryService gameDetailsRepositoryService;
  @Mock
  private VPinMameRomAliasService VPinMameRomAliasService;
  @Mock
  private BackglassService backglassService;
  @Mock
  private VpsService vpsService;
  @Mock
  private GameValidationService gameValidationService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private AltSoundService altSoundService;
  @Mock
  private AltColorService altColorService;
  @Mock
  private PupPacksService pupPacksService;
  @Mock
  private PreferencesService preferencesService;
  @Mock
  private VPinMameService vPinMameService;
  @Mock
  private RomService romService;
  @Mock
  private GameLifecycleService gameLifecycleService;
  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private SystemService systemService;

  @InjectMocks
  private GameCachingService service;

  @Test
  void clearCache_doesNotThrow() {
    assertDoesNotThrow(() -> service.clearCache());
  }

  @Test
  void clearCacheForEmulator_doesNotThrow() {
    assertDoesNotThrow(() -> service.clearCacheForEmulator(1));
  }

  @Test
  void preferenceChanged_updatesServerSettings_forServerSettingsKey() throws Exception {
    ServerSettings settings = new ServerSettings();
    when(preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class))
        .thenReturn(settings);

    service.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);

    verify(preferencesService).getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
  }

  @Test
  void preferenceChanged_ignoresOtherKeys() throws Exception {
    service.preferenceChanged("some.other.key", null, null);

    verify(preferencesService, never()).getJsonPreference(eq(PreferenceNames.SERVER_SETTINGS), any());
  }

  @Test
  void afterPropertiesSet_registersListenersAndLoadsSettings() throws Exception {
    ServerSettings settings = new ServerSettings();
    when(preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class))
        .thenReturn(settings);

    service.afterPropertiesSet();

    verify(VPinMameRomAliasService).setGameCachingService(service);
    verify(preferencesService).addChangeListener(service);
    verify(gameLifecycleService).addGameLifecycleListener(service);
    verify(gameLifecycleService).addGameDataChangedListener(service);
    verify(competitionLifecycleService).addCompetitionChangeListener(service);
    verify(emulatorService).addEmulatorChangeListener(service);
  }
}
