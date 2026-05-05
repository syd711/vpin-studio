package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FuturePinballServiceTest {

  @Mock
  private FPCommandLineService fpCommandLineService;

  @InjectMocks
  private FuturePinballService futurePinballService;

  // ---- play ----

  @Test
  void play_launchesGame_whenGameIsNotNull() {
    Game game = mock(Game.class);
    when(fpCommandLineService.execute(game, null)).thenReturn(true);

    boolean result = futurePinballService.play(game, null);

    assertTrue(result);
    verify(fpCommandLineService).execute(game, null);
  }

  @Test
  void play_launchesFrontend_whenGameIsNull() {
    when(fpCommandLineService.launch()).thenReturn(true);

    boolean result = futurePinballService.play(null, null);

    assertTrue(result);
    verify(fpCommandLineService).launch();
  }

  @Test
  void play_passesAltExe_toCommandLineService() {
    Game game = mock(Game.class);
    when(fpCommandLineService.execute(game, "BAM.exe")).thenReturn(false);

    boolean result = futurePinballService.play(game, "BAM.exe");

    assertFalse(result);
    verify(fpCommandLineService).execute(game, "BAM.exe");
  }

  // ---- installLibrary ----

  @Test
  void installLibrary_doesNothing_whenInstallationFolderIsNull() throws IOException {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(null);

    UploadDescriptor descriptor = new UploadDescriptor();
    UploaderAnalysis analysis = mock(UploaderAnalysis.class);

    // must not throw
    futurePinballService.installLibrary(descriptor, emulator, File.createTempFile("test", ".zip"), analysis);

    verify(analysis, never()).getFileNamesForAssetType(any());
  }

  @Test
  void installLibrary_doesNothing_whenInstallationFolderDoesNotExist() throws IOException {
    GameEmulator emulator = mock(GameEmulator.class);
    when(emulator.getInstallationFolder()).thenReturn(new File("/nonexistent/path"));

    UploadDescriptor descriptor = new UploadDescriptor();
    UploaderAnalysis analysis = mock(UploaderAnalysis.class);

    futurePinballService.installLibrary(descriptor, emulator, File.createTempFile("test", ".zip"), analysis);

    verify(analysis, never()).getFileNamesForAssetType(any());
  }

  // ---- installModelPackage ----

  @Test
  void installModelPackage_doesNothing_whenGameIsNull() throws IOException {
    File tempFile = File.createTempFile("model", ".zip");
    UploaderAnalysis analysis = mock(UploaderAnalysis.class);

    // must not throw
    futurePinballService.installModelPackage(tempFile, analysis, null);

    tempFile.delete();
  }

  @Test
  void installModelPackage_copiesFileToGameFolder(@TempDir File tempDir) throws IOException {
    File sourceFile = new File(tempDir, "source.zip");
    Files.writeString(sourceFile.toPath(), "model data");

    File gameDir = new File(tempDir, "tables");
    gameDir.mkdirs();
    File gameFile = new File(gameDir, "MyTable.fpt");
    gameFile.createNewFile();

    Game game = mock(Game.class);
    when(game.getGameFile()).thenReturn(gameFile);
    when(game.getGameFileName()).thenReturn("MyTable.fpt");

    UploaderAnalysis analysis = mock(UploaderAnalysis.class);

    futurePinballService.installModelPackage(sourceFile, analysis, game);

    File expectedOutput = new File(gameDir, "MyTable.zip");
    assertTrue(expectedOutput.exists(), "Model package should be copied to game folder");
  }
}
