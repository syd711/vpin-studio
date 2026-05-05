package de.mephisto.vpin.server.pinemhi;

import de.mephisto.vpin.server.preferences.Preferences;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PINemHiServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private SystemService systemService;

  @Mock
  private VPinMameService vPinMameService;

  @InjectMocks
  private PINemHiService pinemHiService;

  @Test
  void getAutoStart_delegatesToPreferences() {
    Preferences prefs = mock(Preferences.class);
    when(prefs.getPinemhiAutoStartEnabled()).thenReturn(true);
    when(preferencesService.getPreferences()).thenReturn(prefs);

    boolean result = pinemHiService.getAutoStart();

    assertTrue(result);
  }

  @Test
  void getAutoStart_returnsFalse_whenDisabled() {
    Preferences prefs = mock(Preferences.class);
    when(prefs.getPinemhiAutoStartEnabled()).thenReturn(false);
    when(preferencesService.getPreferences()).thenReturn(prefs);

    boolean result = pinemHiService.getAutoStart();

    assertFalse(result);
  }

  @Test
  void getRomList_returnsEmptyList_initially() {
    List<String> result = pinemHiService.getRomList();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void isRunning_delegatesToSystemService() {
    when(systemService.isProcessRunning("pinemhi_rom_monitor")).thenReturn(true);

    boolean result = pinemHiService.isRunning();

    assertTrue(result);
    verify(systemService).isProcessRunning("pinemhi_rom_monitor");
  }

  @Test
  void kill_delegatesToSystemService() {
    when(systemService.killProcesses("pinemhi_rom_monitor")).thenReturn(true);

    boolean result = pinemHiService.kill();

    assertTrue(result);
    verify(systemService).killProcesses("pinemhi_rom_monitor");
  }

  @Test
  void toggleAutoStart_togglesFromFalseToTrue() throws Exception {
    pinemHiService.toggleAutoStart();

    verify(preferencesService).savePreference(
        eq(de.mephisto.vpin.restclient.PreferenceNames.PINEMHI_AUTOSTART_ENABLED),
        eq(true),
        eq(false));
  }

  @Test
  void toggleAutoStart_togglesBackToFalse_onSecondCall() throws Exception {
    pinemHiService.toggleAutoStart();
    pinemHiService.toggleAutoStart();

    verify(preferencesService).savePreference(
        eq(de.mephisto.vpin.restclient.PreferenceNames.PINEMHI_AUTOSTART_ENABLED),
        eq(false),
        eq(false));
  }
}
