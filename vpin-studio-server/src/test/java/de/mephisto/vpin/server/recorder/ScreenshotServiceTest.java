package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ScreenshotServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private RecorderService recorderService;

  @Mock
  private ScreenDmdRecorder screenDmdRecorder;

  @Mock
  private GameService gameService;

  @Mock
  private GameStatusService gameStatusService;

  @Mock
  private SystemService systemService;

  @Mock
  private PlayerService playerService;

  @Mock
  private HighscoreService highscoreService;

  @Mock
  private ScreenPreviewService screenPreviewService;

  @InjectMocks
  private ScreenshotService screenshotService;

  private String originalResources;

  @BeforeEach
  void saveResources() {
    originalResources = SystemService.RESOURCES;
  }

  @AfterEach
  void restoreResources() {
    SystemService.RESOURCES = originalResources;
  }

  @Test
  void getScreenshotFile_returnsFileWithUuidName(@TempDir Path tempDir) {
    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/";
    String uuid = UUID.randomUUID().toString();

    File result = screenshotService.getScreenshotFile(uuid);

    assertNotNull(result);
    assertEquals(uuid + ".png", result.getName());
    assertTrue(result.getParentFile().exists());
    assertTrue(result.getParentFile().getName().equals("screenshots"));
  }

  @Test
  void getScreenshotFile_createsScreenshotsFolder_whenMissing(@TempDir Path tempDir) {
    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/";
    String uuid = "test-uuid-123";

    File screenshotFolder = new File(tempDir.toFile(), "screenshots");
    assertFalse(screenshotFolder.exists());

    screenshotService.getScreenshotFile(uuid);

    assertTrue(screenshotFolder.exists());
  }

  @Test
  void getScreenshotFile_usesNullLiteral_whenUuidAndLastIdAreNull(@TempDir Path tempDir) {
    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/";

    File result = screenshotService.getScreenshotFile(null);

    assertNotNull(result);
    assertEquals("null.png", result.getName());
  }
}
