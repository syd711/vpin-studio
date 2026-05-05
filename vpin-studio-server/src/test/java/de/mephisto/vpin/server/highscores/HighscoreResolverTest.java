package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.parsing.ini.IniHighscoreAdapters;
import de.mephisto.vpin.server.highscores.parsing.text.TextHighscoreAdapters;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegFile;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HighscoreResolverTest {

  @Mock
  private SystemService systemService;

  @Mock
  private TextHighscoreAdapters textHighscoreAdapters;

  @Mock
  private IniHighscoreAdapters initHighscoreAdapters;

  @Mock
  private VPRegService vpRegService;

  @Mock
  private FolderLookupService folderLookupService;

  @InjectMocks
  private HighscoreResolver resolver;

  // ---- getHighscoreFile: null type ----

  @Test
  void getHighscoreFile_returnsNull_whenHighscoreTypeIsNull() {
    Game game = mock(Game.class);
    when(game.getHighscoreType()).thenReturn(null);

    assertNull(resolver.getHighscoreFile(game));
  }

  // ---- getHighscoreFile: EM type ----

  @Test
  void getHighscoreFile_delegatesToFolderLookup_forEMType() {
    Game game = mock(Game.class);
    File expected = mock(File.class);
    when(game.getHighscoreType()).thenReturn(HighscoreType.EM);
    when(folderLookupService.getHighscoreTextFile(game)).thenReturn(expected);

    File result = resolver.getHighscoreFile(game);

    assertSame(expected, result);
    verify(folderLookupService).getHighscoreTextFile(game);
  }

  // ---- getHighscoreFile: VPReg type ----

  @Test
  void getHighscoreFile_returnsVPRegFile_forVPRegType() {
    Game game = mock(Game.class);
    File expected = mock(File.class);
    VPRegFile vpRegFile = mock(VPRegFile.class);
    when(game.getHighscoreType()).thenReturn(HighscoreType.VPReg);
    when(folderLookupService.getVPRegFileForGame(game)).thenReturn(vpRegFile);
    when(vpRegFile.getFile()).thenReturn(expected);

    File result = resolver.getHighscoreFile(game);

    assertSame(expected, result);
  }

  // ---- getHighscoreFile: NVRam type ----

  @Test
  void getHighscoreFile_returnsNvRamFile_forNvRamType() {
    Game game = mock(Game.class);
    File expected = mock(File.class);
    when(game.getHighscoreType()).thenReturn(HighscoreType.NVRam);
    when(folderLookupService.getNvRamFile(game)).thenReturn(expected);

    File result = resolver.getHighscoreFile(game);

    assertSame(expected, result);
  }

  // ---- getHighscoreFile: Ini type ----

  @Test
  void getHighscoreFile_returnsIniFile_forIniType_withRom() {
    File parentDir = new File(System.getProperty("java.io.tmpdir"));
    File gameFile = new File(parentDir, "mytable.vpx");

    Game game = mock(Game.class);
    when(game.getHighscoreType()).thenReturn(HighscoreType.Ini);
    when(game.getRom()).thenReturn("mygame");
    when(game.getGameFile()).thenReturn(gameFile);

    File result = resolver.getHighscoreFile(game);

    assertNotNull(result);
    assertTrue(result.getName().startsWith("mygame") && result.getName().endsWith("_glf.ini"),
        "Expected name like mygame_glf.ini but got: " + result.getName());
  }

  // ---- getHighscoreIniFile ----

  @Test
  void getHighscoreIniFile_usesRomName_whenOnlyRomPresent() {
    File parentDir = new File(System.getProperty("java.io.tmpdir"));
    File gameFile = new File(parentDir, "table.vpx");

    Game game = mock(Game.class);
    when(game.getRom()).thenReturn("mygame");
    when(game.getTableName()).thenReturn("");
    when(game.getGameFile()).thenReturn(gameFile);

    File result = resolver.getHighscoreIniFile(game);

    assertNotNull(result);
    assertEquals("mygame_glf.ini", result.getName());
    assertEquals(parentDir, result.getParentFile());
  }

  @Test
  void getHighscoreIniFile_usesTableName_whenRomIsEmpty() {
    File parentDir = new File(System.getProperty("java.io.tmpdir"));
    File gameFile = new File(parentDir, "table.vpx");

    Game game = mock(Game.class);
    when(game.getRom()).thenReturn("");
    when(game.getTableName()).thenReturn("Funhouse");
    when(game.getGameFile()).thenReturn(gameFile);

    File result = resolver.getHighscoreIniFile(game);

    assertNotNull(result);
    assertEquals("Funhouse_glf.ini", result.getName());
  }

  @Test
  void getHighscoreIniFile_returnsNull_whenBothRomAndTableNameAreEmpty() {
    Game game = mock(Game.class);
    when(game.getRom()).thenReturn("");
    when(game.getTableName()).thenReturn("");

    File result = resolver.getHighscoreIniFile(game);

    assertNull(result);
  }

  // ---- getNvRamFile ----

  @Test
  void getNvRamFile_delegatesToFolderLookupService() {
    Game game = mock(Game.class);
    File expected = mock(File.class);
    when(folderLookupService.getNvRamFile(game)).thenReturn(expected);

    File result = resolver.getNvRamFile(game);

    assertSame(expected, result);
  }
}
