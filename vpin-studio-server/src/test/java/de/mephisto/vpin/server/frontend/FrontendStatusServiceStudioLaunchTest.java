package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.games.TableStatusChangedOrigin;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests the emulator process monitoring that fires the table exit event for tables launched from the Studio.
 * The scheduler is mocked, so every poll is triggered manually by running the active monitor.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FrontendStatusServiceStudioLaunchTest {

  /**
   * Time to wait to make sure an asynchronous event has NOT been fired.
   */
  private static final int NEGATIVE_WAIT_MS = 300;
  /**
   * Maximum time to wait for an asynchronous event to be fired.
   */
  private static final int POSITIVE_WAIT_MS = 2000;

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

  //created in setUp() after the injection, @InjectMocks would otherwise inject them into the monitor fields
  //of the service (it also picks up fields that already hold a mock, not only @Mock annotated ones)
  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> scheduledFuture;

  @Mock
  private TableStatusChangeListener listener;

  @InjectMocks
  private FrontendStatusService frontendStatusService;

  private final AtomicBoolean emulatorRunning = new AtomicBoolean(false);
  private final AtomicLong now = new AtomicLong(1_000_000L);
  private final GameStatus gameStatus = new GameStatus();

  @BeforeEach
  void setUp() {
    scheduler = mock(ScheduledExecutorService.class);
    scheduledFuture = mock(ScheduledFuture.class);
    frontendStatusService.studioLaunchScheduler = scheduler;
    frontendStatusService.emulatorRunningCheck = emulatorRunning::get;
    frontendStatusService.clock = now::get;
    frontendStatusService.addTableStatusChangeListener(listener);

    doReturn(scheduledFuture).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    when(gameStatusService.getStatus()).thenReturn(gameStatus);
  }

  private Game game(int id, String name) {
    Game game = new Game();
    game.setId(id);
    game.setGameDisplayName(name);
    return game;
  }

  /**
   * Launches the game from the Studio and marks it as active, like GamesResource#play does.
   */
  private Runnable launch(Game game) {
    gameStatus.setGameId(game.getId());
    frontendStatusService.notifyStudioLaunch(game);
    Runnable monitor = frontendStatusService.getActiveStudioLaunchMonitor();
    assertNotNull(monitor, "a Studio launch must start the emulator monitoring");
    return monitor;
  }

  private static TableStatusChangedEvent studioEventFor(Game game) {
    return argThat(e -> e != null && e.getGame() == game && e.getOrigin() == TableStatusChangedOrigin.ORIGIN_STUDIO);
  }

  // ---- launch ----

  @Test
  void notifyStudioLaunch_firesTableLaunchedWithStudioOrigin() {
    Game game = game(1, "Medieval Madness");

    launch(game);

    verify(listener, timeout(POSITIVE_WAIT_MS)).tableLaunched(studioEventFor(game));
  }

  @Test
  void notifyStudioLaunch_schedulesMonitorWithPollInterval() {
    launch(game(1, "Medieval Madness"));

    int interval = FrontendStatusService.STUDIO_LAUNCH_POLL_INTERVAL_SECONDS;
    verify(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq((long) interval), eq((long) interval), eq(TimeUnit.SECONDS));
  }

  @Test
  void notifyStudioLaunch_eventsDisabled_neitherFiresEventsNorMonitors() {
    frontendStatusService.setEventsEnabled(false);

    frontendStatusService.notifyStudioLaunch(game(1, "Medieval Madness"));

    assertNull(frontendStatusService.getActiveStudioLaunchMonitor());
    verifyNoInteractions(scheduler);
    verify(listener, after(NEGATIVE_WAIT_MS).never()).tableLaunched(any());
  }

  // ---- exit detection ----

  @Test
  void monitor_exitsAfterRequiredConsecutiveMisses() {
    Game game = game(1, "Medieval Madness");
    Runnable monitor = launch(game);

    emulatorRunning.set(true);
    monitor.run();

    emulatorRunning.set(false);
    for (int i = 1; i < FrontendStatusService.STUDIO_LAUNCH_REQUIRED_MISSES; i++) {
      monitor.run();
    }
    verify(listener, after(NEGATIVE_WAIT_MS).never()).tableExited(any());

    monitor.run();

    verify(listener, timeout(POSITIVE_WAIT_MS)).tableExited(studioEventFor(game));
    verify(scheduledFuture).cancel(false);
    assertNull(frontendStatusService.getActiveStudioLaunchMonitor());
  }

  @Test
  void monitor_singleMissIsResetWhenEmulatorIsBack() {
    Runnable monitor = launch(game(1, "Medieval Madness"));

    emulatorRunning.set(true);
    monitor.run();
    emulatorRunning.set(false);
    monitor.run();
    emulatorRunning.set(true);
    monitor.run();
    emulatorRunning.set(false);
    monitor.run();

    verify(listener, after(NEGATIVE_WAIT_MS).never()).tableExited(any());
    assertSame(monitor, frontendStatusService.getActiveStudioLaunchMonitor());
  }

  @Test
  void monitor_waitsForEmulatorStartup() {
    Runnable monitor = launch(game(1, "Medieval Madness"));

    //the emulator is still starting, so missing processes must not be counted as exit
    for (int i = 0; i < 10; i++) {
      now.addAndGet(FrontendStatusService.STUDIO_LAUNCH_POLL_INTERVAL_SECONDS * 1000L);
      monitor.run();
    }

    verify(listener, after(NEGATIVE_WAIT_MS).never()).tableExited(any());
    assertSame(monitor, frontendStatusService.getActiveStudioLaunchMonitor());
  }

  @Test
  void monitor_firesExitWhenEmulatorNeverStarts() {
    Game game = game(1, "Medieval Madness");
    Runnable monitor = launch(game);

    now.addAndGet(FrontendStatusService.STUDIO_LAUNCH_STARTUP_TIMEOUT_MS + 1);
    monitor.run();

    verify(listener, timeout(POSITIVE_WAIT_MS)).tableExited(studioEventFor(game));
    assertNull(frontendStatusService.getActiveStudioLaunchMonitor());
  }

  @Test
  void monitor_exceptionInProcessCheckDoesNotEscape() {
    Runnable monitor = launch(game(1, "Medieval Madness"));
    frontendStatusService.emulatorRunningCheck = () -> {
      throw new IllegalStateException("boom");
    };

    //an escaping exception would silently cancel the scheduled task
    assertDoesNotThrow(monitor::run);
    assertSame(monitor, frontendStatusService.getActiveStudioLaunchMonitor());
  }

  @Test
  void monitor_noExitEventWhenNoGameIsActive() {
    Runnable monitor = launch(game(1, "Medieval Madness"));
    emulatorRunning.set(true);
    monitor.run();

    //e.g. the game status has been reset by a frontend exit in the meantime
    gameStatus.setGameId(-1);
    emulatorRunning.set(false);
    for (int i = 0; i < FrontendStatusService.STUDIO_LAUNCH_REQUIRED_MISSES; i++) {
      monitor.run();
    }

    verify(listener, after(NEGATIVE_WAIT_MS).never()).tableExited(any());
  }

  // ---- monitor replacement and cancellation ----

  @Test
  void newStudioLaunch_replacesMonitorAndOldMonitorCannotFireExit() {
    Game first = game(1, "Medieval Madness");
    Game second = game(2, "Attack from Mars");
    Runnable firstMonitor = launch(first);
    emulatorRunning.set(true);
    firstMonitor.run();

    Runnable secondMonitor = launch(second);
    assertNotSame(firstMonitor, secondMonitor);
    verify(scheduledFuture).cancel(false);

    //a last poll of the replaced monitor that was already in progress
    emulatorRunning.set(false);
    for (int i = 0; i < FrontendStatusService.STUDIO_LAUNCH_REQUIRED_MISSES; i++) {
      firstMonitor.run();
    }

    verify(listener, after(NEGATIVE_WAIT_MS).never()).tableExited(any());
    assertSame(secondMonitor, frontendStatusService.getActiveStudioLaunchMonitor());
  }

  @Test
  void frontendLaunch_stopsMonitor() {
    Runnable monitor = launch(game(1, "Medieval Madness"));
    emulatorRunning.set(true);
    monitor.run();

    frontendStatusService.notifyFrontendLaunch();

    verify(scheduledFuture).cancel(false);
    assertNull(frontendStatusService.getActiveStudioLaunchMonitor());

    emulatorRunning.set(false);
    for (int i = 0; i < FrontendStatusService.STUDIO_LAUNCH_REQUIRED_MISSES; i++) {
      monitor.run();
    }
    verify(listener, after(NEGATIVE_WAIT_MS).never()).tableExited(any());
  }

  @Test
  void frontendGameLaunch_stopsMonitor() {
    launch(game(1, "Medieval Madness"));
    Game popperGame = game(2, "Attack from Mars");
    when(gameService.getGameByTableAndEmuParameter("Attack from Mars", null)).thenReturn(popperGame);

    assertTrue(frontendStatusService.gameLaunch("Attack from Mars", null));

    verify(scheduledFuture).cancel(false);
    assertNull(frontendStatusService.getActiveStudioLaunchMonitor());
    verify(listener, timeout(POSITIVE_WAIT_MS)).tableLaunched(argThat(e -> e.getGame() == popperGame && e.getOrigin() == TableStatusChangedOrigin.ORIGIN_POPPER));
  }
}
