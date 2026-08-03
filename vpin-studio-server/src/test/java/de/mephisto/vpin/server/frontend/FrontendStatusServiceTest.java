package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.games.TableStatusChangedOrigin;
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

  // ---- listener isolation: one failing listener must not block the others ----

  @Test
  void notifyTableStatusChange_launched_failingListenerDoesNotBlockLaterListeners() {
    TableStatusChangeListener failing = mock(TableStatusChangeListener.class);
    doThrow(new RuntimeException("boom")).when(failing).tableLaunched(any());
    TableStatusChangeListener healthy = mock(TableStatusChangeListener.class);

    frontendStatusService.addTableStatusChangeListener(failing);
    frontendStatusService.addTableStatusChangeListener(healthy);

    Game game = mock(Game.class);
    frontendStatusService.notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_POPPER);

    verify(healthy).tableLaunched(any());
  }

  @Test
  void notifyTableStatusChange_exited_failingListenerDoesNotBlockLaterListeners() {
    TableStatusChangeListener failing = mock(TableStatusChangeListener.class);
    doThrow(new RuntimeException("boom")).when(failing).tableExited(any());
    TableStatusChangeListener healthy = mock(TableStatusChangeListener.class);

    frontendStatusService.addTableStatusChangeListener(failing);
    frontendStatusService.addTableStatusChangeListener(healthy);

    Game game = mock(Game.class);
    frontendStatusService.notifyTableStatusChange(game, false, TableStatusChangedOrigin.ORIGIN_POPPER);

    verify(healthy).tableExited(any());
  }

  @Test
  void notifyFrontendLaunch_failingListenerDoesNotBlockLaterListeners() {
    FrontendStatusChangeListener failing = mock(FrontendStatusChangeListener.class);
    doThrow(new RuntimeException("boom")).when(failing).frontendLaunched();
    FrontendStatusChangeListener healthy = mock(FrontendStatusChangeListener.class);

    frontendStatusService.addFrontendStatusChangeListener(failing);
    frontendStatusService.addFrontendStatusChangeListener(healthy);

    frontendStatusService.notifyFrontendLaunch();

    verify(healthy).frontendLaunched();
  }

  @Test
  void notifyFrontendExit_failingListenerDoesNotBlockLaterListeners() {
    FrontendStatusChangeListener failing = mock(FrontendStatusChangeListener.class);
    doThrow(new RuntimeException("boom")).when(failing).frontendExited();
    FrontendStatusChangeListener healthy = mock(FrontendStatusChangeListener.class);

    frontendStatusService.addFrontendStatusChangeListener(failing);
    frontendStatusService.addFrontendStatusChangeListener(healthy);
    when(gameStatusService.getStatus()).thenReturn(new GameStatus());

    frontendStatusService.notifyFrontendExit();

    verify(healthy).frontendExited();
  }
}
