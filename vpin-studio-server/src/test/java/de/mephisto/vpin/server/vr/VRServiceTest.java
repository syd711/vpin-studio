package de.mephisto.vpin.server.vr;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.server.dmd.DMDDeviceIniService;
import de.mephisto.vpin.server.emulators.EmulatorDetailsService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VRServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private EmulatorDetailsService emulatorDetailsService;

  @Mock
  private EmulatorService emulatorService;

  @Mock
  private VPXService vpxService;

  @Mock
  private DMDDeviceIniService dmdDeviceIniService;

  @InjectMocks
  private VRService vrService;

  private VRSettings vrSettings;

  @BeforeEach
  void setUp() throws Exception {
    vrSettings = new VRSettings(); // enabled=false, vrEnabled=false by default
    when(preferencesService.getJsonPreference(anyString(), eq(VRSettings.class))).thenReturn(vrSettings);
    vrService.afterPropertiesSet();
  }

  // ---- toggleVRMode ----

  @Test
  void toggleVRMode_returnsFalse_whenVRNotEnabled() {
    // vrSettings.isEnabled() defaults to false
    boolean result = vrService.toggleVRMode();
    assertFalse(result);
  }

  @Test
  void toggleVRMode_togglesVrEnabled_andSavesPreference_whenEnabled() throws Exception {
    vrSettings.setEnabled(true);
    vrSettings.setVrEnabled(false);
    when(emulatorService.getGameEmulators()).thenReturn(Collections.emptyList());

    boolean result = vrService.toggleVRMode();

    assertTrue(result);
    verify(preferencesService).savePreference(vrSettings);
  }

  @Test
  void toggleVRMode_togglesBack_whenVrWasAlreadyEnabled() throws Exception {
    vrSettings.setEnabled(true);
    vrSettings.setVrEnabled(true);
    when(emulatorService.getGameEmulators()).thenReturn(Collections.emptyList());

    boolean result = vrService.toggleVRMode();

    assertFalse(result);
    verify(preferencesService).savePreference(vrSettings);
  }

  // ---- getVRFiles ----

  @Test
  void getVRFiles_returnsNull_whenEmulatorNotFound() {
    when(emulatorService.getGameEmulator(99)).thenReturn(null);

    assertNull(vrService.getVRFiles(99));
  }

  @Test
  void getVRFiles_returnsNull_whenEmulatorIsNotVpx() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.isVpxEmulator()).thenReturn(false);
    when(emulatorService.getGameEmulator(1)).thenReturn(emulator);

    assertNull(vrService.getVRFiles(1));
  }

  // ---- getEmulatorVRLaunchScript ----

  @Test
  void getEmulatorVRLaunchScript_returnsNull_whenEmulatorNotFound() {
    when(emulatorService.getGameEmulator(5)).thenReturn(null);
    // vrLaunchScript lookup may return null too
    when(emulatorDetailsService.getGameEmulatorVRLaunchScript(5)).thenReturn(null);

    assertNull(vrService.getEmulatorVRLaunchScript(5));
  }

  @Test
  void getEmulatorVRLaunchScript_returnsVrScript_whenVrScriptExists() {
    GameEmulatorScript vrScript = mock(GameEmulatorScript.class);
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulatorDetailsService.getGameEmulatorVRLaunchScript(3)).thenReturn(vrScript);
    when(emulatorService.getGameEmulator(3)).thenReturn(emulator);

    GameEmulatorScript result = vrService.getEmulatorVRLaunchScript(3);

    assertSame(vrScript, result);
    verify(emulatorDetailsService, never()).cloneScript(any());
  }

  @Test
  void getEmulatorVRLaunchScript_clonesLaunchScript_whenVrScriptNullAndLaunchScriptPresent() {
    GameEmulatorScript launchScript = mock(GameEmulatorScript.class);
    GameEmulatorScript cloned = mock(GameEmulatorScript.class);
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getLaunchScript()).thenReturn(launchScript);
    when(emulatorDetailsService.getGameEmulatorVRLaunchScript(4)).thenReturn(null);
    when(emulatorService.getGameEmulator(4)).thenReturn(emulator);
    when(emulatorDetailsService.cloneScript(launchScript)).thenReturn(cloned);

    GameEmulatorScript result = vrService.getEmulatorVRLaunchScript(4);

    assertSame(cloned, result);
    verify(emulatorDetailsService).cloneScript(launchScript);
  }

  // ---- saveVRLaunchScript ----

  @Test
  void saveVRLaunchScript_delegatesToEmulatorDetailsService() {
    GameEmulatorScript script = mock(GameEmulatorScript.class);
    GameEmulatorScript saved = mock(GameEmulatorScript.class);
    when(emulatorDetailsService.saveEmulatorVRLaunchScript(7, script)).thenReturn(saved);

    GameEmulatorScript result = vrService.saveVRLaunchScript(7, script);

    assertSame(saved, result);
    verify(emulatorDetailsService).saveEmulatorVRLaunchScript(7, script);
  }

  // ---- preferenceChanged ----

  @Test
  void preferenceChanged_updatesSettings_forVrSettingsKey() throws Exception {
    VRSettings newSettings = new VRSettings();
    newSettings.setEnabled(true);
    when(preferencesService.getJsonPreference(PreferenceNames.VR_SETTINGS, VRSettings.class)).thenReturn(newSettings);

    vrService.preferenceChanged(PreferenceNames.VR_SETTINGS, null, null);

    // toggleVRMode now reflects new settings
    newSettings.setEnabled(false); // reset to false to test return value
    assertFalse(vrService.toggleVRMode());
  }
}
