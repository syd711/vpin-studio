package de.mephisto.vpin.server.pinvol;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PinVolServiceTest {

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private SystemService systemService;
  @Mock
  private GameService gameService;
  @Mock
  private GameLifecycleService gameLifecycleService;

  @InjectMocks
  private PinVolService pinVolService;

  @TempDir
  Path tempDir;

  @Test
  void isRunning_returnsTrueWhenProcessRunning() {
    when(systemService.isProcessRunning("PinVol")).thenReturn(true);

    assertThat(pinVolService.isRunning()).isTrue();
  }

  @Test
  void isRunning_returnsFalseWhenNotRunning() {
    when(systemService.isProcessRunning("PinVol")).thenReturn(false);

    assertThat(pinVolService.isRunning()).isFalse();
  }

  @Test
  void isValid_returnsFalseWhenExeDoesNotExist() {
    when(preferencesService.getPreferenceValue(PreferenceNames.PINVOL_FOLDER))
        .thenReturn(tempDir.toString());

    assertThat(pinVolService.isValid()).isFalse();
  }

  @Test
  void isValid_returnsTrueWhenExeExists() throws Exception {
    File exe = tempDir.resolve("PinVol.exe").toFile();
    exe.createNewFile();
    when(preferencesService.getPreferenceValue(PreferenceNames.PINVOL_FOLDER))
        .thenReturn(tempDir.toString());

    assertThat(pinVolService.isValid()).isTrue();
  }

  @Test
  void killPinVol_delegatesToSystemService() {
    when(systemService.killProcesses("PinVol")).thenReturn(true);

    assertThat(pinVolService.killPinVol()).isTrue();
    verify(systemService).killProcesses("PinVol");
  }

  @Test
  void getPinVolTablesIniFile_returnsFileNextToExe() {
    when(preferencesService.getPreferenceValue(PreferenceNames.PINVOL_FOLDER))
        .thenReturn(tempDir.toString());

    File result = pinVolService.getPinVolTablesIniFile();

    assertThat(result.getParentFile().getAbsolutePath()).isEqualTo(tempDir.toFile().getAbsolutePath());
    assertThat(result.getName()).isEqualTo("PinVolTables.ini");
  }

  @Test
  void getPinVolSettingsIniFile_returnsCorrectFilename() {
    when(preferencesService.getPreferenceValue(PreferenceNames.PINVOL_FOLDER))
        .thenReturn(tempDir.toString());

    File result = pinVolService.getPinVolSettingsIniFile();

    assertThat(result.getName()).isEqualTo("PinVolSettings.ini");
  }

  @Test
  void getPinVolVolIniFile_returnsCorrectFilename() {
    when(preferencesService.getPreferenceValue(PreferenceNames.PINVOL_FOLDER))
        .thenReturn(tempDir.toString());

    File result = pinVolService.getPinVolVolIniFile();

    assertThat(result.getName()).isEqualTo("PinVolVol.ini");
  }

  @Test
  void getPinVolTablePreferences_initiallyNull() {
    assertThat(pinVolService.getPinVolTablePreferences()).isNull();
  }
}
