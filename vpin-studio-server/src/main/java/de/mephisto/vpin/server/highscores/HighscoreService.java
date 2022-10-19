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
      String rawHighscore = highscoreResolver.readHighscore(game);
      highscore = Highscore.forGame(game, rawHighscore);
      highscoreRepository.saveAndFlush(highscore);
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

    String rawHighscore = highscoreResolver.readHighscore(game);
    Highscore updatedHighscore = Highscore.forGame(game, rawHighscore);
    Highscore existingHighscore = highscoreRepository.findByPupId(game.getId());

    if (existingHighscore != null) {
      if (containsHigherScoreThan(updatedHighscore, existingHighscore)) {
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

      //the updated score contains the new data, so we only need to update the date
      updatedHighscore.setCreatedAt(existingHighscore.getCreatedAt());
    }
    highscoreRepository.save(updatedHighscore);
    LOG.info("Invalidated highscore of " + game);
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

  /**
   * The score has changed, which only happens when a new score is created.
   * So we only have to check if the actual score changed.
   *
   * @param updatedHighscore the new highscore
   * @param existingScore                the old highscore to compare with
   * @return true if the compared highscore is higher than this one.
   */
  public boolean containsHigherScoreThan(Highscore updatedHighscore, Highscore existingScore) {
    return false; //TODO
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
