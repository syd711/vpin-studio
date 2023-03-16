package de.mephisto.vpin.server.highscores;

import com.google.common.annotations.VisibleForTesting;
import de.mephisto.vpin.commons.HighscoreType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.CompetitionsRepository;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import de.mephisto.vpin.server.vpa.VpaExporterJob;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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
  private CompetitionsRepository competitionsRepository;

  @Autowired
  private HighscoreParser highscoreParser;

  @Autowired
  private PreferencesService preferencesService;

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

  public Optional<Highscore> getHighscore(int gameId) {
    return highscoreRepository.findByGameId(gameId);
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
      h.setType(metadata.getType() != null ? metadata.getType().name() : null);
      h.setRaw(metadata.getRaw());
      h.setStatus(metadata.getStatus());
      h.setLastScanned(metadata.getScanned());
    }
    LOG.info("Updated highscore data for " + game.getGameDisplayName());
    highscoreRepository.saveAndFlush(h);
    return metadata;
  }

  public boolean resetHighscore(@NonNull Game game, boolean deleteHistory) {
    HighscoreType highscoreType = game.getHighscoreType();
    boolean result = false;
    if (highscoreType != null) {
      switch (highscoreType) {
        case EM: {
          result = game.getEMHighscoreFile() != null && game.getEMHighscoreFile().exists() && game.getEMHighscoreFile().delete();
          break;
        }
        case NVRam: {
          result = game.getNvRamFile().exists() && game.getNvRamFile().delete();//TODO unify game file access
          break;
        }
        case VPReg: {
          VPReg reg = new VPReg(systemService.getVPRegFile(), game);
          result = reg.resetHighscores();
          break;
        }
        default: {
          LOG.error("No matching highscore type found for '" + highscoreType + "'");
        }
      }
    }
    else {
      result = true;
    }
    deleteScores(game.getId(), deleteHistory);
    return result;
  }

  @NonNull
  public List<Score> parseScores(Date createdAt, String raw, int gameId, long serverId) {
    return highscoreParser.parseScores(createdAt, raw, gameId, serverId);
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
            p.setInitials(player.getInitials());
            p.setCompetitionsWon(competitionsRepository.findByWinnerInitials(player.getInitials()).size());
            playerMap.put(score.getPlayerInitials(), p);
          }

          RankedPlayer player = playerMap.get(score.getPlayerInitials());
          player.addBy(i);
        }
      }
    }

    String rankingPoints = (String) preferencesService.getPreferenceValue(PreferenceNames.RANKING_POINTS, "4,2,1,0");
    List<Integer> points = Arrays.stream(rankingPoints.split(",")).map(Integer::parseInt).collect(Collectors.toList());

    List<RankedPlayer> rankedPlayers = new ArrayList<>(playerMap.values());
    for (RankedPlayer rankedPlayer : rankedPlayers) {
      rankedPlayer.setPoints(rankedPlayer.getPoints() + (points.get(0) * rankedPlayer.getFirst()));
      rankedPlayer.setPoints(rankedPlayer.getPoints() + (points.get(1) * rankedPlayer.getSecond()));
      rankedPlayer.setPoints(rankedPlayer.getPoints() + (points.get(2) * rankedPlayer.getThird()));
      rankedPlayer.setPoints(rankedPlayer.getPoints() + (points.get(3) * rankedPlayer.getCompetitionsWon()));
    }

    rankedPlayers.sort((o2, o1) -> o1.getPoints() - o2.getPoints());
    for (int i = 1; i <= rankedPlayers.size(); i++) {
      rankedPlayers.get(i - 1).setRank(i);
    }
    return rankedPlayers;
  }

  public List<ScoreSummary> getHighscoresWithScore() {
    List<ScoreSummary> result = new ArrayList<>();
    List<Highscore> byRawIsNotNull = highscoreRepository.findByRawIsNotNull();
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    for (Highscore highscore : byRawIsNotNull) {
      List<Score> scores = highscoreParser.parseScores(highscore.getLastModified(), highscore.getRaw(), highscore.getGameId(), serverId);
      result.add(new ScoreSummary(scores, highscore.getCreatedAt()));
    }
    return result;
  }

  public List<HighscoreVersion> getAllHighscoreVersions(int gameId) {
    return highscoreVersionRepository.findByGameIdAndCreatedAtBetween(gameId, new Date(0), new Date());
  }

  public ScoreList getScoreHistory(int gameId) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    return getScoresBetween(gameId, new Date(0), new Date(), serverId);
  }

  /**
   * Returns all available scores for the game with the given id and time frame
   */
  public ScoreList getScoresBetween(int gameId, Date start, Date end, long serverId) {
    ScoreList scoreList = new ScoreList();

    Date latestVersion = null;
    List<HighscoreVersion> byGameIdAndCreatedAtBetween = highscoreVersionRepository.findByGameIdAndCreatedAtBetween(gameId, start, end);
    for (HighscoreVersion version : byGameIdAndCreatedAtBetween) {
      latestVersion = version.getCreatedAt();
      ScoreSummary scoreSummary = getScoreSummary(version.getCreatedAt(), version.getOldRaw(), gameId, serverId);
      scoreList.getScores().add(scoreSummary);
    }

    Optional<Highscore> highscore = highscoreRepository.findByGameId(gameId);
    if (highscore.isPresent()) {
      Highscore h = highscore.get();
      if (h.getRaw() != null) {
        //set the latest score date here for the existing score list, because the current highscore is the latest!
        Date latestScoreTime = h.getLastModified();
        if (latestVersion != null) {
          latestScoreTime.setTime(latestVersion.getTime() + 1);
        }//TODO only use version

        ScoreSummary scoreSummary = getScoreSummary(h.getLastModified(), h.getRaw(), gameId, serverId);
//        scoreSummary.setCreatedAt(latestScoreTime);
        scoreList.setLatestScore(scoreSummary);
        scoreList.getScores().add(0, scoreSummary);
      }
    }

    scoreList.getScores().sort(Comparator.comparing(ScoreSummary::getCreatedAt));
    return scoreList;
  }

  public ScoreSummary getAllHighscoresForPlayer(String initials) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    return getAllHighscoresForPlayer(serverId, initials);
  }

  /**
   * Returns a list of all scores that are available for the player with the given initials
   *
   * @param initials the initials to filter for
   * @return all highscores of the given player
   */
  public ScoreSummary getAllHighscoresForPlayer(long serverId, String initials) {
    ScoreSummary summary = new ScoreSummary(new ArrayList<>(), new Date());
    List<Highscore> all = highscoreRepository.findAllByOrderByCreatedAtDesc();
    for (Highscore highscore : all) {
      if (StringUtils.isEmpty(highscore.getRaw())) {
        continue;
      }

      List<Score> scores = parseScores(highscore.getCreatedAt(), highscore.getRaw(), highscore.getGameId(), serverId);
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
   * @param gameId      the game to retrieve the highscores for
   * @param displayName the optional display name/name of the table the summary is for
   * @return all highscores of the given player
   */
  @NonNull
  public ScoreSummary getScoreSummary(long serverId, int gameId, @Nullable String displayName) {
    ScoreSummary summary = new ScoreSummary(new ArrayList<>(), new Date());
    Optional<Highscore> highscore = highscoreRepository.findByGameId(gameId);
    if (highscore.isPresent()) {
      Highscore h = highscore.get();
      if (!StringUtils.isEmpty(h.getRaw())) {
        List<Score> scores = parseScores(h.getCreatedAt(), h.getRaw(), gameId, serverId);
        summary.setRaw(h.getRaw());
        summary.getScores().addAll(scores);
      }

      HighscoreMetadata metadata = new HighscoreMetadata();
      metadata.setDisplayName(displayName);
      metadata.setModified(h.getLastModified());
      metadata.setScanned(h.getLastScanned());
      metadata.setFilename(h.getFilename());
      metadata.setType(h.getType() != null ? HighscoreType.valueOf(h.getType()) : null);
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
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);

    for (HighscoreVersion version : all) {
      List<Score> versionScores = highscoreParser.parseScores(version.getCreatedAt(), version.getNewRaw(), version.getGameId(), serverId);
      //change positions start with 1!
      if (version.getChangedPosition() < 0 || version.getChangedPosition() > versionScores.size()) {
        LOG.error("Found invalid change position '" + version.getChangedPosition() + "' for " + version);
      }
      else {
        int changedPos = version.getChangedPosition() - 1;
        scores.add(versionScores.get(changedPos));
      }

    }
    return scores;
  }

  public void addHighscoreChangeListener(@NonNull HighscoreChangeListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Returns true when the new highscore contains a higher value, than the folder one.
   *
   * @param game the game that should be updated
   */
  public HighscoreMetadata updateHighscore(@NonNull Game game) {
    HighscoreMetadata metadata = highscoreResolver.readHighscore(game);
    String rawHighscore = metadata.getRaw();
    if (StringUtils.isEmpty(rawHighscore)) {
      LOG.info("Skipped highscore changed event for {} because the raw data of the score is empty.", game);
      return metadata;
    }

    this.updateHighscore(game, metadata);
    return metadata;
  }

  /**
   * Collects a list of highscores for serialization
   *
   * @param createdAt the date the highscores have been created
   * @param raw       the raw data
   * @param gameId    the gameId of the game
   * @param serverId  the discord server id
   */
  private ScoreSummary getScoreSummary(Date createdAt, String raw, int gameId, long serverId) {
    List<Score> scores = parseScores(createdAt, raw, gameId, serverId);
    if (scores.size() > 0) {
      return new ScoreSummary(scores, createdAt);
    }
    return null;
  }

  @VisibleForTesting
  protected void updateHighscore(@NonNull Game game, @NonNull HighscoreMetadata metadata) {
    Highscore newHighscore = Highscore.forGame(game, metadata);
    Optional<Highscore> existingHighscore = highscoreRepository.findByGameId(game.getId());

    if (StringUtils.isEmpty(metadata.getRaw())) {
      LOG.info("Skipped highscore change event for {} because the raw highscore data does not exist.", game);
      return;
    }

    //check if the table highscore file was ever written
    if (existingHighscore.isEmpty()) {
      newHighscore = highscoreRepository.saveAndFlush(newHighscore);
      LOG.info("Saved initial highscore for " + game);
      notifyInitialHighscoreChange(newHighscore, game);
    }
    else {
      runHighscoreChangedCheck(existingHighscore.get(), newHighscore, game);
    }
  }

  /**
   * This is the first highscore created, so we always declare it as a change.
   */
  private void notifyInitialHighscoreChange(@NonNull Highscore newHighscore, @NonNull Game game) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    List<Score> newScores = highscoreParser.parseScores(newHighscore.getLastModified(), newHighscore.getRaw(), game.getId(), serverId);
    HighscoreChangeEvent event = new HighscoreChangeEvent(game, newScores.get(1), newScores.get(0), newScores.size());
    triggerHighscoreChange(event);
  }

  /**
   * Calculates if there is a difference between the old and new highscore.
   *
   * @param oldHighscore
   * @param newHighscore
   * @param game
   */
  private void runHighscoreChangedCheck(Highscore oldHighscore, Highscore newHighscore, Game game) {
    //check if there is a difference
    String oldRaw = oldHighscore.getRaw();
    String newRaw = newHighscore.getRaw();
    if (oldRaw != null && oldRaw.equals(newRaw)) {
      LOG.info("Skipped highscore change event for {} because the no score change detected.", game);
      return;
    }

    /*
     * Diff calculation:
     * Note that this only determines if the highscore has changed locally and a change event should be fired.
     */
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    List<Score> newScores = highscoreParser.parseScores(newHighscore.getLastModified(), newHighscore.getRaw(), game.getId(), serverId);
    List<Score> oldScores = getOrCloneOldHighscores(oldHighscore, game, oldRaw, serverId, newScores);

    List<Integer> changedPositions = calculateChangedPositions(oldScores, newScores);
    if (changedPositions.isEmpty()) {
      LOG.info("No highscore change detected for " + game + ", skipping highscore notification event.");
      return;
    }

    for (Integer changedPosition : changedPositions) {
      //so we have a highscore update, let's decide the distribution
      Score oldScore = oldScores.get(changedPosition - 1);
      Score newScore = newScores.get(changedPosition - 1);

      //archive old existingScore only if it had actual data
      if (!StringUtils.isEmpty(oldRaw)) {
        HighscoreVersion version = oldHighscore.toVersion(changedPosition);
        version.setNewRaw(newHighscore.getRaw());
        highscoreVersionRepository.saveAndFlush(version);
        LOG.info("Created highscore version for " + game);
      }

      //update existing one
      oldHighscore.setRaw(newHighscore.getRaw());
      oldHighscore.setType(newHighscore.getType());
      oldHighscore.setLastScanned(newHighscore.getLastScanned());
      oldHighscore.setLastModified(newHighscore.getLastModified());
      oldHighscore.setFilename(newHighscore.getFilename());
      oldHighscore.setStatus(null);
      oldHighscore.setDisplayName(newHighscore.getDisplayName());
      highscoreRepository.saveAndFlush(oldHighscore);
      LOG.info("Saved updated highscore for " + game);

      //finally, fire the update event to notify all listeners
      HighscoreChangeEvent event = new HighscoreChangeEvent(game, oldScore, newScore, oldScores.size());
      triggerHighscoreChange(event);
    }
  }

  /**
   * The old highscore may be empty if a competitions did reset them.
   */
  @NotNull
  private List<Score> getOrCloneOldHighscores(Highscore oldHighscore, Game game, String oldRaw, long serverId, List<Score> newScores) {
    List<Score> oldScores = new ArrayList<>();
    if (oldRaw != null) {
      oldScores = highscoreParser.parseScores(oldHighscore.getLastModified(), oldHighscore.getRaw(), game.getId(), serverId);
    }
    else {
      for (Score newScore : newScores) {
        Score score = new Score(newScore.getCreatedAt(), newScore.getGameId(), "???", null, "0", 0, newScore.getPosition());
        oldScores.add(score);
      }
    }
    return oldScores;
  }

  private void deleteScores(int gameId, boolean deleteHistory) {
    Optional<Highscore> byGameId = highscoreRepository.findByGameId(gameId);
    byGameId.ifPresent(highscore -> highscoreRepository.delete(highscore));
    LOG.info("Deleted latest highscore for " + gameId);

    if (deleteHistory) {
      List<HighscoreVersion> versions = highscoreVersionRepository.findByGameId(gameId);
      highscoreVersionRepository.deleteAll(versions);
      LOG.info("Deleted all highscore versions for " + gameId);
    }
  }

  /**
   * Returns the highscore difference position, starting from 1.
   */
  public List<Integer> calculateChangedPositions(@NonNull List<Score> oldScores, @NonNull List<Score> newScores) {
    int count = oldScores.size();
    List<Integer> changes = new ArrayList<>();
    int offset = 0;
    for (int i = 0; i < count; i++) {
      if (offset == count) {
        break;
      }

      if (!oldScores.get(i).equals(newScores.get(offset))) {
        Score score = newScores.get(offset);
        if (!score.getPlayerInitials().equals("???") && score.getNumericScore() > 0) {
          LOG.info("Calculated changed score: [" + newScores.get(offset) + "] has beaten [" + oldScores.get(i) + "]");
          changes.add(offset + 1);
          i--;
        }
      }

      offset++;
    }
    return changes;
  }

  public int calculateChangedPositionByScore(@NonNull List<Score> oldScores, @NonNull Score newScore) {
    for (int i = 0; i < oldScores.size(); i++) {
      if (oldScores.get(i).getNumericScore() < newScore.getNumericScore()) {
        LOG.info("Calculated changed score at position " + (i + 1) + ": [" + newScore + "] has beaten [" + oldScores.get(i) + "]");
        return i + 1;
      }
    }
    return -1;
  }

  private void triggerHighscoreChange(@NonNull HighscoreChangeEvent event) {
    for (HighscoreChangeListener listener : listeners) {
      listener.highscoreChanged(event);
    }
  }

  @Override
  public void afterPropertiesSet() {
    this.highscoreResolver = new HighscoreResolver(systemService);
  }

  public void importScoreEntry(Game game, VpaExporterJob.ScoreVersionEntry score) {
    HighscoreVersion version = new HighscoreVersion();
    version.setNewRaw(score.getNewRaw());
    version.setOldRaw(score.getOldRaw());
    version.setGameId(game.getId());
    version.setChangedPosition(score.getChangedPosition());
    version.setCreatedAt(score.getCreatedAt());
    version.setDisplayName(game.getGameDisplayName());

    Optional<HighscoreVersion> byCreatedAt = highscoreVersionRepository.findByCreatedAt(score.getCreatedAt());
    if (byCreatedAt.isEmpty()) {
      HighscoreVersion saved = highscoreVersionRepository.saveAndFlush(version);
      LOG.info("Imported " + saved);
    }
  }
}
