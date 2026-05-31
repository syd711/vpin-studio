package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
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
public class DMDDeviceIniServiceTest {

  @Mock
  private EmulatorService emulatorService;
  @Mock
  private VPinMameService vPinMameService;
  @Mock
  private SystemService systemService;

  @InjectMocks
  private DMDDeviceIniService dmdDeviceIniService;

  @TempDir
  Path tempDir;

  // ---- getDmdDeviceIniFile ----

  @Test
  void getDmdDeviceIniFile_returnsFileInMameFolder() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getMameFolder()).thenReturn(tempDir.toFile());

    File result = dmdDeviceIniService.getDmdDeviceIniFile(emulator);

    assertThat(result.getParentFile().getAbsolutePath()).isEqualTo(tempDir.toFile().getAbsolutePath());
    assertThat(result.getName()).isEqualTo(DMDDeviceIniService.DMD_DEVICE_INI);
  }

  // ---- getStoreName ----

  @Test
  void getStoreName_standardRom_returnsRom() {
    Game game = mock(Game.class);
    when(game.getRomAlias()).thenReturn(null);
    when(game.getRom()).thenReturn("myrom");
    when(game.getDMDType()).thenReturn(null);

    String result = dmdDeviceIniService.getStoreName(game);

    assertThat(result).isEqualTo("myrom");
  }

  @Test
  void getStoreName_romAliasTakesPrecedenceOverRom() {
    Game game = mock(Game.class);
    when(game.getRomAlias()).thenReturn("aliased_rom");
    when(game.getDMDType()).thenReturn(null);

    String result = dmdDeviceIniService.getStoreName(game);

    assertThat(result).isEqualTo("aliased_rom");
  }

  @Test
  void getStoreName_ultraDmd_usesTableBaseName() {
    Game game = mock(Game.class);
    when(game.getRomAlias()).thenReturn(null);
    when(game.getRom()).thenReturn("myrom");
    when(game.getDMDType()).thenReturn(DMDPackageTypes.UltraDMD);
    when(game.getGameFileName()).thenReturn("MyTable 1.0.vpx");

    String result = dmdDeviceIniService.getStoreName(game);

    // Should strip version suffix from basename
    assertThat(result).isEqualTo("MyTable");
  }

  @Test
  void getStoreName_flexDmd_withDmdGameName_usesDmdGameName() {
    Game game = mock(Game.class);
    when(game.getRomAlias()).thenReturn(null);
    when(game.getRom()).thenReturn("myrom");
    when(game.getDMDType()).thenReturn(DMDPackageTypes.FlexDMD);
    when(game.getDMDGameName()).thenReturn("FlexTable 2.1");

    String result = dmdDeviceIniService.getStoreName(game);

    assertThat(result).isEqualTo("FlexTable");
  }

  @Test
  void getStoreName_dotsReplacedWithSpaces() {
    Game game = mock(Game.class);
    when(game.getRomAlias()).thenReturn(null);
    when(game.getRom()).thenReturn("rom.with.dots");
    when(game.getDMDType()).thenReturn(null);

    String result = dmdDeviceIniService.getStoreName(game);

    assertThat(result).isEqualTo("rom with dots");
  }

  // ---- getDmdDeviceIni returns null when file missing ----

  @Test
  void getDmdDeviceIni_returnsNullWhenIniFileMissing() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getId()).thenReturn(1);
    when(emulator.getName()).thenReturn("VPX");
    when(emulator.getMameFolder()).thenReturn(tempDir.toFile());

    var result = dmdDeviceIniService.getDmdDeviceIni(emulator);

    assertThat(result).isNull();
  }

  // ---- getIniConfiguration returns null when file missing ----

  @Test
  void getIniConfiguration_returnsNullWhenIniFileMissing() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getId()).thenReturn(1);
    when(emulator.getMameFolder()).thenReturn(tempDir.toFile());
    Game game = mock(Game.class);
    when(game.getEmulator()).thenReturn(emulator);

    var result = dmdDeviceIniService.getIniConfiguration(game);

    assertThat(result).isNull();
  }
}
