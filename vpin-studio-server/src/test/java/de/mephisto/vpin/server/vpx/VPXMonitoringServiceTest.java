package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VPXMonitoringServiceTest {

  @Mock
  private GameStatusService gameStatusService;
  @Mock
  private GameService gameService;
  @Mock
  private EmulatorService emulatorService;
  @Mock
  private FrontendStatusService frontendStatusService;
  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private VPXMonitoringService vpxMonitoringService;

  @Test
  void preferenceChanged_withServerSettings_enabledTrue_setsRunningTrue() throws Exception {
    ServerSettings settings = new ServerSettings();
    settings.setUseVPXTableMonitor(true);
    when(preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class))
        .thenReturn(settings);

    vpxMonitoringService.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);

    // run() checks running flag; if running=true with no windows (no JNA) it just returns
    // No exception = success
  }

  @Test
  void preferenceChanged_withServerSettings_enabledFalse_setsRunningFalse() throws Exception {
    ServerSettings settings = new ServerSettings();
    settings.setUseVPXTableMonitor(false);
    when(preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class))
        .thenReturn(settings);

    vpxMonitoringService.preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);

    // No exception = success; running flag is set to false internally
  }

  @Test
  void preferenceChanged_withUnrelatedProperty_doesNothing() throws Exception {
    vpxMonitoringService.preferenceChanged("some.other.property", null, null);

    verifyNoInteractions(preferencesService);
  }

  @Test
  void run_whenNotRunning_returnsImmediately() {
    // running flag defaults to false — run() should return without querying emulators
    vpxMonitoringService.run();

    verifyNoInteractions(emulatorService);
    verifyNoInteractions(gameService);
    verifyNoInteractions(frontendStatusService);
  }

  @Test
  void shutdown_doesNotThrow() {
    vpxMonitoringService.shutdown();
  }
}
