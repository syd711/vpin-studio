package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
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

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    Game game = event.getGame();
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
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        //ignore
      }
      LOG.info("Finished 5 second update delay, updating highscores.");
      highscoreService.updateHighscore(game);
    }).start();
  }

  @Override
  public void popperLaunched() {

  }

  @Override
  public void popperExited() {
    discordService.setStatus(null);
  }

  @Override
  public void popperRestarted() {
    discordService.setStatus(null);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    popperService.addPopperStatusChangeListener(this);
  }
}
