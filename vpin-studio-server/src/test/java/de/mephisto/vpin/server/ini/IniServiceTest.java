package de.mephisto.vpin.server.ini;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.ini.IniRepresentation;
import de.mephisto.vpin.restclient.ini.IniSectionRepresentation;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IniServiceTest {

  @Mock
  private GameService gameService;

  @Mock
  private GameLifecycleService gameLifecycleService;

  @InjectMocks
  private IniService iniService;

  @Test
  void delete_returnsFalse_whenGameNotFound() {
    when(gameService.getGame(99)).thenReturn(null);

    boolean result = iniService.delete(99);

    assertFalse(result);
    verifyNoInteractions(gameLifecycleService);
  }

  @Test
  void delete_returnsFalse_whenIniFileDoesNotExist(@TempDir Path tempDir) {
    Game game = mock(Game.class);
    when(game.getIniFile()).thenReturn(tempDir.resolve("nonexistent.ini").toFile());
    when(gameService.getGame(1)).thenReturn(game);

    boolean result = iniService.delete(1);

    assertFalse(result);
    verifyNoInteractions(gameLifecycleService);
  }

  @Test
  void delete_returnsTrue_andNotifiesLifecycle_whenIniFileExists(@TempDir Path tempDir) throws IOException {
    Path iniPath = tempDir.resolve("table.ini");
    Files.createFile(iniPath);

    Game game = mock(Game.class);
    when(game.getId()).thenReturn(1);
    when(game.getIniFile()).thenReturn(iniPath.toFile());
    when(gameService.getGame(1)).thenReturn(game);

    boolean result = iniService.delete(1);

    assertTrue(result);
    assertFalse(iniPath.toFile().exists());
    verify(gameLifecycleService).notifyGameAssetsChanged(eq(1), eq(AssetType.INI), isNull());
  }

  @Test
  void getIniFile_returnsRepresentationWithNoSections_whenIniFileDoesNotExist(@TempDir Path tempDir) throws Exception {
    Game game = mock(Game.class);
    when(game.getIniFile()).thenReturn(tempDir.resolve("missing.ini").toFile());
    when(gameService.getGame(2)).thenReturn(game);

    IniRepresentation result = iniService.getIniFile(2);

    assertNotNull(result);
    assertTrue(result.getSections().isEmpty());
    assertNull(result.getFileName());
  }

  @Test
  void getIniFile_returnsParsedSections_whenIniFileExists(@TempDir Path tempDir) throws Exception {
    Path iniPath = tempDir.resolve("table.ini");
    String iniContent = "[Settings]\nvolume=80\nmute=false\n";
    Files.write(iniPath, iniContent.getBytes(StandardCharsets.UTF_8));

    Game game = mock(Game.class);
    when(game.getIniFile()).thenReturn(iniPath.toFile());
    when(gameService.getGame(3)).thenReturn(game);

    IniRepresentation result = iniService.getIniFile(3);

    assertNotNull(result);
    assertEquals("table.ini", result.getFileName());
    assertFalse(result.getSections().isEmpty());
    IniSectionRepresentation section = result.getSections().stream()
        .filter(s -> "Settings".equals(s.getName()))
        .findFirst()
        .orElse(null);
    assertNotNull(section);
    assertEquals("80", String.valueOf(section.getValues().get("volume")));
  }

  @Test
  void save_returnsFalse_whenIniFileDoesNotExist(@TempDir Path tempDir) throws Exception {
    Game game = mock(Game.class);
    when(game.getIniFile()).thenReturn(tempDir.resolve("missing.ini").toFile());
    when(gameService.getGame(4)).thenReturn(game);

    boolean result = iniService.save(4, new IniRepresentation());

    assertFalse(result);
    verifyNoInteractions(gameLifecycleService);
  }
}
