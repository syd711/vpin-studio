package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.games.Game;
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
import java.util.Optional;

@Service
public class HighscoreService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private HighscoreRepository highscoreRepository;

  @Autowired
  private HighscoreVersionRepository highscoreVersionRepository;

  private HighscoreResolver highscoreResolver;

  private final List<HighscoreChangeListener> listeners = new ArrayList<>();

  @Nullable
  public Highscore getOrCreateHighscore(@NonNull Game game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return null;
    }

    //check if an entry exists, create the first one with empty values otherwise
    Optional<Highscore> highscore = highscoreRepository.findByGameId(game.getId());
    if (highscore.isEmpty()) {
      String rawHighscore = highscoreResolver.readHighscore(game);
      Highscore h = Highscore.forGame(game, rawHighscore);
      highscoreRepository.saveAndFlush(h);
      LOG.info("Written " + h);
      return h;
    }

    return highscore.get();
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

    String rawHighscore = highscoreResolver.readHighscore(game);
    if(StringUtils.isEmpty(rawHighscore)) {
      LOG.info("Skipped highscore changed event for {} because the raw data of the score is empty.", game);
      return;
    }

    Highscore newHighscore = Highscore.forGame(game, rawHighscore);
    Optional<Highscore> existingHighscore = highscoreRepository.findByGameId(game.getId());

    if (existingHighscore.isEmpty() || !existingHighscore.get().getRaw().equals(rawHighscore)) {
      //save the highscore for the first time
      if (existingHighscore.isEmpty()) {
        highscoreRepository.saveAndFlush(newHighscore);
        LOG.info("Saved highscore for " + game);
      }
      else {
        //archive old highscore
        Highscore highscore = existingHighscore.get();
        HighscoreVersion version = highscore.toVersion();
        highscoreVersionRepository.saveAndFlush(version);

        //update existing one
        highscore.setRaw(rawHighscore);
        highscoreRepository.saveAndFlush(highscore);

        LOG.info("Archived old highscore and saved updated highscore for " + game);
      }

      event = new HighscoreChangeEvent() {
        @Override
        public Game getGame() {
          return game;
        }

        @Override
        public Highscore getOldHighscore() {
          return existingHighscore.get();
        }

        @Override
        public Highscore getNewHighscore() {
          return newHighscore;
        }
      };

      triggerHighscoreChange(event);
    }
    else {
      LOG.info("Skipped highscore change event for {} because the raw highscore data did not change.", game);
    }
  }

  public List<Score> convertToScores(Highscore highscore) {
    List<Score> result = new ArrayList<>();
//    if (!StringUtils.isEmpty(this.score1)) {
//      result.add(new Score(initials1, score1, 1));
//    }
//    if (!StringUtils.isEmpty(this.score2)) {
//      result.add(new Score(initials2, score2, 2));
//    }
//    if (!StringUtils.isEmpty(this.score3)) {
//      result.add(new Score(initials3, score3, 3));
//    }
    return result;
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
