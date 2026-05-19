package de.mephisto.vpin.server.music;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MusicServiceTest {

  @Mock
  private FolderLookupService folderLookupService;

  @InjectMocks
  private MusicService musicService;

  private Game game;

  @BeforeEach
  void setUp() {
    game = new Game();
  }

  // ---- getMp3Files ----

  @Test
  void getMp3Files_returnsEmpty_whenMusicFolderIsNull() {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(null);

    List<File> result = musicService.getMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMp3Files_returnsEmpty_whenMusicFolderDoesNotExist(@TempDir Path tempDir) {
    File nonExistent = new File(tempDir.toFile(), "nonexistent");
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(nonExistent);
    game.setAssets("intro.mp3");

    List<File> result = musicService.getMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMp3Files_returnsEmpty_whenAssetsIsNull(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets(null);

    List<File> result = musicService.getMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMp3Files_returnsEmpty_whenAssetsIsBlank(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("   ");

    List<File> result = musicService.getMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMp3Files_returnsFile_whenExactAssetExists(@TempDir Path tempDir) throws IOException {
    File mp3 = Files.createFile(tempDir.resolve("intro.mp3")).toFile();
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("intro.mp3");

    List<File> result = musicService.getMp3Files(game);

    assertEquals(1, result.size());
    assertEquals(mp3, result.get(0));
  }

  @Test
  void getMp3Files_skipsNonExistentExactAsset(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("missing.mp3");

    List<File> result = musicService.getMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMp3Files_skipsBogusPureWildcard(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("track.mp3"));
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    // pattern "/.mp3" and "/*.mp3" must be skipped
    game.setAssets("/.mp3|/*.mp3");

    List<File> result = musicService.getMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMp3Files_matchesWildcardPattern(@TempDir Path tempDir) throws IOException {
    Path sub = Files.createDirectory(tempDir.resolve("MFDOOM"));
    Files.createFile(sub.resolve("Attract1.mp3"));
    Files.createFile(sub.resolve("Attract2.mp3"));
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("MFDOOM/Attract*.mp3");

    List<File> result = musicService.getMp3Files(game);

    assertEquals(2, result.size());
  }

  @Test
  void getMp3Files_deduplicatesFiles(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("intro.mp3"));
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    // same asset listed twice
    game.setAssets("intro.mp3|intro.mp3");

    List<File> result = musicService.getMp3Files(game);

    assertEquals(1, result.size());
  }

  // ---- getMissingMp3Files ----

  @Test
  void getMissingMp3Files_returnsEmpty_whenMusicFolderIsNull() {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(null);

    List<String> result = musicService.getMissingMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMissingMp3Files_returnsEmpty_whenAssetsIsEmpty(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("");

    List<String> result = musicService.getMissingMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMissingMp3Files_reportsMissingExactFile(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("missing.mp3");

    List<String> result = musicService.getMissingMp3Files(game);

    assertEquals(1, result.size());
    assertEquals("missing.mp3", result.get(0));
  }

  @Test
  void getMissingMp3Files_doesNotReportExistingFile(@TempDir Path tempDir) throws IOException {
    Files.createFile(tempDir.resolve("present.mp3"));
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("present.mp3");

    List<String> result = musicService.getMissingMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMissingMp3Files_skipsBogusWildcardPatterns(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("/.mp3|/*.mp3");

    List<String> result = musicService.getMissingMp3Files(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void getMissingMp3Files_reportsMissingWildcard(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("MFDOOM/Attract*.mp3");

    List<String> result = musicService.getMissingMp3Files(game);

    assertEquals(1, result.size());
    assertEquals("MFDOOM/Attract*.mp3", result.get(0));
  }

  @Test
  void getMissingMp3Files_doesNotReportMatchedWildcard(@TempDir Path tempDir) throws IOException {
    Path sub = Files.createDirectory(tempDir.resolve("MFDOOM"));
    Files.createFile(sub.resolve("Attract1.mp3"));
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("MFDOOM/Attract*.mp3");

    List<String> result = musicService.getMissingMp3Files(game);

    assertTrue(result.isEmpty());
  }

  // ---- getGameMusicFolder ----

  @Test
  void getGameMusicFolder_delegatesToFolderLookupService(@TempDir Path tempDir) {
    File expected = tempDir.toFile();
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(expected);

    File result = musicService.getGameMusicFolder(game);

    assertEquals(expected, result);
  }

  @Test
  void getGameMusicFolder_returnsNull_whenFolderLookupReturnsNull() {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(null);

    File result = musicService.getGameMusicFolder(game);

    assertNull(result);
  }

  // ---- delete ----

  @Test
  void delete_returnsTrue_whenNoMp3FilesExist(@TempDir Path tempDir) {
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("");

    boolean result = musicService.delete(game);

    assertTrue(result);
  }

  @Test
  void delete_deletesFilesAndReturnsTrue(@TempDir Path tempDir) throws IOException {
    File mp3 = Files.createFile(tempDir.resolve("track.mp3")).toFile();
    when(folderLookupService.getGameMusicFolder(game)).thenReturn(tempDir.toFile());
    game.setAssets("track.mp3");

    boolean result = musicService.delete(game);

    assertTrue(result);
    assertFalse(mp3.exists());
  }
}
