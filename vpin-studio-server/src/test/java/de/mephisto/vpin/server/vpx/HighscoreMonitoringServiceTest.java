package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreMonitoringService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HighscoreMonitoringServiceTest {

  @Mock
  private GameService gameService;
  @Mock
  private EmulatorService emulatorService;
  @Mock
  private PreferencesService preferencesService;
  @Mock
  private VPinMameService vPinMameService;
  @Mock
  private HighscoreService highscoreService;

  @InjectMocks
  private HighscoreMonitoringService highscoreMonitoringService;

  @Test
  void preferenceChanged_withServerSettings_enabledTrue_startsFolderMonitors() throws Exception {
    ServerSettings settings = new ServerSettings();
    settings.setHighscoreMonitorEnabled(true);
    when(preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class))
        .thenReturn(settings);

    highscoreMonitoringService.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);

    // emulatorService is consulted to enumerate emulator folders to watch
    verify(emulatorService).getVpxGameEmulators();
    verify(emulatorService).getFpGameEmulators();
  }

  @Test
  void preferenceChanged_withServerSettings_enabledFalse_stopsFolderMonitors() throws Exception {
    ServerSettings settings = new ServerSettings();
    settings.setHighscoreMonitorEnabled(false);
    when(preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class))
        .thenReturn(settings);

    highscoreMonitoringService.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);

    // No exception = success, no folder monitors get started
    verifyNoInteractions(highscoreService);
  }

  @Test
  void preferenceChanged_withUnrelatedProperty_doesNothing() throws Exception {
    highscoreMonitoringService.preferenceChanged("some.other.property", null, null);

    verifyNoInteractions(preferencesService);
    verifyNoInteractions(emulatorService);
  }

  @Test
  void shutdown_doesNotThrow() {
    highscoreMonitoringService.shutdown();
  }
}
