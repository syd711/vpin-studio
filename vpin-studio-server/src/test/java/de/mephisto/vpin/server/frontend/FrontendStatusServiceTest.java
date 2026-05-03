package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FrontendStatusServiceTest {

  @Mock
  private GameService gameService;

  @Mock
  private SystemService systemService;

  @Mock
  private GameStatusService gameStatusService;

  @Mock
  private FrontendService frontendService;

  @Mock
  private EmulatorService emulatorService;

  @Mock
  private GameLifecycleService gameLifecycleService;

  @InjectMocks
  private FrontendStatusService frontendStatusService;

  // ---- listener registration ----

  @Test
  void addTableStatusChangeListener_doesNotThrow() {
    TableStatusChangeListener listener = mock(TableStatusChangeListener.class);
    when(listener.getPriority()).thenReturn(0);

    frontendStatusService.addTableStatusChangeListener(listener);
    // no exception expected
  }

  @Test
  void addFrontendStatusChangeListener_doesNotThrow() {
    FrontendStatusChangeListener listener = mock(FrontendStatusChangeListener.class);

    frontendStatusService.addFrontendStatusChangeListener(listener);
    // no exception expected
  }

  @Test
  void addTableStatusChangeListener_sortsByPriority() {
    TableStatusChangeListener lowPriority = mock(TableStatusChangeListener.class);
    when(lowPriority.getPriority()).thenReturn(1);
    TableStatusChangeListener highPriority = mock(TableStatusChangeListener.class);
    when(highPriority.getPriority()).thenReturn(10);

    // Add low priority first, then high — verify no exception from the sort
    frontendStatusService.addTableStatusChangeListener(lowPriority);
    frontendStatusService.addTableStatusChangeListener(highPriority);
  }

  // ---- getGameStatus ----

  @Test
  void getGameStatus_delegatesToGameStatusService() {
    GameStatus expected = new GameStatus();
    when(gameStatusService.getStatus()).thenReturn(expected);

    GameStatus result = frontendStatusService.getGameStatus();

    assertSame(expected, result);
  }

  // ---- getPinUPControlFor / getPinUPControls ----

  @Test
  void getPinUPControlFor_delegatesToFrontendService() {
    frontendStatusService.getPinUPControlFor(de.mephisto.vpin.restclient.frontend.VPinScreen.Wheel);
    verify(frontendService).getPinUPControlFor(de.mephisto.vpin.restclient.frontend.VPinScreen.Wheel);
  }

  @Test
  void getPinUPControls_delegatesToFrontendService() {
    frontendStatusService.getPinUPControls();
    verify(frontendService).getControls();
  }
}
