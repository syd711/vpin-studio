package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.discord.DiscordCompetitionService;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HighscoreChangeListenerImplTest {

  @Mock
  private HighscoreService highscoreService;
  @Mock
  private CompetitionService competitionService;
  @Mock
  private DiscordService discordService;
  @Mock
  private PreferencesService preferencesService;
  @Mock
  private DiscordCompetitionService discordCompetitionService;
  @Mock
  private HighscoreParsingService highscoreParsingService;

  @InjectMocks
  private HighscoreChangeListenerImpl listener;

  // --- afterPropertiesSet ---

  @Test
  void afterPropertiesSet_registersListenerWithHighscoreService() throws Exception {
    listener.afterPropertiesSet();

    verify(highscoreService).addHighscoreChangeListener(listener);
  }

  // --- highscoreUpdated ---

  @Test
  void highscoreUpdated_isNoOp() {
    Game game = mock(Game.class);
    Highscore highscore = mock(Highscore.class);

    listener.highscoreUpdated(game, highscore);

    verifyNoInteractions(competitionService, discordService);
  }

  // --- highscoreChanged ---

  @Test
  void highscoreChanged_skipsDefaultDiscordNotification_whenInitialScore() {
    setupCommonStubs();
    HighscoreChangeEvent event = buildEvent(true, false, "SCORES");

    listener.highscoreChanged(event);

    verify(discordService, never()).sendDefaultHighscoreMessage(any());
  }

  @Test
  void highscoreChanged_skipsDefaultDiscordNotification_whenEventReplay() {
    setupCommonStubs();
    HighscoreChangeEvent event = buildEvent(false, false, "SCORES");
    event.setEventReplay(true);

    listener.highscoreChanged(event);

    verify(discordService, never()).sendDefaultHighscoreMessage(any());
  }

  @Test
  void highscoreChanged_skipsDefaultDiscordNotification_whenRawIsEmpty() {
    setupCommonStubs();
    HighscoreChangeEvent event = buildEvent(false, false, "");

    listener.highscoreChanged(event);

    verify(discordService, never()).sendDefaultHighscoreMessage(any());
  }

  @Test
  void highscoreChanged_updatesSubscriptionChannels_forEachSubscription() {
    setupCommonStubs();
    HighscoreChangeEvent event = buildEvent(true, false, "");

    listener.highscoreChanged(event);

    verify(competitionService).getSubscriptions(any());
  }

  // --- helpers ---

  private void setupCommonStubs() {
    when(preferencesService.getPreferenceValue(PreferenceNames.DISCORD_DYNAMIC_SUBSCRIPTIONS))
        .thenReturn(false);
    when(competitionService.getSubscriptions(any())).thenReturn(Collections.emptyList());
    when(competitionService.getCompetitionForGame(anyInt())).thenReturn(Collections.emptyList());
  }

  private HighscoreChangeEvent buildEvent(boolean initialScore, boolean eventReplay, String raw) {
    Game game = mock(Game.class);
    when(game.getRom()).thenReturn("rom1");
    when(game.getId()).thenReturn(1);
    Score newScore = new Score(new Date(), 1, "AAA", null, "raw", 1000000L, 1);
    Score oldScore = new Score(new Date(), 1, "???", null, "raw", 0L, 2);
    HighscoreChangeEvent event = new HighscoreChangeEvent(game, oldScore, newScore, raw, 1, initialScore, EventOrigin.USER_INITIATED);
    event.setEventReplay(eventReplay);
    return event;
  }
}
