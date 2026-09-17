package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
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
public class FolderLookupServiceTest {

  @Mock
  private SystemService systemService;
  @Mock
  private VPinMameService vPinMameService;

  @InjectMocks
  private FolderLookupService folderLookupService;

  @TempDir
  Path tempDir;

  // ---- getAltSoundFolder ----

  @Test
  void getAltSoundFolder_legacyLayout_usesEmulatorMameFolder() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getMameFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);

    File result = folderLookupService.getAltSoundFolder(game, "myrom");

    assertThat(result.getAbsolutePath()).contains("altsound");
    assertThat(result.getName()).isEqualTo("myrom");
  }

  // ---- getAltColorFolder ----

  @Test
  void getAltColorFolder_legacyLayout_usesVPinMameAltColorFolder() {
    GameEmulator emulator = legacyEmulator();
    File altColorRoot = tempDir.resolve("altcolor").toFile();
    when(vPinMameService.getAltColorFolder()).thenReturn(altColorRoot);
    Game game = mockGame(emulator);

    File result = folderLookupService.getAltColorFolder(game, "myrom");

    assertThat(result.getParentFile()).isEqualTo(altColorRoot);
    assertThat(result.getName()).isEqualTo("myrom");
  }

  // ---- getNvRamFolder ----

  @Test
  void getNvRamFolder_legacyLayout_usesVPinMameNvRamFolder() {
    GameEmulator emulator = legacyEmulator();
    File nvRamFolder = tempDir.resolve("nvram").toFile();
    when(vPinMameService.getNvRamFolder()).thenReturn(nvRamFolder);
    Game game = mockGame(emulator);

    File result = folderLookupService.getNvRamFolder(game);

    assertThat(result).isEqualTo(nvRamFolder);
  }

  @Test
  void getNvRamFolder_legacyLayout_fallsBackToMameFolderWhenNvRamFolderNull() {
    GameEmulator emulator = legacyEmulator();
    when(vPinMameService.getNvRamFolder()).thenReturn(null);
    when(emulator.getMameFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);

    File result = folderLookupService.getNvRamFolder(game);

    assertThat(result.getName()).isEqualTo("nvram");
  }

  // ---- getMusicFolder ----

  @Test
  void getMusicFolder_legacyLayout_returnsInstallationFolderMusic() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);

    File result = folderLookupService.getMusicFolder(game);

    assertThat(result.getName()).isEqualTo("Music");
    assertThat(result.getParentFile()).isEqualTo(tempDir.toFile());
  }

  // ---- getGameMusicFolder ----

  @Test
  void getGameMusicFolder_noAssets_noRom_returnsMusicRoot() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn(null);
    when(game.getRom()).thenReturn(null);

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result).isEqualTo(folderLookupService.getMusicFolder(game));
  }

  @Test
  void getGameMusicFolder_noAssets_withRom_returnsRomSubfolder() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("");
    when(game.getRom()).thenReturn("myrom");

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result.getName()).isEqualTo("myrom");
  }

  @Test
  void getGameMusicFolder_assetsAtRoot_returnsMusicRoot() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("intro.mp3|theme.mp3");
    when(game.getRom()).thenReturn("myrom");

    File musicRoot = folderLookupService.getMusicFolder(game);
    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result).isEqualTo(musicRoot);
  }

  @Test
  void getGameMusicFolder_singleSubfolder_usesThatSubfolder() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("MFDOOM/Attract*.mp3|MFDOOM/intro.mp3");
    when(game.getRom()).thenReturn("myrom");

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result.getName()).isEqualTo("MFDOOM");
  }

  @Test
  void getGameMusicFolder_multipleFolders_prefersRomMatch() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("myrom/track1.mp3|other/track2.mp3");
    when(game.getRom()).thenReturn("myrom");

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result.getName()).isEqualTo("myrom");
  }

  @Test
  void getGameMusicFolder_multipleFolders_noRomMatch_picksDeepest() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("a/b/c/track.mp3|a/track.mp3");
    when(game.getRom()).thenReturn("unrelated");

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result.getAbsolutePath()).contains("a");
    // deepest path is a/b/c
    assertThat(result.getAbsolutePath()).contains("c");
  }

  // ---- getHighscoreTextFile ----

  @Test
  void getHighscoreTextFile_withHsFileName_returnsFile() {
    GameEmulator emulator = legacyEmulator();
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getHsFileName()).thenReturn("scores.txt");

    File result = folderLookupService.getHighscoreTextFile(game);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("scores.txt");
  }

  @Test
  void getHighscoreTextFile_withNoHsFileName_returnsNull() {
    Game game = mock(Game.class);
    when(game.getHsFileName()).thenReturn(null);

    File result = folderLookupService.getHighscoreTextFile(game);

    assertThat(result).isNull();
  }

  // ---- getRomFile ----

  @Test
  void getRomFile_withRomAndExistingFolder_returnsRomZip() {
    GameEmulator emulator = legacyEmulator();
    File romFolder = tempDir.toFile();
    when(vPinMameService.getRomsFolder()).thenReturn(romFolder);
    Game game = mockGame(emulator);
    when(game.getRom()).thenReturn("myrom");

    File result = folderLookupService.getRomFile(game);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("myrom.zip");
  }

  @Test
  void getRomFile_withNoRom_returnsNull() {
    GameEmulator emulator = legacyEmulator();
    File romFolder = tempDir.toFile();
    when(vPinMameService.getRomsFolder()).thenReturn(romFolder);
    Game game = mockGame(emulator);
    when(game.getRom()).thenReturn("");

    File result = folderLookupService.getRomFile(game);

    assertThat(result).isNull();
  }

  // ---- standalone layout: PinMAME files next to each table ----

  @Test
  void isPreferLegacyFileStructure_dependsOnPlatformAndVPinMameFolder() {
    GameEmulator standalone = mock(GameEmulator.class);
    assertThat(FolderLookupService.isPreferLegacyFileStructure(standalone, true)).isTrue();
    assertThat(FolderLookupService.isPreferLegacyFileStructure(standalone, false)).isFalse();
    assertThat(FolderLookupService.isPreferLegacyFileStructure(legacyEmulator(), false)).isTrue();
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void standaloneLayout_pinmameFoldersAreNextToTheTable() {
    File tableFolder = tempDir.resolve("Twister (1996)").toFile();
    File installFolder = tempDir.resolve("vpx").toFile();
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(installFolder);
    Game game = mockGame(emulator);
    when(game.getGameFolder()).thenReturn(tableFolder);

    assertThat(folderLookupService.getRomFolder(game)).isEqualTo(new File(tableFolder, "pinmame/roms"));
    assertThat(folderLookupService.getNvRamFolder(game)).isEqualTo(new File(tableFolder, "pinmame/nvram"));
    assertThat(folderLookupService.getMusicFolder(game)).isEqualTo(new File(tableFolder, "music"));
    assertThat(folderLookupService.getUserFolder(game)).isEqualTo(new File(tableFolder, "user"));
    // the core scripts ship with VPX
    assertThat(folderLookupService.getScriptsFolder(game)).isEqualTo(new File(installFolder, "scripts"));
    verifyNoInteractions(vPinMameService);
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void standaloneLayout_nvRamFileIsFoundWithoutVPinMameFolder() throws Exception {
    File nvramFolder = tempDir.resolve("Twister (1996)/pinmame/nvram").toFile();
    nvramFolder.mkdirs();
    File nvram = new File(nvramFolder, "twst_405.nv");
    nvram.createNewFile();
    Game game = mockGame(mock(GameEmulator.class));
    when(game.getGameFolder()).thenReturn(nvramFolder.getParentFile().getParentFile());
    when(game.getRom()).thenReturn("twst_405");

    assertThat(folderLookupService.getNvRamFile(game)).isEqualTo(nvram);
  }

  @Test
  @DisabledOnOs(OS.WINDOWS)
  void standaloneLayout_romFilesAreFoundRegardlessOfCase() throws Exception {
    // the table script says cGameName = "SS_15", PinMAME writes lowercase file names
    File tableFolder = tempDir.resolve("Scared Stiff").toFile();
    File nvram = new File(tableFolder, "pinmame/nvram/ss_15.nv");
    File rom = new File(tableFolder, "pinmame/roms/ss_15.zip");
    nvram.getParentFile().mkdirs();
    rom.getParentFile().mkdirs();
    nvram.createNewFile();
    rom.createNewFile();
    Game game = mockGame(mock(GameEmulator.class));
    when(game.getGameFolder()).thenReturn(tableFolder);
    when(game.getRom()).thenReturn("SS_15");

    assertThat(folderLookupService.getNvRamFile(game)).isEqualTo(nvram);
    assertThat(folderLookupService.getRomFile(game)).isEqualTo(rom);
  }

  @Test
  void findIgnoreCase_prefersExactNameAndFallsBackToGivenName() throws Exception {
    File exact = tempDir.resolve("ss_15.nv").toFile();
    exact.createNewFile();

    assertThat(FolderLookupService.findIgnoreCase(tempDir.toFile(), "ss_15.nv")).isEqualTo(exact);
    assertThat(FolderLookupService.findIgnoreCase(tempDir.toFile(), "missing.nv")).isEqualTo(tempDir.resolve("missing.nv").toFile());
    assertThat(FolderLookupService.findIgnoreCase(tempDir.resolve("nofolder").toFile(), "x.nv")).doesNotExist();
  }

  /**
   * An emulator with a VPinMAME folder, which keeps the shared legacy layout on every platform.
   */
  private GameEmulator legacyEmulator() {
    GameEmulator emulator = mock(GameEmulator.class);
    // not consulted on Windows, where the legacy layout is always used
    lenient().when(emulator.getMameDirectory()).thenReturn(tempDir.resolve("VPinMAME").toString());
    return emulator;
  }

  private Game mockGame(GameEmulator emulator) {
    Game game = mock(Game.class);
    when(game.getEmulator()).thenReturn(emulator);
    return game;
  }
}
