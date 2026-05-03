package de.mephisto.vpin.server.hooks;

import de.mephisto.vpin.restclient.hooks.HookCommand;
import de.mephisto.vpin.restclient.hooks.HookList;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HooksServiceTest {

  @Mock
  private GameService gameService;

  @InjectMocks
  private HooksService hooksService;

  private String originalResources;

  @BeforeEach
  void saveOriginalResources() {
    originalResources = SystemService.RESOURCES;
  }

  @AfterEach
  void restoreResources() {
    SystemService.RESOURCES = originalResources;
  }

  // ---- getHooks: folder does not exist ----

  @Test
  void getHooks_returnsEmptyList_whenHooksFolderDoesNotExist(@TempDir Path tempDir) {
    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/"; // hooks subfolder does not exist

    HookList result = hooksService.getHooks();

    assertNotNull(result);
    assertTrue(result.getHooks().isEmpty());
  }

  // ---- getHooks: folder exists with files ----

  @Test
  void getHooks_includesExeAndBatAndVbsFiles(@TempDir Path tempDir) throws IOException {
    Path hooksDir = tempDir.resolve("hooks");
    Files.createDirectories(hooksDir);
    Files.createFile(hooksDir.resolve("script.bat"));
    Files.createFile(hooksDir.resolve("launcher.exe"));
    Files.createFile(hooksDir.resolve("macro.vbs"));
    Files.createFile(hooksDir.resolve("readme.txt")); // should be excluded

    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/";

    HookList result = hooksService.getHooks();

    assertEquals(3, result.getHooks().size());
    assertTrue(result.getHooks().contains("script.bat"));
    assertTrue(result.getHooks().contains("launcher.exe"));
    assertTrue(result.getHooks().contains("macro.vbs"));
    assertFalse(result.getHooks().contains("readme.txt"));
  }

  @Test
  void getHooks_excludesNonHookExtensions(@TempDir Path tempDir) throws IOException {
    Path hooksDir = tempDir.resolve("hooks");
    Files.createDirectories(hooksDir);
    Files.createFile(hooksDir.resolve("config.ini"));
    Files.createFile(hooksDir.resolve("data.json"));

    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/";

    HookList result = hooksService.getHooks();

    assertTrue(result.getHooks().isEmpty());
  }

  // ---- execute ----

  @Test
  void execute_returnsCmd_evenWhenGameNotFound() {
    HookCommand cmd = new HookCommand();
    cmd.setName("test.bat");
    cmd.setGameId(99);
    when(gameService.getGame(99)).thenReturn(null);

    HookCommand result = hooksService.execute(cmd);

    assertSame(cmd, result);
  }

  @Test
  void execute_returnsCmd_whenGameFound() {
    // game info is added to local commands list for SystemCommandExecutor, not cmd.getCommands()
    Game game = mock(Game.class);
    when(game.getGameFileName()).thenReturn("funhouse.vpx");
    when(game.getRom()).thenReturn("fh_l9");
    when(gameService.getGame(1)).thenReturn(game);

    HookCommand cmd = new HookCommand();
    cmd.setName("test.bat");
    cmd.setGameId(1);

    HookCommand result = hooksService.execute(cmd);

    assertSame(cmd, result);
  }
}
