package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IScoredHighscoreChangeListenerTest {

  @Mock
  private IScoredService iScoredService;
  @Mock
  private CompetitionService competitionService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private IScoredHighscoreChangeListener listener;

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_registersListener_whenIScoredEnabled() throws Exception {
    listener.afterPropertiesSet();

    verify(highscoreService).addHighscoreChangeListener(listener);
  }

  // --- highscoreUpdated ---

  @Test
  void highscoreUpdated_isNoOp() {
    Game game = mock(Game.class);
    Highscore highscore = mock(Highscore.class);

    listener.highscoreUpdated(game, highscore);

    verifyNoInteractions(iScoredService, competitionService);
  }

  // --- highscoreChanged ---

  @Test
  void highscoreChanged_skips_whenIScoredDisabled() {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(false);
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);

    HighscoreChangeEvent event = buildEvent(false);

    listener.highscoreChanged(event);

    verifyNoInteractions(iScoredService, competitionService);
  }

  @Test
  void highscoreChanged_skips_whenInitialScore() {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(true);
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);

    HighscoreChangeEvent event = buildEvent(true);

    listener.highscoreChanged(event);

    verifyNoInteractions(iScoredService, competitionService);
  }

  @Test
  void highscoreChanged_skips_whenNoPlayerOnScore() {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(true);
    settings.setGameRooms(Collections.emptyList());
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);

    Game game = mock(Game.class);
    Score newScore = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);
    Score oldScore = new Score(Instant.now(), 1, "???", null, "raw", 0L, 2);
    HighscoreChangeEvent event = new HighscoreChangeEvent(game, oldScore, newScore, "raw", 1, false, EventOrigin.USER_INITIATED);

    listener.highscoreChanged(event);

    verifyNoInteractions(iScoredService);
  }

  @Test
  void highscoreChanged_skips_whenNoGameRooms() {
    IScoredSettings settings = new IScoredSettings();
    settings.setEnabled(true);
    settings.setGameRooms(Collections.emptyList());
    when(preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class))
        .thenReturn(settings);

    Game game = mock(Game.class);
    de.mephisto.vpin.server.players.Player player = mock(de.mephisto.vpin.server.players.Player.class);
    Score newScore = new Score(Instant.now(), 1, "AAA", player, "raw", 1000000L, 1);
    Score oldScore = new Score(Instant.now(), 1, "???", null, "raw", 0L, 2);
    HighscoreChangeEvent event = new HighscoreChangeEvent(game, oldScore, newScore, "raw", 1, false, EventOrigin.USER_INITIATED);

    listener.highscoreChanged(event);

    verifyNoInteractions(iScoredService);
  }

  // --- helpers ---

  private HighscoreChangeEvent buildEvent(boolean initialScore) {
    Game game = mock(Game.class);
    Score newScore = new Score(Instant.now(), 1, "AAA", null, "raw", 1000000L, 1);
    Score oldScore = new Score(Instant.now(), 1, "???", null, "raw", 0L, 2);
    return new HighscoreChangeEvent(game, oldScore, newScore, "raw", 1, initialScore, EventOrigin.USER_INITIATED);
  }
}
