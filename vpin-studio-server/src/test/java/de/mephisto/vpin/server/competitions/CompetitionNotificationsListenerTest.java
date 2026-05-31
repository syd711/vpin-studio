package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompetitionNotificationsListenerTest {

  @Mock
  private NotificationService notificationService;
  @Mock
  private CompetitionLifecycleService competitionLifecycleService;
  @Mock
  private PreferencesService preferencesService;
  @Mock
  private GameService gameService;

  @InjectMocks
  private CompetitionNotificationsListener listener;

  private void enableNotifications(boolean enabled) {
    NotificationSettings settings = mock(NotificationSettings.class);
    when(settings.isCompetitionNotification()).thenReturn(enabled);
    when(preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class))
        .thenReturn(settings);
  }

  private Competition buildCompetition(int gameId, String name) {
    Competition c = new Competition();
    c.setGameId(gameId);
    c.setName(name);
    return c;
  }

  // --- competitionStarted ---

  @Test
  void competitionStarted_showsNotification_whenEnabledAndGameFound() {
    enableNotifications(true);
    Competition competition = buildCompetition(1, "Test Cup");
    Game game = mock(Game.class);
    when(game.getGameDisplayName()).thenReturn("Funhouse");
    when(gameService.getGame(1)).thenReturn(game);
    when(gameService.getWheelImage(game)).thenReturn(mock(File.class));

    listener.competitionStarted(competition);

    verify(notificationService).showNotification(any());
  }

  @Test
  void competitionStarted_skipsNotification_whenDisabled() {
    enableNotifications(false);
    Competition competition = buildCompetition(1, "Test Cup");

    listener.competitionStarted(competition);

    verifyNoInteractions(notificationService, gameService);
  }

  @Test
  void competitionStarted_skipsNotification_whenGameNotFound() {
    enableNotifications(true);
    Competition competition = buildCompetition(99, "Ghost Cup");
    when(gameService.getGame(99)).thenReturn(null);

    listener.competitionStarted(competition);

    verifyNoInteractions(notificationService);
  }

  // --- competitionFinished ---

  @Test
  void competitionFinished_showsNotification_whenEnabledAndGameFound() {
    enableNotifications(true);
    Competition competition = buildCompetition(2, "Finals");
    Game game = mock(Game.class);
    when(game.getGameDisplayName()).thenReturn("Medieval Madness");
    when(gameService.getGame(2)).thenReturn(game);
    when(gameService.getWheelImage(game)).thenReturn(mock(File.class));

    listener.competitionFinished(competition, mock(Player.class), new ScoreSummary());

    verify(notificationService).showNotification(any());
  }

  @Test
  void competitionFinished_skipsNotification_whenDisabled() {
    enableNotifications(false);
    Competition competition = buildCompetition(2, "Finals");

    listener.competitionFinished(competition, null, new ScoreSummary());

    verifyNoInteractions(notificationService, gameService);
  }

  @Test
  void competitionFinished_skipsNotification_whenGameNotFound() {
    enableNotifications(true);
    Competition competition = buildCompetition(99, "No-Game Cup");
    when(gameService.getGame(99)).thenReturn(null);

    listener.competitionFinished(competition, null, new ScoreSummary());

    verifyNoInteractions(notificationService);
  }

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_registersListenerWithLifecycleService() throws Exception {
    listener.afterPropertiesSet();

    verify(competitionLifecycleService).addCompetitionChangeListener(listener);
  }
}
