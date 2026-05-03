package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlxServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private EmulatorService emulatorService;

  @Mock
  private HighscoreService highscoreService;

  @InjectMocks
  private AlxService alxService;

  // ---- getAlxSummary(int) ----

  @Test
  void getAlxSummary_withGameId_fetchesSingleGameAlxData() {
    TableAlxEntry entry = new TableAlxEntry();
    entry.setGameId(5);
    when(frontendService.getStartDate()).thenReturn(new Date());
    when(frontendService.getAlxData(5)).thenReturn(List.of(entry));
    when(highscoreService.getHighscoreVersionsByGame(5)).thenReturn(Collections.emptyList());

    AlxSummary result = alxService.getAlxSummary(5);

    assertNotNull(result);
    assertEquals(1, result.getEntries().size());
    verify(frontendService).getAlxData(5);
    verify(frontendService, never()).getAlxData();
  }

  @Test
  void getAlxSummary_withNegativeId_fetchesAllAlxData() {
    when(frontendService.getStartDate()).thenReturn(new Date());
    when(frontendService.getAlxData()).thenReturn(Collections.emptyList());

    AlxSummary result = alxService.getAlxSummary(-1);

    assertNotNull(result);
    verify(frontendService).getAlxData();
    verify(frontendService, never()).getAlxData(anyInt());
  }

  @Test
  void getAlxSummary_countsScoresAndHighscores() {
    TableAlxEntry entry = new TableAlxEntry();
    entry.setGameId(3);
    when(frontendService.getStartDate()).thenReturn(new Date());
    when(frontendService.getAlxData(3)).thenReturn(List.of(entry));

    HighscoreVersion rank1 = mock(HighscoreVersion.class);
    when(rank1.getChangedPosition()).thenReturn(1);
    HighscoreVersion rank2 = mock(HighscoreVersion.class);
    when(rank2.getChangedPosition()).thenReturn(2);
    HighscoreVersion rank0 = mock(HighscoreVersion.class);
    when(rank0.getChangedPosition()).thenReturn(0);

    when(highscoreService.getHighscoreVersionsByGame(3)).thenReturn(Arrays.asList(rank1, rank2, rank0));

    AlxSummary result = alxService.getAlxSummary(3);

    TableAlxEntry resultEntry = result.getEntries().get(0);
    assertEquals(2, resultEntry.getScores());   // rank1 + rank2 have position > 0
    assertEquals(1, resultEntry.getHighscores()); // only rank1 has position == 1
  }

  @Test
  void getAlxSummary_noArgs_delegatesToGetAlxSummaryWithMinusOne() {
    when(frontendService.getStartDate()).thenReturn(new Date());
    when(frontendService.getAlxData()).thenReturn(Collections.emptyList());

    AlxSummary result = alxService.getAlxSummary();

    assertNotNull(result);
    verify(frontendService).getAlxData();
  }

  // ---- deleteNumberPlaysForGame ----

  @Test
  void deleteNumberPlaysForGame_setsCountToZero() {
    FrontendConnector connector = mock(FrontendConnector.class);
    when(frontendService.getFrontendConnector()).thenReturn(connector);
    when(connector.updateNumberOfPlaysForGame(7, 0)).thenReturn(true);

    boolean result = alxService.deleteNumberPlaysForGame(7);

    assertTrue(result);
    verify(connector).updateNumberOfPlaysForGame(7, 0);
  }

  // ---- deleteTimePlayedForGame ----

  @Test
  void deleteTimePlayedForGame_setsSecondsToZero() {
    FrontendConnector connector = mock(FrontendConnector.class);
    when(frontendService.getFrontendConnector()).thenReturn(connector);
    when(connector.updateSecondsPlayedForGame(9, 0)).thenReturn(true);

    boolean result = alxService.deleteTimePlayedForGame(9);

    assertTrue(result);
    verify(connector).updateSecondsPlayedForGame(9, 0);
  }

  // ---- deleteNumberOfPlaysForEmulator ----

  @Test
  void deleteNumberOfPlaysForEmulator_withMinusOne_deletesAllEmulators() {
    GameEmulator emu1 = new GameEmulator();
    emu1.setId(1);
    GameEmulator emu2 = new GameEmulator();
    emu2.setId(2);
    when(emulatorService.getValidGameEmulators()).thenReturn(Arrays.asList(emu1, emu2));

    Game g1 = new Game();
    g1.setId(10);
    Game g2 = new Game();
    g2.setId(11);
    when(frontendService.getGamesByEmulator(1)).thenReturn(List.of(g1));
    when(frontendService.getGamesByEmulator(2)).thenReturn(List.of(g2));

    FrontendConnector connector = mock(FrontendConnector.class);
    when(frontendService.getFrontendConnector()).thenReturn(connector);
    when(connector.updateNumberOfPlaysForGame(anyInt(), eq(0L))).thenReturn(true);

    boolean result = alxService.deleteNumberOfPlaysForEmulator(-1);

    assertTrue(result);
    verify(connector).updateNumberOfPlaysForGame(10, 0);
    verify(connector).updateNumberOfPlaysForGame(11, 0);
  }

  @Test
  void deleteNumberOfPlaysForEmulator_withSpecificId_deletesOnlyThatEmulator() {
    Game g = new Game();
    g.setId(20);
    when(frontendService.getGamesByEmulator(3)).thenReturn(List.of(g));

    FrontendConnector connector = mock(FrontendConnector.class);
    when(frontendService.getFrontendConnector()).thenReturn(connector);
    when(connector.updateNumberOfPlaysForGame(20, 0)).thenReturn(true);

    boolean result = alxService.deleteNumberOfPlaysForEmulator(3);

    assertTrue(result);
    verify(emulatorService, never()).getValidGameEmulators();
  }

  // ---- substractPlayTimeForGame ----

  @Test
  void substractPlayTimeForGame_subtractsFromTimePlayed_whenSummaryHasEntry() {
    TableAlxEntry entry = new TableAlxEntry();
    entry.setGameId(5);
    entry.setTimePlayedSecs(100);

    when(frontendService.getStartDate()).thenReturn(new Date());
    when(frontendService.getAlxData(5)).thenReturn(List.of(entry));
    when(highscoreService.getHighscoreVersionsByGame(5)).thenReturn(Collections.emptyList());

    FrontendConnector connector = mock(FrontendConnector.class);
    when(frontendService.getFrontendConnector()).thenReturn(connector);
    when(connector.updateSecondsPlayedForGame(eq(5), anyLong())).thenReturn(true);

    alxService.substractPlayTimeForGame(5, 10_000); // 10 seconds in ms

    verify(connector).updateSecondsPlayedForGame(5, 90); // 100 - 10 = 90
  }

  @Test
  void substractPlayTimeForGame_doesNotUpdate_whenPauseExceedsTimePlayed() {
    TableAlxEntry entry = new TableAlxEntry();
    entry.setGameId(5);
    entry.setTimePlayedSecs(5); // only 5 seconds played

    when(frontendService.getStartDate()).thenReturn(new Date());
    when(frontendService.getAlxData(5)).thenReturn(List.of(entry));
    when(highscoreService.getHighscoreVersionsByGame(5)).thenReturn(Collections.emptyList());

    FrontendConnector connector = mock(FrontendConnector.class);

    alxService.substractPlayTimeForGame(5, 10_000); // 10 seconds > 5 played

    verify(connector, never()).updateSecondsPlayedForGame(anyInt(), anyLong());
  }
}
