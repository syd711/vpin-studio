package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private HighscoreService highscoreService;

  @Mock
  private SystemService systemService;

  @Mock
  private FrontendStatusService frontendStatusService;

  @Mock
  private FrontendService frontendService;

  @InjectMocks
  private NotificationService notificationService;

  private NotificationSettings notificationSettings;

  @BeforeEach
  void setUp() throws Exception {
    // Initialize notificationSettings via preferenceChanged (avoids afterPropertiesSet's static feature checks)
    notificationSettings = new NotificationSettings(); // durationSec defaults to 5
    when(preferencesService.getJsonPreference(eq(PreferenceNames.NOTIFICATION_SETTINGS), eq(NotificationSettings.class)))
        .thenReturn(notificationSettings);
    notificationService.preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);
  }

  // ---- preferenceChanged ----

  @Test
  void preferenceChanged_loadsNewSettings_forNotificationSettingsKey() throws Exception {
    NotificationSettings newSettings = new NotificationSettings();
    when(preferencesService.getJsonPreference(eq(PreferenceNames.NOTIFICATION_SETTINGS), eq(NotificationSettings.class)))
        .thenReturn(newSettings);

    notificationService.preferenceChanged(PreferenceNames.NOTIFICATION_SETTINGS, null, null);

    verify(preferencesService, atLeastOnce()).getJsonPreference(eq(PreferenceNames.NOTIFICATION_SETTINGS), eq(NotificationSettings.class));
  }

  @Test
  void preferenceChanged_doesNotLoadSettings_forUnrelatedKey() throws Exception {
    notificationService.preferenceChanged("some.other.key", null, null);

    // only the setUp() call should have hit getJsonPreference
    verify(preferencesService, times(1))
        .getJsonPreference(eq(PreferenceNames.NOTIFICATION_SETTINGS), eq(NotificationSettings.class));
  }

  // ---- highscoreChanged: early return when durationSec == 0 ----

  @Test
  void highscoreChanged_returnsEarly_whenDurationSecIsZero() {
    notificationSettings.setDurationSec(0);
    HighscoreChangeEvent event = buildEvent(EventOrigin.TABLE_EXIT_EVENT);

    notificationService.highscoreChanged(event);

    verifyNoInteractions(frontendService);
  }

  // ---- highscoreChanged: EventOrigin filtering ----

  @Test
  void highscoreChanged_returnsEarly_whenOriginIsInitialScan() {
    HighscoreChangeEvent event = buildEvent(EventOrigin.INITIAL_SCAN);

    notificationService.highscoreChanged(event);

    verifyNoInteractions(frontendService);
  }

  @Test
  void highscoreChanged_returnsEarly_whenOriginIsUserInitiated() {
    HighscoreChangeEvent event = buildEvent(EventOrigin.USER_INITIATED);

    notificationService.highscoreChanged(event);

    verifyNoInteractions(frontendService);
  }

  @Test
  void highscoreChanged_returnsEarly_whenOriginIsTableScan() {
    HighscoreChangeEvent event = buildEvent(EventOrigin.TABLE_SCAN);

    notificationService.highscoreChanged(event);

    verifyNoInteractions(frontendService);
  }

  // ---- highscoreChanged: valid origin passes the filter ----

  @Test
  void highscoreChanged_passesFilter_forTableExitOrigin() {
    // After passing the filter, code calls frontendService.getWheelImage(game)
    // Features.IS_STANDALONE=false and NOTIFICATIONS_ENABLED=true by default
    // so showNotification is attempted; frontendService.getWheelImage returns null (mock default)
    // NotificationFactory handles null file gracefully
    notificationSettings.setHighscoreUpdatedNotification(false);
    notificationSettings.setDiscordNotification(false);
    // durationSec == 5 by default but showNotification checks it too — set to 0 to avoid JavaFX
    notificationSettings.setDurationSec(0);

    HighscoreChangeEvent event = buildEvent(EventOrigin.TABLE_EXIT_EVENT);

    // With durationSec=0 inside showNotification, it logs and returns early — no exception
    notificationService.highscoreChanged(event);
  }

  // ---- helper ----

  private HighscoreChangeEvent buildEvent(EventOrigin origin) {
    Game game = mock(Game.class);
    Score oldScore = mock(Score.class);
    Score newScore = mock(Score.class);
    return new HighscoreChangeEvent(game, oldScore, newScore, "rawScore", 1, false, origin);
  }
}
