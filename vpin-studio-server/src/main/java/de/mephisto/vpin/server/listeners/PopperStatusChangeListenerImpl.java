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
    preferencesService.savePreference(PreferenceNames.ACTIVE_GAME, game.getId());
    discordService.setStatus(game.getGameDisplayName());
    highscoreService.updateHighscore(game);
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    LOG.info("Executing table exit commands for '" + game + "'");
    discordService.setStatus(null);
    new Thread(() -> {
      try {
        Thread.sleep(4000);
      } catch (InterruptedException e) {
        //ignore
      }
      LOG.info("Finished 4 second update delay, updating highscores.");
      highscoreService.updateHighscore(game);
      preferencesService.savePreference(PreferenceNames.ACTIVE_GAME, -1);
    }).start();
  }

  @Override
  public void popperLaunched() {
    LOG.info("Popper launch event");
    int activeTableId = (int) preferencesService.getPreferenceValue(PreferenceNames.ACTIVE_GAME);
    if (activeTableId >= 0) {
      Game game = gameService.getGame(activeTableId);
      if (game != null) {
        highscoreService.updateHighscore(game);
        preferencesService.savePreference(PreferenceNames.ACTIVE_GAME, -1);
      }
    }
  }

  @Override
  public void popperExited() {
    LOG.info("Popper exit event");
    discordService.setStatus(null);
  }

  @Override
  public void popperRestarted() {
    LOG.info("Popper restarted event");
    discordService.setStatus(null);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    popperService.addPopperStatusChangeListener(this);
  }
}
