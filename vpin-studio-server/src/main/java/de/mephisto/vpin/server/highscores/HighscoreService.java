package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.competitions.ScoreSummary;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HighscoreService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private HighscoreRepository highscoreRepository;

  @Autowired
  private HighscoreVersionRepository highscoreVersionRepository;

  @Autowired
  private HighscoreParser highscoreParser;

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

  public List<Score> parseScores(Date createdAt, String raw, int gameId) {
    return highscoreParser.parseScores(createdAt, raw, gameId);
  }

  /**
   * Returns all available scores for the game with the given id and time frame
   *
   * @param gameId
   * @param start
   * @param end
   * @return
   */
  public ScoreList getScoresBetween(int gameId, Date start, Date end) {
    ScoreList scoreList = new ScoreList();
    Optional<Highscore> highscore = highscoreRepository.findByGameIdAndCreatedAtBetween(gameId, start, end);
    if (highscore.isPresent()) {
      Highscore h = highscore.get();
      ScoreSummary scoreSummary = getScoreSummary(h.getCreatedAt(), h.getRaw(), gameId);
      scoreList.setLatestScore(scoreSummary);
      scoreList.getScores().add(scoreSummary);
    }

    List<HighscoreVersion> byGameIdAndCreatedAtBetween = highscoreVersionRepository.findByGameIdAndCreatedAtBetween(gameId, start, end);
    for (HighscoreVersion version : byGameIdAndCreatedAtBetween) {
      ScoreSummary scoreSummary = getScoreSummary(version.getCreatedAt(), version.getRaw(), gameId);
      scoreList.getScores().add(scoreSummary);
    }
    return scoreList;
  }


  /**
   * Returns a list of all scores that are available for the player with the given initials
   *
   * @param initials the initials to filter for
   * @return all highscores of the given player
   */
  public ScoreSummary getHighscores(String initials) {
    ScoreSummary summary = new ScoreSummary(new ArrayList<>(), new Date());
    List<Highscore> all = highscoreRepository.findAll();
    for (Highscore highscore : all) {
      if (StringUtils.isEmpty(highscore.getRaw())) {
        continue;
      }

      List<Score> scores = parseScores(highscore.getCreatedAt(), highscore.getRaw(), highscore.getGameId());
      for (Score score : scores) {
        if (score.getPlayerInitials().equalsIgnoreCase(initials)) {
          summary.getScores().add(score);
        }
      }
    }
    return summary;
  }

  /**
   * Returns a list of all scores for the given game
   *
   * @param game the game to retrieve the highscores for
   * @return all highscores of the given player
   */
  public ScoreSummary getHighscores(Game game) {
    ScoreSummary summary = new ScoreSummary(new ArrayList<>(), new Date());
    Optional<Highscore> highscore = highscoreRepository.findByGameId(game.getId());
    if (highscore.isPresent()) {
      Highscore h = highscore.get();
      if (!StringUtils.isEmpty(h.getRaw())) {
        List<Score> scores = parseScores(h.getCreatedAt(), h.getRaw(), game.getId());
        summary.setRaw(h.getRaw());
        summary.getScores().addAll(scores);
      }
    }
    return summary;
  }

  public List<Highscore> getRecentHighscores() {
    List<Highscore> highscores = highscoreRepository.findAllByOrderByCreatedAtAsc();
    return highscores.stream().filter(h -> !StringUtils.isEmpty(h.getRaw())).collect(Collectors.toList());
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
    if (StringUtils.isEmpty(rawHighscore)) {
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
        int changedPosition = calculateChangedPosition(highscore, newHighscore, game.getId());
        HighscoreVersion version = highscore.toVersion(changedPosition);
        highscoreVersionRepository.saveAndFlush(version);

        //update existing one
        highscore.setRaw(rawHighscore);
        highscore.setCreatedAt(new Date());
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

  /**
   * Collects a list of highscores for serialization
   *
   * @param createdAt the date the highscores have been created
   * @param raw       the raw data
   * @param gameId    the gameId of the game
   */
  private ScoreSummary getScoreSummary(Date createdAt, String raw, int gameId) {
    List<Score> scores = parseScores(createdAt, raw, gameId);
    if (scores.size() > 0) {
      return new ScoreSummary(scores, createdAt);
    }
    return null;
  }

  private int calculateChangedPosition(Highscore oldHighscore, Highscore newHighscore, int gameId) {
    List<Score> oldScores = highscoreParser.parseScores(oldHighscore.getCreatedAt(), oldHighscore.getRaw(), gameId);
    List<Score> newScores = highscoreParser.parseScores(newHighscore.getCreatedAt(), newHighscore.getRaw(), gameId);
    for (int i = 0; i < oldScores.size(); i++) {
      if (oldScores.get(i).getScore().equalsIgnoreCase(newScores.get(i).getScore())) {
        return (i + 1);
      }
    }
    return 0;
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
