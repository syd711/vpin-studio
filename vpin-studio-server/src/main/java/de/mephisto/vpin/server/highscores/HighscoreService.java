package de.mephisto.vpin.server.highscores;

import com.google.common.annotations.VisibleForTesting;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.competitions.CompetitionsRepository;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.nvrams.NVRamService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HighscoreService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreService.class);

  @Autowired
  private HighscoreRepository highscoreRepository;

  @Autowired
  private HighscoreVersionRepository highscoreVersionRepository;

  @Autowired
  private CompetitionsRepository competitionsRepository;

  @Autowired
  private HighscoreParsingService highscoreParser;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private NVRamService nvRamService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private ScoreFilter scoreFilter;

  @Autowired
  private HighscoreResolver highscoreResolver;

  // manually injected
  private GameService gameService;

  private boolean pauseHighscoreEvents;

  private final List<HighscoreChangeListener> listeners = new ArrayList<>();
  private final List<String> vpRegEntries = new ArrayList<>();
  private final List<String> highscoreFiles = new ArrayList<>();

  public File getHighscoreFile(@NonNull Game game) {
    return highscoreResolver.getHighscoreFile(game);
  }

  public HighscoreFiles getHighscoreFiles(@NonNull Game game) {
    HighscoreFiles highscoreFiles = new HighscoreFiles();

    File vpRegFile = game.getEmulator().getVPRegFile();
    if (vpRegFile.exists()) {
      VPReg reg = new VPReg(vpRegFile);
      highscoreFiles.setVpRegEntries(reg.getEntries());
    }

    File userFolder = game.getEmulator().getUserFolder();
    if (userFolder.exists()) {
      File[] files = userFolder.listFiles((dir, name) -> name.endsWith(".txt"));
      if (files != null) {
        highscoreFiles.setTextFiles(Arrays.stream(files).map(File::getName).collect(Collectors.toList()));
      }
    }

    File nvramFolder = highscoreResolver.getNvRamFile(game);
    if (nvramFolder != null && nvramFolder.exists()) {
      File[] files = nvramFolder.listFiles((dir, name) -> name.endsWith(".nv"));
      if (files != null) {
        highscoreFiles.setNvRams(Arrays.stream(files).map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList()));
      }
    }
    return highscoreFiles;
  }

  public boolean resetHighscore(@NonNull Game game) {
    return resetHighscore(game, 0);
  }

  public boolean resetHighscore(@NonNull Game game, long score) {
    try {
      setPauseHighscoreEvents(true);
      HighscoreType highscoreType = game.getHighscoreType();
      boolean result = false;
      if (highscoreType != null) {
        switch (highscoreType) {
          case EM: {
            result = highscoreResolver.deleteTextScore(game, score);
            break;
          }
          case NVRam: {
            File nvRamFile = highscoreResolver.getNvRamFile(game);
            result = nvRamFile == null || !nvRamFile.exists() || nvRamFile.delete();
            break;
          }
          case Ini: {
            result = highscoreResolver.deleteIniScore(game, score);
            break;
          }
          case VPReg: {
            VPReg reg = new VPReg(game.getEmulator().getVPRegFile(), game.getRom(), game.getTableName());
            result = reg.resetHighscores(score);
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

      //always check nvram in case the highscore type was not determined
      NVRamList nvRamList = nvRamService.getResettedNVRams();
      if (nvRamList.contains(game.getRom()) || nvRamList.contains(game.getTableName())) {
        File nvRamFile = highscoreResolver.getNvRamFile(game);
        if (nvRamFile != null && !nvRamService.copyResettedNvRam(nvRamFile)) {
          result = false;
        }
      }

      deleteScores(game.getId(), true);
      scanScore(game, EventOrigin.USER_INITIATED);
      return result;
    }
    catch (Exception e) {
      LOG.error("Failed to reset highscore: " + e.getMessage(), e);
    }
    finally {
      setPauseHighscoreEvents(false);
    }
    return false;
  }


  public void deleteHighscore(Game game) {
    resetHighscore(game);

    File highscoreTextFile = highscoreResolver.getHighscoreTextFile(game);
    if (highscoreTextFile != null && highscoreTextFile.exists()) {
      highscoreTextFile.delete();
    }

    File highscoreIniFile = highscoreResolver.getHighscoreIniFile(game);
    if (highscoreIniFile != null && highscoreIniFile.exists()) {
      highscoreIniFile.delete();
    }
  }

  @NonNull
  public List<Score> parseScores(Date createdAt, String raw, @NonNull Game game, long serverId) {
    return highscoreParser.parseScores(createdAt, raw, game, serverId);
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
      Game game = gameService.getGame(highscore.getGameId());
      if (game == null) {
        continue;
      }
      List<Score> scores = highscoreParser.parseScores(highscore.getLastModified(), highscore.getRaw(), game, serverId);
      result.add(new ScoreSummary(scores, highscore.getCreatedAt(), highscore.getRaw()));
    }
    return result;
  }

  public ScoreList getScoreHistory(@NonNull Game game) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    return getScoresBetween(game, new Date(0), new Date(), serverId);
  }

  /**
   * Returns all available scores for the game with the given id and time frame
   */
  public ScoreList getScoresBetween(@NonNull Game game, Date start, Date end, long serverId) {
    ScoreList scoreList = new ScoreList();
    List<HighscoreVersion> byGameIdAndCreatedAtBetween = highscoreVersionRepository.findByGameIdAndCreatedAtBetween(game.getId(), start, end);
    for (HighscoreVersion version : byGameIdAndCreatedAtBetween) {
      ScoreSummary scoreSummary = null;
      List<Score> scores = parseScores(version.getCreatedAt(), version.getNewRaw(), game, serverId);
      if (scores.size() > 0) {
        scoreSummary = new ScoreSummary(scores, version.getCreatedAt(), version.getNewRaw());
      }
      if (scoreSummary != null) {
        scoreList.getScores().add(scoreSummary);
      }
      else {
        LOG.warn("Failed to create score history summary for version with id '" + version.getId() + "', may happen because of older unsupported formats.");
      }
    }
    scoreList.getScores().sort(Comparator.comparing(ScoreSummary::getCreatedAt));

    if (!scoreList.getScores().isEmpty()) {
      scoreList.setLatestScore(scoreList.getScores().get(0));
    }
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
    ScoreSummary summary = new ScoreSummary();
    List<Highscore> all = highscoreRepository.findAllByOrderByCreatedAtDesc();
    for (Highscore highscore : all) {
      if (StringUtils.isEmpty(highscore.getRaw())) {
        continue;
      }
      Game game = gameService.getGame(highscore.getGameId());
      if (game == null) {
        continue;
      }
      List<Score> scores = parseScores(highscore.getCreatedAt(), highscore.getRaw(), game, serverId);
      for (Score score : scores) {
        String playerInitials = score.getPlayerInitials();
        String altPlayerInitials = playerInitials.replaceAll(" ", "@");
        if (playerInitials.equalsIgnoreCase(initials) || altPlayerInitials.equalsIgnoreCase(initials)) {
          //FIXME Why ?????? considering parseScores() imposes a non null game and score is forcibly from the parsed game
          Game _game = frontendService.getOriginalGame(score.getGameId());
          if (_game == null && score.getGameId() > 0) {
            deleteScores(score.getGameId(), true);
            continue;
          }
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
  @NonNull
  public ScoreSummary getScoreSummary(long serverId, @Nullable Game game) {
    ScoreSummary summary = new ScoreSummary();
    if (game != null) {
      Optional<Highscore> highscore = highscoreRepository.findByGameId(game.getId());
      if (highscore.isPresent()) {
        Highscore h = highscore.get();
        if (!StringUtils.isEmpty(h.getRaw())) {
          List<Score> scores = parseScores(h.getCreatedAt(), h.getRaw(), game, serverId);
          summary.setRaw(h.getRaw());
          summary.getScores().addAll(scores);
        }

        if (summary.getScores().size() < 5) {
          List<HighscoreVersion> highscoreVersions = highscoreVersionRepository.findByGameIdOrderByCreatedAtDesc(game.getId());
          for (HighscoreVersion highscoreVersion : highscoreVersions) {
            List<Score> scores = parseScores(highscoreVersion.getCreatedAt(), highscoreVersion.getNewRaw(), game, serverId);
            summary.addScores(scores);
          }
          summary.sortScores();
          summary.setLimit(5);
        }
      }
    }
    return summary;
  }

  public ScoreSummary getScoreSummaryWithDates(long serverId, @NonNull Game game) {
    ScoreSummary summary = getScoreSummary(serverId, game);
    List<Score> scores = summary.getScores();
    if (!scores.isEmpty()) {
      List<HighscoreVersion> versions = getHighscoreVersionsByGame(game.getId());
      // to avoid parsing a version multiple time, use a local cache
      Map<HighscoreVersion, List<Score>> parsedVersions = new HashMap<>(versions.size());

      // for each line of scores, check when it has been lastly changed
      int offset = 0;
      for (int pos = 0; pos < scores.size(); pos++) {
        Score newScore = scores.get(pos);
        boolean found = false;
        for (HighscoreVersion version : versions) {
          //change positions start with 1!
          int changedPos = version.getChangedPosition() - 1;
          // last version where line has been changed
          // As better score could have been inserted before, check changedPos <= pos
          if (((pos - offset) <= changedPos) && (changedPos <= pos)) {
            List<Score> versionScores = parsedVersions.get(version);
            if (versionScores == null) {
              versionScores = highscoreParser.parseScores(version.getCreatedAt(), version.getNewRaw(), game, serverId);
              parsedVersions.put(version, versionScores);
            }
            Score oldScore = versionScores.get(changedPos);
            // verify same score and same player
            if (newScore.matches(oldScore)) {
              found = true;
              offset++;
              newScore.setCreatedAt(version.getCreatedAt());
              // found score so stop iteration
              break;
            }
          }
        }
        // empty the date as it is the last modification date of the Highscore
        if (!found) {
          newScore.setCreatedAt(null);
        }
      }
    }
    return summary;
  }


  public List<HighscoreVersion> getHighscoreVersionsByGame(int gameId) {
    return highscoreVersionRepository.findByGameIdOrderByCreatedAtDesc(gameId);
  }

  /**
   * Used for the dashboard widget to show the list of newly created highscores
   */
  public List<Score> getAllHighscoreVersions(@Nullable String initials) {
    List<Score> scores = new ArrayList<>();
    List<HighscoreVersion> all;
    if (!StringUtils.isEmpty(initials)) {
      all = highscoreVersionRepository.findAllByInitials(initials);
    }
    else {
      all = highscoreVersionRepository.findAllLimited();
    }

    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    for (HighscoreVersion version : all) {
      Game game = gameService.getGame(version.getGameId());
      if (game == null) {
        continue;
      }

      List<Score> versionScores = highscoreParser.parseScores(version.getCreatedAt(), version.getNewRaw(), game, serverId);
      //change positions start with 1!
      if (version.getChangedPosition() > versionScores.size()) {
        LOG.error("Found invalid change position '" + version.getChangedPosition() + "' for " + version);
      }
      else {
        int changedPos = version.getChangedPosition() - 1;
        scores.add(versionScores.get(changedPos));
      }

    }
    return scores;
  }

  //--------------------------------------------------------------------

  @NonNull
  public Optional<Highscore> getHighscore(@NonNull Game game, boolean forceScan, EventOrigin eventOrigin) {
    Optional<Highscore> highscore = Optional.empty();
    try {
      highscore = highscoreRepository.findByGameId(game.getId());
      if (forceScan) {
        if (highscore.isEmpty() && !StringUtils.isEmpty(game.getRom())) {
          HighscoreMetadata metadata = scanScore(game, eventOrigin);
          if (metadata != null) {
            return updateHighscore(game, metadata, eventOrigin);
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Error updating highscores for \"" + game.getGameDisplayName() + "\"/" + game.getId() + ": " + e.getMessage(), e);
    }
    return highscore;
  }

  @Nullable
  public HighscoreMetadata scanScore(@NonNull Game game, @NonNull EventOrigin eventOrigin) {
    if (!game.isVpxGame()) {
      SLOG.info("Game " + game.getGameDisplayName() + " is not a VPX game, highscore parsing cancelled.");
      LOG.info("Game " + game.getGameDisplayName() + " is not a VPX game, highscore parsing cancelled.");
      return null;
    }
    HighscoreMetadata highscoreMetadata = readHighscore(game);
    updateHighscore(game, highscoreMetadata, eventOrigin);
    return highscoreMetadata;
  }

  @NonNull
  public HighscoreMetadata readHighscore(@NonNull Game game) {
    return highscoreResolver.readHighscore(game);
  }

  /**
   * Rules for highscore versioning:
   * - a Highscore record is only created, when an RAW data is present
   * - for the first record Highscore, a version is created too with an empty OLD value
   * - for every newly recorded Highscore, an additional version is stored
   * The method must be synchronized because multiple UI threads access it after a competition creation.
   *
   * @param game        the game to update the highscore for
   * @param metadata    the extracted metadata from the system
   * @param eventOrigin the initiator of the scan, important for update emitting
   * @return the highscore optional if a score could be extracted
   */
  @VisibleForTesting
  protected synchronized Optional<Highscore> updateHighscore(@NonNull Game game, @NonNull HighscoreMetadata metadata, @NonNull EventOrigin eventOrigin) {
    //we don't do anything if not value is extract, this may lead to superflous system calls, but we have time
    if (StringUtils.isEmpty(metadata.getRaw())) {
      return Optional.empty();
    }

    boolean initialScore = false;

    //first highscore parsing situation: store the first record and the first version
    Optional<Highscore> existingHighscore = highscoreRepository.findByGameId(game.getId());
    if (existingHighscore.isEmpty()) {
      initialScore = true;
      Highscore newHighscore = Highscore.forGame(game, metadata);

      //create artificial empty init version
      List<Score> newScores = highscoreParser.parseScores(newHighscore.getLastModified(), newHighscore.getRaw(), game, -1);
      StringBuilder emptyRaw = new StringBuilder("HIGHEST SCORES\n");
      for (Score newScore : newScores) {
        emptyRaw.append("#");
        emptyRaw.append(newScore.getPosition());
        emptyRaw.append(" ");
        emptyRaw.append("???");
        emptyRaw.append("   ");
        emptyRaw.append("0");
        emptyRaw.append("\n");
      }
      newHighscore.setRaw(emptyRaw.toString());
      Highscore updatedNewHighScore = highscoreRepository.saveAndFlush(newHighscore);

      HighscoreVersion highscoreVersion = newHighscore.toVersion(-1, emptyRaw.toString());
      //!!! this is the first highscore version, so the old RAW value must be corrected to NULL
      highscoreVersion.setOldRaw(null);
      highscoreVersionRepository.saveAndFlush(highscoreVersion);

      existingHighscore = Optional.of(updatedNewHighScore);
      SLOG.info("Created new initial highscore record.");
    }

    Highscore newHighscore = Highscore.forGame(game, metadata);
    Highscore oldHighscore = existingHighscore.get();

    //check if there is a difference
    String oldRaw = oldHighscore.getRaw();
    String newRaw = newHighscore.getRaw();

    if (oldRaw == null || newRaw == null) {
      LOG.error("The highscore data of \"" + game.getGameDisplayName() + "\" has become invalid, no RAW data can be extracted anymore.");
      SLOG.info("The highscore data of \"" + game.getGameDisplayName() + "\" has become invalid, no RAW data can be extracted anymore.");
      return Optional.of(oldHighscore);
    }

    if (oldRaw.equals(newRaw)) {
      LOG.info("Skipped highscore change event for \"" + game.getRom() + "\" because there was no score change for rom '{}' detected.", game, game.getRom());
      SLOG.info("Skipped highscore change event for \"" + game.getRom() + "\" because there was no score change for rom '" + game.getRom() + "' detected.");
      return Optional.of(oldHighscore);
    }

    /*
     * Diff calculation:
     * Note that this only determines if the highscore has changed locally and a change event should be fired.
     */
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    List<Score> newScores = highscoreParser.parseScores(newHighscore.getLastModified(), newHighscore.getRaw(), game, serverId);
    List<Score> oldScores = getOrCloneOldHighscores(oldHighscore, game, oldRaw, serverId, newScores);
    List<HighscoreChangeEvent> highscoreChangeEvents = new ArrayList<>();
    if (!oldScores.isEmpty()) {
      List<Integer> changedPositions = calculateChangedPositions(game.getGameDisplayName(), oldScores, newScores);
      if (changedPositions.isEmpty()) {
        LOG.info("No highscore change of rom '" + game.getRom() + "' detected for " + game + ", skipping notification event.");
        SLOG.info("No highscore change of rom '" + game.getRom() + "' detected for " + game + ", skipping notification event.");
      }
      else {
        LOG.info("Calculated changed positions for '" + game.getRom() + "': " + changedPositions);
        SLOG.info("Calculated changed positions for '" + game.getRom() + "': " + changedPositions.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        if (!changedPositions.isEmpty()) {
          for (Integer changedPosition : changedPositions) {
            //so we have a highscore update, let's decide the distribution
            Score oldScore = oldScores.get(changedPosition - 1);
            Score newScore = newScores.get(changedPosition - 1);

            //archive old existingScore only if it had actual data
            if (!StringUtils.isEmpty(oldRaw)) {
              HighscoreVersion version = oldHighscore.toVersion(changedPosition, newRaw);
              highscoreVersionRepository.saveAndFlush(version);
              LOG.info("Created highscore version for " + game + ", changed position " + changedPosition);
              SLOG.info("Created highscore version for " + game + ", changed position " + changedPosition);
            }

            if (!scoreFilter.isScoreFiltered(newScore)) {
              //finally, fire the update event to notify all listeners
              HighscoreChangeEvent event = new HighscoreChangeEvent(game, oldScore, newScore, newRaw, oldScores.size(), initialScore, eventOrigin);
              highscoreChangeEvents.add(event);
            }
          }
        }
      }
    }

    /*
     * The old and the new highscores are not matching.
     * Even if no diff was calculated, we update the latest score and generated the highscore card.
     */
    oldHighscore.setRaw(newHighscore.getRaw());
    oldHighscore.setType(newHighscore.getType());
    oldHighscore.setLastScanned(newHighscore.getLastScanned());
    oldHighscore.setLastModified(newHighscore.getLastModified());
    oldHighscore.setFilename(newHighscore.getFilename());
    oldHighscore.setStatus(null);
    oldHighscore.setDisplayName(newHighscore.getDisplayName());
    highscoreRepository.saveAndFlush(oldHighscore);
    LOG.info("Saved updated highscore for " + game + " to Studio database.");
    SLOG.info("Saved updated highscore for " + game + " to Studio database.");

    triggerHighscoreChange(highscoreChangeEvents);

    triggerHighscoreUpdate(game, oldHighscore);

    return Optional.of(oldHighscore);
  }

  /**
   * The old highscore may be empty if a competitions did reset them.
   */
  @NonNull
  private List<Score> getOrCloneOldHighscores(Highscore oldHighscore, Game game, String oldRaw, long serverId, List<Score> newScores) {
    List<Score> oldScores = new ArrayList<>();
    if (oldRaw != null) {
      oldScores = highscoreParser.parseScores(oldHighscore.getLastModified(), oldHighscore.getRaw(), game, serverId);
    }
    else {
      for (Score newScore : newScores) {
        Score score = new Score(newScore.getCreatedAt(), newScore.getGameId(), "???", null, "0", 0, newScore.getPosition());
        oldScores.add(score);
      }
    }
    return oldScores;
  }

  public void deleteScores(int gameId, boolean deleteVersions) {
    Optional<Highscore> byGameId = highscoreRepository.findByGameId(gameId);
    byGameId.ifPresent(highscore -> highscoreRepository.deleteById(highscore.getId()));
    LOG.info("Deleted latest highscore for " + gameId);

    if (deleteVersions) {
      List<HighscoreVersion> versions = getHighscoreVersionsByGame(gameId);
      highscoreVersionRepository.deleteAll(versions);
      LOG.info("Deleted all highscore versions for " + gameId);
    }
  }

  /**
   * Returns the highscore difference position, starting from 1.
   */
  public List<Integer> calculateChangedPositions(@NonNull String gameDisplayName, @NonNull List<Score> oldScores, @NonNull List<Score> newScores) {
    List<Integer> changes = new ArrayList<>();
    try {
      for (int i = 0; i < newScores.size(); i++) {
        Score newScore = newScores.get(i);
        if (newScore.isSkipped()) {
          continue;
        }

        if (i >= oldScores.size()) {
          LOG.info("The number of score entries of the old scores and the new scores do differ: " + oldScores.size() + " vs. " + newScores.size() + ", this happens when resetted empty values are filtered.");
          continue;
        }


        boolean notFound = oldScores.stream().noneMatch(score -> score.matches(newScore));
        if (notFound) {
          changes.add(newScore.getPosition());
          LOG.info(gameDisplayName + ": Calculated changed score [" + newScore + "] has beaten [" + oldScores.get(newScore.getPosition() - 1) + "]");
        }
      }
    }
    catch (Exception e) {
      LOG.info("Failed to calculate score change: " + e.getMessage(), e);
    }
    return changes;
  }

  public int calculateChangedPositionByScore(@NonNull List<Score> oldScores, @NonNull Score newScore) {
    for (int i = 0; i < oldScores.size(); i++) {
      if (oldScores.get(i).getScore() < newScore.getScore()) {
        LOG.info("Calculated changed score at position " + (i + 1) + ": [" + newScore + "] has beaten [" + oldScores.get(i) + "]");
        return i + 1;
      }
    }
    return -1;
  }

  public void setPauseHighscoreEvents(boolean pauseHighscoreEvents) {
    this.pauseHighscoreEvents = pauseHighscoreEvents;
    LOG.info("Setting highscore change events to: " + pauseHighscoreEvents);
  }

  public void triggerHighscoreChange(@NonNull List<HighscoreChangeEvent> events) {
    if (pauseHighscoreEvents) {
      LOG.info("Skipping highscore change event because change events are paused.");
      return;
    }

    Collections.sort(events, (o1, o2) -> Long.compare(o2.getNewScore().getScore(), o1.getNewScore().getScore()));
    for (HighscoreChangeEvent event : events) {
      for (HighscoreChangeListener listener : listeners) {
        listener.highscoreChanged(event);
      }
    }
  }

  private void triggerHighscoreUpdate(@NonNull Game game, @NonNull Highscore highscore) {
    if (pauseHighscoreEvents) {
      LOG.info("Skipping highscore update event because update events are paused.");
      return;
    }

    refreshAvailableScores();

    for (HighscoreChangeListener listener : new ArrayList<>(listeners)) {
      listener.highscoreUpdated(game, highscore);
    }
  }

  public void addHighscoreChangeListener(@NonNull HighscoreChangeListener listener) {
    this.listeners.add(listener);
    LOG.info("Registered highscore change listener: " + listener.getClass().getSimpleName());
  }

  public void refreshAvailableScores() {
    this.refreshHighscoreFiles();
    this.refreshVPRegEntries();
  }

  public List<String> getVPRegEntries() {
    return this.vpRegEntries;
  }

  public List<String> getHighscoreFiles() {
    return highscoreFiles;
  }

  public void refreshVPRegEntries() {
    try {
      List<File> vpRegFiles = new ArrayList<>();
      vpRegEntries.clear();
      List<GameEmulator> gameEmulators = emulatorService.getVpxGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        File vpRegFile = gameEmulator.getVPRegFile();
        if (vpRegFile.exists() && !vpRegFiles.contains(vpRegFile)) {
          vpRegFiles.add(vpRegFile);
          VPReg reg = new VPReg(vpRegFile);
          vpRegEntries.addAll(reg.getEntries());
        }
      }
      LOG.info("Highscore Service read " + vpRegEntries.size() + " VPReg.stg entries");
    }
    catch (Exception e) {
      LOG.error("Failed to refresh VPReg entries: " + e.getMessage(), e);
    }
  }


  public void refreshHighscoreFiles() {
    try {
      highscoreFiles.clear();
      List<GameEmulator> gameEmulators = emulatorService.getVpxGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        File[] files = gameEmulator.getUserFolder().listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null) {
          for (File file : files) {
            if (!highscoreFiles.contains(file.getName())) {
              highscoreFiles.add(file.getName());
            }
          }
        }
      }
      LOG.info("Highscore Service read " + highscoreFiles.size() + " highscore text files");
    }
    catch (Exception e) {
      LOG.error("Failed to refresh highscore filenames: " + e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() {
    this.refreshVPRegEntries();
    this.refreshHighscoreFiles();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  public void setGameService(GameService gameService) {
    this.gameService = gameService;
  }

}
