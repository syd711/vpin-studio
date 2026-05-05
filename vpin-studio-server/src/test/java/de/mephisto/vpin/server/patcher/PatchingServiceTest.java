package de.mephisto.vpin.server.patcher;

import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatchingServiceTest {

  @InjectMocks
  private PatchingService patchingService;

  @Test
  void patch_returnsErrorMessage_whenJptchExeNotAvailable() {
    // jptch.exe is absent; ProcessBuilder fails → IOException is caught → returns "Patching failed: ..."
    Game game = mock(Game.class);
    when(game.getGameFile()).thenReturn(new File("test.vpx"));

    String result = patchingService.patch(game, new File("patch.dif"), new File("target.vpx"));

    assertThat(result).isNotNull();
    assertThat(result).startsWith("Patching failed:");
  }

  @Test
  void patch_returnsNullOrError_dependingOnExecutionResult() {
    Game game = mock(Game.class);
    when(game.getGameFile()).thenReturn(new File("nonexistent.vpx"));

    String result = patchingService.patch(game, new File("diff.dif"), new File("out.vpx"));

    if (result != null) {
      assertThat(result).startsWith("Patching failed:");
    }
  }
}
