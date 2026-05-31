package de.mephisto.vpin.server.res;

import de.mephisto.vpin.restclient.assets.AssetType;
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
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResServiceTest {

  @Mock
  private GameService gameService;
  @Mock
  private GameLifecycleService gameLifecycleService;

  @InjectMocks
  private ResService resService;

  @Test
  void delete_returnsFalse_whenGameNotFound() {
    when(gameService.getGame(1)).thenReturn(null);

    assertFalse(resService.delete(1));
    verifyNoInteractions(gameLifecycleService);
  }

  @Test
  void delete_returnsFalse_whenResFileDoesNotExist() {
    Game game = mock(Game.class);
    File resFile = mock(File.class);
    when(resFile.exists()).thenReturn(false);
    when(game.getResFile()).thenReturn(resFile);
    when(gameService.getGame(1)).thenReturn(game);

    assertFalse(resService.delete(1));
    verifyNoInteractions(gameLifecycleService);
  }

  @Test
  void delete_returnsFalse_whenFileDeletionFails() {
    Game game = mock(Game.class);
    File resFile = mock(File.class);
    when(resFile.exists()).thenReturn(true);
    when(resFile.delete()).thenReturn(false);
    when(game.getResFile()).thenReturn(resFile);
    when(gameService.getGame(5)).thenReturn(game);

    assertFalse(resService.delete(5));
    verifyNoInteractions(gameLifecycleService);
  }

  @Test
  void delete_returnsTrue_andNotifiesLifecycle_whenResFileDeletedSuccessfully(@TempDir Path tempDir) throws IOException {
    File realFile = tempDir.resolve("game.res").toFile();
    realFile.createNewFile();
    assertTrue(realFile.exists());

    Game game = mock(Game.class);
    when(game.getResFile()).thenReturn(realFile);
    when(game.getId()).thenReturn(42);
    when(gameService.getGame(42)).thenReturn(game);

    boolean result = resService.delete(42);

    assertTrue(result);
    assertFalse(realFile.exists());
    verify(gameLifecycleService).notifyGameAssetsChanged(42, AssetType.RES, null);
  }
}
