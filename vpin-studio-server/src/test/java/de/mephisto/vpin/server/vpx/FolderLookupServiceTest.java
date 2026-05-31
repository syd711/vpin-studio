package de.mephisto.vpin.server.vpx;

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
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getMameFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);

    File result = folderLookupService.getAltSoundFolder(game, "myrom");

    assertThat(result.getAbsolutePath()).contains("altsound");
    assertThat(result.getName()).isEqualTo("myrom");
  }

  // ---- getAltColorFolder ----

  @Test
  void getAltColorFolder_legacyLayout_usesVPinMameAltColorFolder() {
    GameEmulator emulator = mock(GameEmulator.class);
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
    GameEmulator emulator = mock(GameEmulator.class);
    File nvRamFolder = tempDir.resolve("nvram").toFile();
    when(vPinMameService.getNvRamFolder()).thenReturn(nvRamFolder);
    Game game = mockGame(emulator);

    File result = folderLookupService.getNvRamFolder(game);

    assertThat(result).isEqualTo(nvRamFolder);
  }

  @Test
  void getNvRamFolder_legacyLayout_fallsBackToMameFolderWhenNvRamFolderNull() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(vPinMameService.getNvRamFolder()).thenReturn(null);
    when(emulator.getMameFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);

    File result = folderLookupService.getNvRamFolder(game);

    assertThat(result.getName()).isEqualTo("nvram");
  }

  // ---- getMusicFolder ----

  @Test
  void getMusicFolder_legacyLayout_returnsInstallationFolderMusic() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);

    File result = folderLookupService.getMusicFolder(game);

    assertThat(result.getName()).isEqualTo("Music");
    assertThat(result.getParentFile()).isEqualTo(tempDir.toFile());
  }

  // ---- getGameMusicFolder ----

  @Test
  void getGameMusicFolder_noAssets_noRom_returnsMusicRoot() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn(null);
    when(game.getRom()).thenReturn(null);

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result).isEqualTo(folderLookupService.getMusicFolder(game));
  }

  @Test
  void getGameMusicFolder_noAssets_withRom_returnsRomSubfolder() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("");
    when(game.getRom()).thenReturn("myrom");

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result.getName()).isEqualTo("myrom");
  }

  @Test
  void getGameMusicFolder_assetsAtRoot_returnsMusicRoot() {
    GameEmulator emulator = mock(GameEmulator.class);
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
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("MFDOOM/Attract*.mp3|MFDOOM/intro.mp3");
    when(game.getRom()).thenReturn("myrom");

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result.getName()).isEqualTo("MFDOOM");
  }

  @Test
  void getGameMusicFolder_multipleFolders_prefersRomMatch() {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(tempDir.toFile());
    Game game = mockGame(emulator);
    when(game.getAssets()).thenReturn("myrom/track1.mp3|other/track2.mp3");
    when(game.getRom()).thenReturn("myrom");

    File result = folderLookupService.getGameMusicFolder(game);

    assertThat(result.getName()).isEqualTo("myrom");
  }

  @Test
  void getGameMusicFolder_multipleFolders_noRomMatch_picksDeepest() {
    GameEmulator emulator = mock(GameEmulator.class);
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
    GameEmulator emulator = mock(GameEmulator.class);
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
    GameEmulator emulator = mock(GameEmulator.class);
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
    GameEmulator emulator = mock(GameEmulator.class);
    File romFolder = tempDir.toFile();
    when(vPinMameService.getRomsFolder()).thenReturn(romFolder);
    Game game = mockGame(emulator);
    when(game.getRom()).thenReturn("");

    File result = folderLookupService.getRomFile(game);

    assertThat(result).isNull();
  }

  private Game mockGame(GameEmulator emulator) {
    Game game = mock(Game.class);
    when(game.getEmulator()).thenReturn(emulator);
    return game;
  }
}
