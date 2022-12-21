package de.mephisto.vpin.server.highscores;

import com.google.common.annotations.VisibleForTesting;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
      HighscoreMetadata metadata = highscoreResolver.readHighscore(game);
      Highscore h = Highscore.forGame(game, metadata);
      highscoreRepository.saveAndFlush(h);
      LOG.info("Created initial " + h);
      return h;
    }

    return highscore.get();
  }

  @NonNull
  public HighscoreMetadata scanScore(@NonNull Game game) {
    Optional<Highscore> highscore = highscoreRepository.findByGameId(game.getId());
    HighscoreMetadata metadata = highscoreResolver.readHighscore(game);
    Highscore h = null;
    if (highscore.isEmpty()) {
      h = Highscore.forGame(game, metadata);
    }
    else {
      h = highscore.get();
      h.setFilename(metadata.getFilename());
      h.setLastModified(metadata.getModified());
      h.setType(metadata.getType());
      h.setRaw(metadata.getRaw());
      h.setStatus(metadata.getStatus());
      h.setLastScanned(metadata.getScanned());
    }
    LOG.info("Updated highscore data for " + game.getGameDisplayName());
    highscoreRepository.saveAndFlush(h);
    return metadata;
  }

  @NonNull
  public List<Score> parseScores(Date createdAt, String raw, int gameId) {
    return highscoreParser.parseScores(createdAt, raw, gameId);
  }

  @NonNull
  public List<RankedPlayer> getPlayersByRanks() {
    Map<String, RankedPlayer> playerMap = new HashMap<>();
    List<ScoreSummary> highscoresWithScore = getHighscoresWithScore();
    for (ScoreSummary summary : highscoresWithScore) {
      if (summary.getScores().size() >= 3) {
        List<Score> scores = summary.getScores();
        for (int i = 0; i < scores.size(); i++) {
          Score score = scores.get(i);
          if (score.getPlayer() == null) {
            continue;
          }

          if (!playerMap.containsKey(score.getPlayerInitials())) {
            RankedPlayer p = new RankedPlayer();
            Player player = score.getPlayer();
            p.setAvatarUrl(player.getAvatarUrl());
            if (player.getAvatar() != null) {
              p.setAvatarUuid(player.getAvatar().getUuid());
            }
            p.setName(player.getName());
            playerMap.put(score.getPlayerInitials(), p);
          }

          RankedPlayer player = playerMap.get(score.getPlayerInitials());
          player.addBy(i);
        }
      }
    }

    List<RankedPlayer> rankedPlayers = new ArrayList<>(playerMap.values());
    Collections.sort(rankedPlayers, (o2, o1) -> {
      if (o1.getFirst() == o2.getFirst()) {
        if (o1.getSecond() == o2.getSecond()) {
          return o1.getThird() - o2.getThird();
        }
        else {
          return o1.getSecond() - o2.getSecond();
        }
      }
      return o1.getFirst() - o2.getFirst();
    });

    for (int i = 1; i <= rankedPlayers.size(); i++) {
      rankedPlayers.get(i - 1).setRank(i);
    }

    return rankedPlayers;
  }

  public List<ScoreSummary> getHighscoresWithScore() {
    List<ScoreSummary> result = new ArrayList<>();
    List<Highscore> byRawIsNotNull = highscoreRepository.findByRawIsNotNull();
    for (Highscore highscore : byRawIsNotNull) {
      List<Score> scores = highscoreParser.parseScores(highscore.getLastModified(), highscore.getRaw(), highscore.getGameId());
      result.add(new ScoreSummary(scores, highscore.getCreatedAt()));
    }
    return result;
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
    Optional<Highscore> highscore = highscoreRepository.findByGameIdAndCreatedAtBetweenAndRawIsNotNull(gameId, start, end);
    if (end.after(new Date())) {
      highscore = highscoreRepository.findByGameId(gameId);
    }

    if (highscore.isPresent()) {
      Highscore h = highscore.get();
      ScoreSummary scoreSummary = getScoreSummary(h.getCreatedAt(), h.getRaw(), gameId);
      scoreList.setLatestScore(scoreSummary);
      scoreList.getScores().add(scoreSummary);
    }

    List<HighscoreVersion> byGameIdAndCreatedAtBetween = highscoreVersionRepository.findByGameIdAndCreatedAtBetween(gameId, start, end);
//    List<HighscoreVersion> byGameIdAndCreatedAtBetween = highscoreVersionRepository.findByGameId(gameId);
    for (HighscoreVersion version : byGameIdAndCreatedAtBetween) {
      ScoreSummary scoreSummary = getScoreSummary(version.getCreatedAt(), version.getOldRaw(), gameId);
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
    List<Highscore> all = highscoreRepository.findAllByOrderByCreatedAtDesc();
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
   * @param gameId the game to retrieve the highscores for
   * @return all highscores of the given player
   */
  public ScoreSummary getHighscores(int gameId) {
    ScoreSummary summary = new ScoreSummary(new ArrayList<>(), new Date());
    Optional<Highscore> highscore = highscoreRepository.findByGameId(gameId);
    if (highscore.isPresent()) {
      Highscore h = highscore.get();
      if (!StringUtils.isEmpty(h.getRaw())) {
        List<Score> scores = parseScores(h.getCreatedAt(), h.getRaw(), gameId);
        summary.setRaw(h.getRaw());
        summary.getScores().addAll(scores);
      }

      HighscoreMetadata metadata = new HighscoreMetadata();
      metadata.setModified(h.getLastModified());
      metadata.setScanned(h.getLastScanned());
      metadata.setFilename(h.getFilename());
      metadata.setType(h.getType());
      metadata.setStatus(h.getStatus());
      summary.setMetadata(metadata);
    }
    return summary;
  }

  /**
   * Used for the dashboard widget to show the list of newly created highscores
   */
  public List<Score> getAllHighscoreVersions() {
    List<Score> scores = new ArrayList<>();
    List<HighscoreVersion> all = highscoreVersionRepository.findAllByOrderByCreatedAtDesc();
    for (HighscoreVersion version : all) {
      List<Score> versionScores = highscoreParser.parseScores(version.getCreatedAt(), version.getNewRaw(), version.getGameId());
      int changedPos = version.getChangedPosition() - 1;
      if(version.getChangedPosition() < 0 || version.getChangedPosition() >= versionScores.size()) {
        LOG.error("Found invalid change position " + version.getChangedPosition() + "' for " + version);
      }
      else {
        scores.add(versionScores.get(changedPos));
      }

    }
    return scores;
  }

  public List<Score> parseScores(Highscore highscore) {
    return highscoreParser.parseScores(highscore.getLastModified(), highscore.getRaw(), highscore.getGameId());
  }

  public List<Highscore> getRecentHighscores() {
    return highscoreRepository.findRecent();
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

    HighscoreMetadata metadata = highscoreResolver.readHighscore(game);
    String rawHighscore = metadata.getRaw();
    if (StringUtils.isEmpty(rawHighscore)) {
      LOG.info("Skipped highscore changed event for {} because the raw data of the score is empty.", game);
      return;
    }

    this.updateHighscore(game, metadata);
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

  @VisibleForTesting
  protected void updateHighscore(@NonNull Game game, @NonNull HighscoreMetadata metadata) {
    String rawHighscore = metadata.getRaw();
    Highscore newHighscore = Highscore.forGame(game, metadata);
    Optional<Highscore> existingHighscore = highscoreRepository.findByGameId(game.getId());

    if (existingHighscore.isEmpty() || !existingHighscore.get().getRaw().equals(rawHighscore)) {
      //save the highscore for the first time
      if (existingHighscore.isEmpty()) {
        highscoreRepository.saveAndFlush(newHighscore);
        LOG.info("Saved highscore for " + game);
      }
      else {
        //archive old existingScore
        Highscore existingScore = existingHighscore.get();

        List<Score> oldScores = highscoreParser.parseScores(existingScore.getLastModified(), existingScore.getRaw(), game.getId());
        List<Score> newScores = highscoreParser.parseScores(newHighscore.getLastModified(), newHighscore.getRaw(), game.getId());

        int position = calculateChangedPosition(oldScores, newScores);
        if(position == -1) {
          LOG.info("No highscore change detected for " + game + ", skipping highscore notification event.");
          return;
        }

        HighscoreVersion version = existingScore.toVersion(position);
        version.setNewRaw(rawHighscore);
        highscoreVersionRepository.saveAndFlush(version);

        //update existing one
        existingScore.setRaw(rawHighscore);
        existingScore.setCreatedAt(new Date());
        highscoreRepository.saveAndFlush(existingScore);

        LOG.info("Archived old existingScore and saved updated existingScore for " + game);

        Score oldScore = oldScores.get(position - 1);
        Score newScore = newScores.get(position - 1);
        HighscoreChangeEvent event = createEvent(game, existingHighscore.get(), newHighscore, oldScore, newScore);
        triggerHighscoreChange(event);
      }
    }
    else {
      LOG.info("Skipped highscore change event for {} because the raw highscore data did not change.", game);
    }
  }

  private int calculateChangedPosition(@NonNull List<Score> oldScores, @NonNull List<Score> newScores) {
    for (int i = 0; i < oldScores.size(); i++) {
      if (!oldScores.get(i).getScore().equalsIgnoreCase(newScores.get(i).getScore())) {
        LOG.info("Calculated changed score: " + newScores.get(i) + ", old score was " + oldScores.get(i));
        return i + 1;
      }
    }
    return -1;
  }

  private void triggerHighscoreChange(@NonNull HighscoreChangeEvent event) {
    new Thread(() -> {
      for (HighscoreChangeListener listener : listeners) {
        listener.highscoreChanged(event);
      }
    }).start();
  }

  private HighscoreChangeEvent createEvent(@NonNull Game game, @NonNull Highscore existingHighscore, @NonNull Highscore newHighscore, @NonNull Score oldScore, @NonNull Score newScore) {
    return new HighscoreChangeEvent() {
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
        return newHighscore;
      }

      @Override
      public Score getOldScore() {
        return oldScore;
      }

      @Override
      public Score getNewScore() {
        return newScore;
      }
    };
  }

  @Override
  public void afterPropertiesSet() {
    this.highscoreResolver = new HighscoreResolver(systemService);
  }

}
