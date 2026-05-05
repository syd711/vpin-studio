package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpinmame.VPinMameRomAliasService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import de.mephisto.vpin.server.vpx.VPXService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TextEditServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private EmulatorService emulatorService;

  @Mock
  private GameService gameService;

  @Mock
  private VPinMameService vPinMameService;

  @Mock
  private VPinMameRomAliasService VPinMameRomAliasService;

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private DOFLinxService dofLinxService;

  @Mock
  private VPXService vpxService;

  @InjectMocks
  private TextEditService textEditService;

  // ---- getText: VPMAliasTxt ----

  @Test
  void getText_delegatesToAliasService_forVPMAliasTxt() throws Exception {
    GameEmulator emulator = mock(GameEmulator.class);
    MonitoredTextFile expected = new MonitoredTextFile();
    MonitoredTextFile request = new MonitoredTextFile();
    request.setvPinFile(VPinFile.VPMAliasTxt);
    request.setEmulatorId(1);

    when(emulatorService.getGameEmulator(1)).thenReturn(emulator);
    when(VPinMameRomAliasService.loadAliasFile(emulator)).thenReturn(expected);

    MonitoredTextFile result = textEditService.getText(request);

    assertSame(expected, result);
    verify(VPinMameRomAliasService).loadAliasFile(emulator);
  }

  // ---- getText: LOCAL_GAME_FILE ----

  @Test
  void getText_throwsException_whenLocalGameFileDoesNotExist() {
    MonitoredTextFile request = new MonitoredTextFile();
    request.setvPinFile(VPinFile.LOCAL_GAME_FILE);
    request.setPath("/nonexistent/path/to/file.txt");

    assertThrows(Exception.class, () -> textEditService.getText(request));
  }

  @Test
  void getText_returnsContent_whenLocalGameFileExists(@TempDir Path tempDir) throws Exception {
    File testFile = tempDir.resolve("test.ini").toFile();
    Files.writeString(testFile.toPath(), "key=value");

    MonitoredTextFile request = new MonitoredTextFile();
    request.setvPinFile(VPinFile.LOCAL_GAME_FILE);
    request.setPath(testFile.getAbsolutePath());

    MonitoredTextFile result = textEditService.getText(request);

    assertEquals("key=value", result.getContent());
    assertEquals(testFile.getAbsolutePath(), result.getPath());
  }

  // ---- save: VPMAliasTxt ----

  @Test
  void save_sortsLinesAndSavesAliasFile_forVPMAliasTxt() throws Exception {
    GameEmulator emulator = mock(GameEmulator.class);
    MonitoredTextFile expected = new MonitoredTextFile();
    MonitoredTextFile request = new MonitoredTextFile();
    request.setvPinFile(VPinFile.VPMAliasTxt);
    request.setEmulatorId(2);
    request.setContent("zebra=1\nalpha=2\nmiddle=3");

    when(emulatorService.getGameEmulator(2)).thenReturn(emulator);
    when(VPinMameRomAliasService.loadAliasFile(emulator)).thenReturn(expected);

    MonitoredTextFile result = textEditService.save(request);

    // The sorted content should have been passed to saveAliasFile
    verify(VPinMameRomAliasService).saveAliasFile(eq(emulator), contains("alpha"));
    assertSame(expected, result);
  }

  // ---- save: LOCAL_GAME_FILE ----

  @Test
  void save_writesContent_forLocalGameFile(@TempDir Path tempDir) throws IOException {
    File testFile = tempDir.resolve("output.txt").toFile();
    Files.writeString(testFile.toPath(), "original");

    MonitoredTextFile request = new MonitoredTextFile();
    request.setvPinFile(VPinFile.LOCAL_GAME_FILE);
    request.setPath(testFile.getAbsolutePath());
    request.setContent("line1\nline2");

    MonitoredTextFile result = textEditService.save(request);

    assertNotNull(result);
    assertTrue(testFile.exists());
  }
}
