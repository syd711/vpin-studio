package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.jpa.Highscore;
import de.mephisto.vpin.server.jpa.HighscoreRepository;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HighscoreService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private HighscoreRepository highscoreRepository;

  private HighscoreResolver highscoreResolver;

  private final List<HighscoreChangeListener> listeners = new ArrayList<>();

  @Nullable
  public Highscore getHighscore(@NonNull Game game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return null;
    }

    //check if an entry exists, create the first one with empty values otherwise
    Highscore highscore = highscoreRepository.findByPupId(game.getId());
    if (highscore == null) {
      Highscore initialHighscore = highscoreResolver.parseHighscore(game);
      if (initialHighscore == null) {
        initialHighscore = Highscore.forGame(game, null);
      }
      highscoreRepository.save(initialHighscore);
      highscore = initialHighscore;
    }

    return highscore;
  }

  public void addHighscoreChangeListener(@NonNull HighscoreChangeListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Returns true when the new highscore contains a higher value, than the folder one.
   *
   * @param game the game that should be updated
   */
  public void updateHighscore(@NonNull Game game) {
    highscoreResolver.refresh();
    HighscoreChangeEvent event = null;
    Highscore updatedHighscore = highscoreResolver.parseHighscore(game);
    Highscore existingHighscore = highscoreRepository.findByPupId(game.getId());
    if (updatedHighscore != null) {
      if (existingHighscore != null) {
        if (updatedHighscore.containsHigherScoreThan(existingHighscore)) {
          event = new HighscoreChangeEvent() {
            @Override
            public Game getGame() {
              return game;
            }

            @Override
            public Highscore getOldHighscore() {
              return existingHighscore;
            }

            @Override
            public Highscore getNewHighscore() {
              return updatedHighscore;
            }
          };
          triggerHighscoreChange(event);
        }
        updatedHighscore.setCreatedAt(existingHighscore.getCreatedAt());
      }
      highscoreRepository.save(updatedHighscore);
      LOG.info("Invalidated highscore of " + game);
    }
  }

  private void triggerHighscoreChange(@NonNull HighscoreChangeEvent event) {
    new Thread(() -> {
      for (HighscoreChangeListener listener : listeners) {
        listener.highscoreChanged(event);
      }
    }).start();
  }

  @Override
  public void afterPropertiesSet() {
    this.highscoreResolver = new HighscoreResolver(systemService);
  }
}
