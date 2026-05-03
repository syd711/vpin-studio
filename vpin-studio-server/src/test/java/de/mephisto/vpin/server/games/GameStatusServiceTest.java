package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.alx.AlxService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameStatusServiceTest {

  @Mock
  private AlxService alxService;

  @InjectMocks
  private GameStatusService gameStatusService;

  private TableStatusChangedEvent mockEvent(int gameId, String displayName) {
    Game game = new Game();
    game.setId(gameId);
    game.setGameDisplayName(displayName);
    TableStatusChangedEvent event = mock(TableStatusChangedEvent.class);
    when(event.getGame()).thenReturn(game);
    return event;
  }

  // ---- isActive / forceActive ----

  @Test
  void isActive_returnsFalse_initially() {
    assertFalse(gameStatusService.isActive());
  }

  @Test
  void isActive_returnsTrue_whenForceActiveIsSet() {
    gameStatusService.setForceActive(true);
    assertTrue(gameStatusService.isActive());
  }

  @Test
  void isActive_returnsFalse_afterForceActiveCleared() {
    gameStatusService.setForceActive(true);
    gameStatusService.setForceActive(false);
    assertFalse(gameStatusService.isActive());
  }

  // ---- getStatus ----

  @Test
  void getStatus_returnsNonNull() {
    assertNotNull(gameStatusService.getStatus());
  }

  @Test
  void getStatus_returnsSameInstanceEachTime() {
    assertSame(gameStatusService.getStatus(), gameStatusService.getStatus());
  }

  // ---- tableLaunched / setActiveStatus ----

  @Test
  void tableLaunched_setsGameIdOnStatus() {
    gameStatusService.tableLaunched(mockEvent(42, "Test Game"));

    assertEquals(42, gameStatusService.getStatus().getGameId());
  }

  @Test
  void setActiveStatus_updatesGameId() {
    gameStatusService.setActiveStatus(99);

    assertEquals(99, gameStatusService.getStatus().getGameId());
  }

  // ---- tableExited / frontendEvents → resetStatus ----

  @Test
  void tableExited_resetsGameId() {
    gameStatusService.setActiveStatus(5);
    gameStatusService.tableExited(mockEvent(5, "Test Game"));

    assertEquals(-1, gameStatusService.getStatus().getGameId());
  }

  @Test
  void frontendExited_resetsStatus() {
    gameStatusService.setActiveStatus(7);
    gameStatusService.frontendExited();

    assertEquals(-1, gameStatusService.getStatus().getGameId());
  }

  @Test
  void frontendRestarted_resetsStatus() {
    gameStatusService.setActiveStatus(8);
    gameStatusService.frontendRestarted();

    assertEquals(-1, gameStatusService.getStatus().getGameId());
  }

  @Test
  void frontendLaunched_setsGameIdToMinusOne() {
    gameStatusService.setActiveStatus(10);
    gameStatusService.frontendLaunched();

    assertEquals(-1, gameStatusService.getStatus().getGameId());
  }

  // ---- pause lifecycle ----

  @Test
  void startPause_returnsGameStatus() {
    GameStatus result = gameStatusService.startPause();
    assertNotNull(result);
    assertSame(gameStatusService.getStatus(), result);
  }

  @Test
  void finishPause_returnsGameStatus() {
    gameStatusService.startPause();
    GameStatus result = gameStatusService.finishPause();
    assertNotNull(result);
  }

  // ---- resetStatus calls alxService ----

  @Test
  void resetStatus_callsAlxService_whenGameIdIsNotMinusOne() {
    gameStatusService.setActiveStatus(3);
    gameStatusService.startPause();
    gameStatusService.finishPause();
    // capture the pause duration before reset clears it
    long pauseMs = gameStatusService.getStatus().getPauseDurationMs();

    gameStatusService.resetStatus();

    verify(alxService).substractPlayTimeForGame(3, pauseMs);
  }

  @Test
  void resetStatus_doesNotCallAlxService_whenGameIdIsMinusOne() {
    gameStatusService.frontendLaunched();  // sets gameId to -1
    gameStatusService.resetStatus();

    verifyNoInteractions(alxService);
  }
}
