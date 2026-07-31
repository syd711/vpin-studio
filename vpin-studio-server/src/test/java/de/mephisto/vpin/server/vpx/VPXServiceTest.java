package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.utils.VPXKeyManager;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VPXServiceTest {

  /**
   * Real-world VPinballX.ini captured from a live installation, used to exercise
   * ini parsing against actual VPX output rather than hand-crafted fixtures.
   */
  private static final File REFERENCE_INI = new File("../testsystem/vPinball/VisualPinball/VPinballX.ini");

  @Mock
  private SystemService systemService;
  @Mock
  private VPXCommandLineService vpxCommandLineService;

  @InjectMocks
  private VPXService service;

  @TempDir
  Path tempDir;

  private String originalUserHome;

  @AfterEach
  void restoreUserHome() {
    if (originalUserHome != null) {
      System.setProperty("user.home", originalUserHome);
      originalUserHome = null;
    }
  }

  /**
   * Points VPXService#getVPXFile() at a copy of the given ini file by redirecting
   * "user.home" into the temp dir, mirroring the real AppData/Roaming/VPinballX layout.
   */
  private File installIni(File source) throws IOException {
    originalUserHome = System.getProperty("user.home");
    System.setProperty("user.home", tempDir.toString());

    File target = tempDir.resolve("AppData/Roaming/VPinballX/VPinballX.ini").toFile();
    target.getParentFile().mkdirs();
    Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return target;
  }

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

  @Test
  void isForceDisableB2S_realIniFile_returnsFalse() throws IOException {
    installIni(REFERENCE_INI);
    service.clearCache();

    assertThat(service.isForceDisableB2S()).isFalse();
  }

  // ---- getPlayerConfiguration / getControllerConfiguration ----

  @Test
  void getPlayerConfiguration_noIniLoaded_returnsNull() {
    assertThat(service.getPlayerConfiguration(false)).isNull();
  }

  @Test
  void getPlayerConfiguration_realIniFile_returnsParsedPlayerSection() throws IOException {
    installIni(REFERENCE_INI);
    service.clearCache();

    Configuration player = service.getPlayerConfiguration(false);

    assertThat(player).isNotNull();
    assertThat(player.getInt("Sound3D")).isEqualTo(0);
    assertThat(player.getInt("DisableESC")).isEqualTo(1);
    assertThat(player.getInt("LFlipKey")).isEqualTo(42);
    assertThat(player.getInt("RFlipKey")).isEqualTo(54);
  }

  @Test
  void getControllerConfiguration_realIniFile_actuallyReturnsPlayerSection() throws IOException {
    installIni(REFERENCE_INI);
    service.clearCache();

    // getControllerConfiguration reads the "Player" section, not "Controller", so
    // Controller-only keys such as ForceDisableB2S are never visible through it.
    Configuration controllerSection = service.getControllerConfiguration(false);

    assertThat(controllerSection).isNotNull();
    assertThat(controllerSection.containsKey("Sound3D")).isTrue();
    assertThat(controllerSection.getString("ForceDisableB2S")).isNull();
  }

  @Test
  void getPlayerConfiguration_forceReloadTrue_reflectsUpdatedFileContent() throws IOException {
    File ini = installIni(REFERENCE_INI);
    service.clearCache();
    assertThat(service.getPlayerConfiguration(false).getInt("Sound3D")).isEqualTo(0);

    String updated = new String(Files.readAllBytes(ini.toPath()), StandardCharsets.UTF_8)
        .replace("Sound3D = 0", "Sound3D = 1");
    Files.write(ini.toPath(), updated.getBytes(StandardCharsets.UTF_8));

    Configuration reloaded = service.getPlayerConfiguration(true);

    assertThat(reloaded.getInt("Sound3D")).isEqualTo(1);
  }

  // ---- clearCache / missing ini ----

  @Test
  void clearCache_missingIniFile_leavesConfigurationNull() {
    originalUserHome = System.getProperty("user.home");
    System.setProperty("user.home", tempDir.toString());

    service.clearCache();

    assertThat(service.getPlayerConfiguration(false)).isNull();
    assertThat(service.getKeyManager()).isNull();
  }

  // ---- getKeyManager ----

  @Test
  void getKeyManager_realIniFile_mapsDirectXCodesToNativeCodes() throws IOException {
    installIni(REFERENCE_INI);
    service.clearCache();

    VPXKeyManager keyManager = service.getKeyManager();

    assertThat(keyManager).isNotNull();
    // 42/54 are DirectX scan codes for Shift Left/Right, remapped to native codes 160/161.
    assertThat(keyManager.getBinding(VPXKeyManager.LFlipKey)).isEqualTo(160);
    assertThat(keyManager.getBinding(VPXKeyManager.RFlipKey)).isEqualTo(161);
    assertThat(keyManager.getBinding(VPXKeyManager.StartGameKey)).isEqualTo(2);
  }

  // ---- isFullscreenEnabled ----

  @Test
  void isFullscreenEnabled_realIniFile_returnsFalse() throws IOException {
    installIni(REFERENCE_INI);
    service.clearCache();

    assertThat(service.isFullscreenEnabled()).isFalse();
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
