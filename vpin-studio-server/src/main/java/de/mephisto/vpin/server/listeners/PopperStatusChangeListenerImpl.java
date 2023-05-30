package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PopperStatusChangeListenerImpl implements InitializingBean, PopperStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(PopperStatusChangeListenerImpl.class);
  public static final int EXIT_DELAY = 6000;

  @Autowired
  private PopperService popperService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameService gameService;

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    Game game = event.getGame();
    discordService.setActivity(game.getGameDisplayName());
    highscoreService.scanScore(game);

    try {
      //rescan highscore of last game in case Discord was offline
      int activeTableId = (int) preferencesService.getPreferenceValue(PreferenceNames.ACTIVE_GAME);
      if (activeTableId >= 0) {
        Game lastGamePlayed = gameService.getGame(activeTableId);
        if (lastGamePlayed != null && lastGamePlayed.getId() != game.getId()) {
          highscoreService.scanScore(game);
        }
        preferencesService.savePreference(PreferenceNames.ACTIVE_GAME, game.getId());
      }
    } catch (Exception e) {
      LOG.info("Failed to refresh game: " + e.getMessage(), e);
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    LOG.info("Executing table exit commands for '" + game + "'");
    discordService.setActivity(null);
    new Thread(() -> {
      LOG.info("Starting " + EXIT_DELAY + "ms update delay before updating highscores.");
      try {
        Thread.sleep(EXIT_DELAY);
      } catch (InterruptedException e) {
        //ignore
      }
      LOG.info("Finished " + EXIT_DELAY + "ms update delay, updating highscores.");
      highscoreService.scanScore(game);
    }).start();
  }

  @Override
  public void popperLaunched() {
    LOG.info("Popper launch event");
    int activeTableId = (int) preferencesService.getPreferenceValue(PreferenceNames.ACTIVE_GAME);
    if (activeTableId >= 0) {
      Game game = gameService.getGame(activeTableId);
      if (game != null) {
        highscoreService.scanScore(game);
      }
    }
  }

  @Override
  public void popperExited() {
    LOG.info("Popper exit event");
    discordService.setActivity(null);
  }

  @Override
  public void popperRestarted() {
    LOG.info("Popper restarted event");
    discordService.setActivity(null);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    popperService.addPopperStatusChangeListener(this);
  }
}
