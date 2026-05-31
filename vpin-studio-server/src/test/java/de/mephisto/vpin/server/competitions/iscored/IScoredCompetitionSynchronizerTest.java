package de.mephisto.vpin.server.competitions.iscored;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IScoredCompetitionSynchronizerTest {

  @Mock
  private GameService gameService;
  @Mock
  private CompetitionService competitionService;
  @Mock
  private FrontendStatusService frontendStatusService;
  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private IScoredCompetitionSynchronizer synchronizer;

  // ---- afterPropertiesSet ----

  @Test
  void afterPropertiesSet_registersListeners() throws Exception {
    synchronizer.afterPropertiesSet();

    verify(frontendStatusService).addTableStatusChangeListener(synchronizer);
    verify(preferencesService).addChangeListener(synchronizer);
  }

  // ---- getPriority ----

  @Test
  void getPriority_returnsHighValue() {
    assertTrue(synchronizer.getPriority() > 0,
        "Priority must be positive so this listener runs before the highscore listener");
  }

  // ---- tableLaunched ----

  @Test
  void tableLaunched_doesNotInteractWithServices() {
    // tableLaunched is a no-op; verify no side effects
    synchronizer.tableLaunched(null);

    verifyNoInteractions(gameService, competitionService);
  }

  // ---- synchronizeGameRooms — disabled iScored ----

  @Test
  void synchronizeGameRooms_returnsTrue_andDeletesExistingSubscriptions_whenIScoredDisabled() {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(false);

    Competition sub = mock(Competition.class);

    List<Competition> subscriptions = new ArrayList<>(List.of(sub));
    when(competitionService.getIScoredSubscriptions()).thenReturn(subscriptions);
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);
    when(gameService.getKnownGames(-1)).thenReturn(Collections.emptyList());

    try (MockedStatic<IScored> iscoredStatic = mockStatic(IScored.class)) {
      // disabled → subscription deletion before any game room lookup
      boolean result = synchronizer.synchronizeGameRooms();

      assertTrue(result);
      verify(competitionService).delete(sub.getId());
    }
  }

  @Test
  void synchronizeGameRooms_returnsTrue_andSkipsSync_whenNoGameRoomsConfigured() {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(true);
    settings.setGameRooms(Collections.emptyList());

    when(competitionService.getIScoredSubscriptions()).thenReturn(Collections.emptyList());
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);
    when(gameService.getKnownGames(-1)).thenReturn(Collections.emptyList());

    try (MockedStatic<IScored> iscoredStatic = mockStatic(IScored.class)) {
      boolean result = synchronizer.synchronizeGameRooms();

      assertTrue(result);
      // No game rooms configured → IScored.getGameRoom never called
      iscoredStatic.verifyNoInteractions();
    }
  }

  // ---- tableExited — disabled iScored ----

  @Test
  void tableExited_doesNothing_whenIScoredDisabled() {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(false);
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);

    de.mephisto.vpin.server.games.TableStatusChangedEvent event = mock(de.mephisto.vpin.server.games.TableStatusChangedEvent.class);

    try (MockedStatic<IScored> iscoredStatic = mockStatic(IScored.class)) {
      synchronizer.tableExited(event);

      verifyNoInteractions(gameService, competitionService);
      iscoredStatic.verifyNoInteractions();
    }
  }

  // ---- preferenceChanged ----

  @Test
  void preferenceChanged_ignoresUnrelatedProperty() throws Exception {
    try (MockedStatic<IScored> ignored = mockStatic(IScored.class)) {
      // unrelated property → does NOT trigger synchronizeGameRooms
      synchronizer.preferenceChanged("some.other.property", null, null);

      verifyNoInteractions(gameService, competitionService);
    }
  }

  @Test
  void preferenceChanged_triggersSync_whenIScoredSettingsChanged() throws Exception {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(false); // disabled so sync is a no-op
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);
    when(competitionService.getIScoredSubscriptions()).thenReturn(Collections.emptyList());
    when(gameService.getKnownGames(-1)).thenReturn(Collections.emptyList());

    try (MockedStatic<IScored> ignored = mockStatic(IScored.class)) {
      synchronizer.preferenceChanged(PreferenceNames.ISCORED_SETTINGS, null, null);

      verify(gameService).getKnownGames(-1);
    }
  }
}
