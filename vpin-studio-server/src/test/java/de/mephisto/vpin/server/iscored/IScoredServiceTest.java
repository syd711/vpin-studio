package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IScoredServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private CompetitionService competitionService;

  @Mock
  private GameService gameService;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private IScoredService service;

  @Test
  void deleteGameRoom_returnsTrue_whenRoomExists() throws Exception {
    String uuid = UUID.randomUUID().toString();
    IScoredGameRoom room = new IScoredGameRoom();
    room.setUuid(uuid);
    room.setUrl("http://iscored.example.com/room/1");

    IScoredSettings settings = mock(IScoredSettings.class);
    when(settings.getGameRooms()).thenReturn(Arrays.asList(room));
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class)).thenReturn(settings);
    when(competitionService.getIScoredSubscriptions()).thenReturn(Collections.emptyList());

    boolean result = service.deleteGameRoom(uuid);

    assertTrue(result);
    verify(settings).remove(room);
    verify(preferencesService).savePreference(settings, true);
  }

  @Test
  void deleteGameRoom_returnsFalse_whenRoomDoesNotExist() throws Exception {
    IScoredSettings settings = mock(IScoredSettings.class);
    when(settings.getGameRooms()).thenReturn(Collections.emptyList());
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class)).thenReturn(settings);

    boolean result = service.deleteGameRoom("nonexistent-uuid");

    assertFalse(result);
    verify(settings, never()).remove(any());
  }

  @Test
  void deleteGameRoom_deletesMatchingIScoredCompetitions() throws Exception {
    String uuid = UUID.randomUUID().toString();
    String url = "http://iscored.example.com/room/42";

    IScoredGameRoom room = new IScoredGameRoom();
    room.setUuid(uuid);
    room.setUrl(url);

    IScoredSettings settings = mock(IScoredSettings.class);
    when(settings.getGameRooms()).thenReturn(Arrays.asList(room));
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class)).thenReturn(settings);

    Competition comp = mock(Competition.class);
    when(comp.getUrl()).thenReturn(url);
    when(comp.getId()).thenReturn(100L);
    when(competitionService.getIScoredSubscriptions()).thenReturn(Arrays.asList(comp));

    service.deleteGameRoom(uuid);

    verify(competitionService).delete(100L);
  }

  @Test
  void preferenceChanged_updatesNotificationSettings() throws Exception {
    NotificationSettings notificationSettings = new NotificationSettings();
    when(preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class))
        .thenReturn(notificationSettings);

    service.preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);

    verify(preferencesService).getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
  }

  @Test
  void preferenceChanged_ignoredForOtherPreferenceNames() throws Exception {
    service.preferenceChanged("some.other.preference", null, null);

    verify(preferencesService, never()).getJsonPreference(eq(PreferenceNames.NOTIFICATION_SETTINGS), any());
  }

  @Test
  void afterPropertiesSet_registersChangeListenerAndLoadsSettings() throws Exception {
    NotificationSettings notificationSettings = new NotificationSettings();
    when(preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class))
        .thenReturn(notificationSettings);

    service.afterPropertiesSet();

    verify(preferencesService).addChangeListener(service);
    verify(preferencesService).getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
  }
}
