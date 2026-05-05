package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.games.Game;
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
public class VPXServiceTest {

  @Mock
  private SystemService systemService;
  @Mock
  private VPXCommandLineService vpxCommandLineService;

  @InjectMocks
  private VPXService service;

  @TempDir
  Path tempDir;

  // ---- getVPXFile ----

  @Test
  void getVPXFile_returnsAppDataPath() {
    File file = service.getVPXFile();
    assertThat(file.getName()).isEqualTo("VPinballX.ini");
    assertThat(file.getAbsolutePath()).contains("VPinballX");
  }

  // ---- isForceDisableB2S ----

  @Test
  void isForceDisableB2S_noIniLoaded_returnsFalse() {
    assertThat(service.isForceDisableB2S()).isFalse();
  }

  // ---- play ----

  @Test
  void play_nullGame_returnsFalse() {
    assertThat(service.play(null, null, null)).isFalse();
  }

  @Test
  void play_cameraMode_executesWithPovEdit() {
    Game game = mock(Game.class);
    when(vpxCommandLineService.execute(game, null, "-Minimized", "-PovEdit")).thenReturn(true);

    boolean result = service.play(game, null, "cameraMode");

    assertThat(result).isTrue();
    verify(vpxCommandLineService).execute(game, null, "-Minimized", "-PovEdit");
  }

  @Test
  void play_primaryMode_executesWithPrimaryPlay() {
    Game game = mock(Game.class);
    when(vpxCommandLineService.execute(game, null, "-Minimized", "-Primary", "-Play")).thenReturn(true);

    boolean result = service.play(game, null, "primary");

    assertThat(result).isTrue();
    verify(vpxCommandLineService).execute(game, null, "-Minimized", "-Primary", "-Play");
  }

  @Test
  void play_defaultMode_executesWithPlay() {
    Game game = mock(Game.class);
    when(vpxCommandLineService.execute(game, null, "-Minimized", "-Play")).thenReturn(true);

    boolean result = service.play(game, null, null);

    assertThat(result).isTrue();
    verify(vpxCommandLineService).execute(game, null, "-Minimized", "-Play");
  }

  // ---- delete ----

  @Test
  void delete_nullGame_returnsFalse() {
    assertThat(service.delete(null)).isFalse();
  }

  @Test
  void delete_gameWithMissingPovFile_returnsFalse() {
    Game game = mock(Game.class);
    File nonExistent = new File(tempDir.toFile(), "missing.pov");
    when(game.getPOVFile()).thenReturn(nonExistent);

    assertThat(service.delete(game)).isFalse();
  }

  @Test
  void delete_gameWithExistingPovFile_deletesAndReturnsTrue() throws Exception {
    Game game = mock(Game.class);
    File povFile = tempDir.resolve("game.pov").toFile();
    povFile.createNewFile();
    when(game.getPOVFile()).thenReturn(povFile);

    boolean result = service.delete(game);

    assertThat(result).isTrue();
    assertThat(povFile.exists()).isFalse();
  }

  // ---- getChecksum / getScript / getSources / importVBS ----

  @Test
  void getChecksum_nullGame_returnsNull() {
    assertThat(service.getChecksum(null)).isNull();
  }

  @Test
  void getScript_nullGame_returnsNull() {
    assertThat(service.getScript(null)).isNull();
  }

  @Test
  void getSources_nullGame_returnsNull() {
    assertThat(service.getSources(null)).isNull();
  }

  @Test
  void importVBS_nullGame_returnsFalse() {
    assertThat(service.importVBS(null, "script", false)).isFalse();
  }

  // ---- waitForPlayer ----

  @Test
  void waitForPlayer_delegatesToSystemService() {
    when(systemService.waitForWindow("Visual Pinball Player", 60, 2000)).thenReturn(true);

    assertThat(service.waitForPlayer()).isTrue();
  }
}
