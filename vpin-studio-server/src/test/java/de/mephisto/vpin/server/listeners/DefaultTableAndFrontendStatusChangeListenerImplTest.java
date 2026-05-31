package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.VPinScreenService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultTableAndFrontendStatusChangeListenerImplTest {

  @Mock
  private FrontendStatusService frontendStatusService;
  @Mock
  private FrontendService frontendService;
  @Mock
  private VPinScreenService vpinScreenService;
  @Mock
  private DiscordService discordService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private PreferencesService preferencesService;
  @Mock
  private GameService gameService;
  @Mock
  private PupPacksService pupPacksService;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private DefaultTableAndFrontendStatusChangeListenerImpl listener;

  @Test
  void afterPropertiesSet_registersListenersAndLoadsSettings() throws Exception {
    NotificationSettings settings = new NotificationSettings();
    when(preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class))
        .thenReturn(settings);

    listener.afterPropertiesSet();

    verify(frontendStatusService).addTableStatusChangeListener(listener);
    verify(frontendStatusService).addFrontendStatusChangeListener(listener);
    verify(preferencesService).addChangeListener(listener);
    verify(preferencesService).getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
  }

  @Test
  void preferenceChanged_updatesNotificationSettings() throws Exception {
    NotificationSettings settings = new NotificationSettings();
    when(preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class))
        .thenReturn(settings);

    listener.preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);

    verify(preferencesService).getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
  }

  @Test
  void preferenceChanged_ignoresOtherPropertyNames() throws Exception {
    listener.preferenceChanged("some.other.key", null, null);

    verify(preferencesService, never()).getJsonPreference(eq(PreferenceNames.NOTIFICATION_SETTINGS), any());
  }

  @Test
  void exitDelayConstant_hasExpectedValue() {
    assertEquals(6000, DefaultTableAndFrontendStatusChangeListenerImpl.EXIT_DELAY);
  }
}
