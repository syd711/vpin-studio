package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompetitionService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionService.class);

  @Autowired
  private CompetitionsRepository competitionsRepository;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private ThreadPoolTaskScheduler scheduler;

  @Autowired
  private GameService gameService;

  @Autowired
  private CompetitionValidator competitionValidator;

  @Autowired
  private AssetService assetService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  public List<Competition> getOfflineCompetitions() {
    return competitionsRepository
        .findByTypeOrderByEndDateDesc(CompetitionType.OFFLINE.name())
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
  }

  public List<Competition> getWeeklyCompetitions() {
    return competitionsRepository
        .findByTypeOrderByEndDateDesc(CompetitionType.WEEKLY.name())
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
  }

  public List<Competition> getManiaChallengeCompetitions() {
    return competitionsRepository
        .findByTypeOrderByEndDateDesc(CompetitionType.MANIA_CHALLENGE.name())
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
  }

  public List<Competition> getIScoredSubscriptions() {
    return competitionsRepository.findByTypeOrderByEndDateDesc(CompetitionType.ISCORED.name());
  }

  public Competition getCompetition(long id) {
    Optional<Competition> competition = competitionsRepository.findById(id);
    return competition.orElse(null);
  }

  public List<Competition> getFinishedCompetitions(int limit) {
    List<Competition> competitions = competitionsRepository.findByWinnerInitialsIsNotNull();
    if (competitions.size() > limit && limit > 0) {
      return competitions.subList(0, limit);
    }
    return competitions;
  }

  public List<Competition> getCompetitionToBeFinished() {
    try {
        return competitionsRepository.findByWinnerInitialsIsNullAndEndDateLessThanEqualOrderByEndDate(Instant.now());
    }
    catch (Exception e) {
      LOG.error("Failed to read competitions: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  public ScoreList getCompetitionScores(long id) {
    Competition competition = getCompetition(id);
    competition.getGameId();

    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
        Instant start = competition.getStartDate();
        Instant end = competition.getEndDate();
      int gameId = competition.getGameId();
      Game game = gameService.getGame(gameId);
      long serverId = competition.getDiscordServerId();
        return highscoreService.getScoresBetween(game, start, end, serverId);
    }

    return null;
  }

  @NonNull
  public ScoreSummary getCompetitionScore(long competitionId) {
    Competition competition = getCompetition(competitionId);
    long serverId = competition.getDiscordServerId();

    return highscoreService.getScoreSummary(serverId, gameService.getGame(competition.getGameId()));
  }


  public Competition save(Competition c) {
    boolean isNew = c.getId() == null;
    if (c.getType() == null) {
      c.setType(CompetitionType.OFFLINE.name());
    }
    Competition updated = competitionsRepository.saveAndFlush(c);
    LOG.info("Saved {}", updated);
    if (isNew) {
      competitionLifecycleService.notifyCompetitionCreation(updated);
    }
    else {
      competitionLifecycleService.notifyCompetitionChanged(updated);
    }
    return getCompetition(c.getId());
  }

  /**
   * As the name says: it checks for finished and to be started competitions:
   * Important: finish competitions first as we have to "release" the channel topic for the next one.
   */
  public void runCompetitionsFinishedAndStartedCheck() {
    //check all competitions for their finish state
    List<Competition> openCompetitions = getCompetitionToBeFinished();
    if (!openCompetitions.isEmpty()) {
      LOG.info("Running automated competition status check, found {} candidates.", openCompetitions.size());
    }
    for (Competition openCompetition : openCompetitions) {
      LOG.info("Finishing {}", openCompetition);
      finishCompetition(openCompetition);
    }

    //check if competition have become active, initialize them
    List<Competition> activeCompetitions = getActiveCompetitions();
    for (Competition activeCompetition : activeCompetitions) {
      if (activeCompetition.isActive() && !activeCompetition.isStarted()) {
        LOG.info("Starting {}", activeCompetition);
        //update state
        activeCompetition.setStarted(true);
        competitionsRepository.saveAndFlush(activeCompetition);

        competitionLifecycleService.notifyCompetitionStarted(activeCompetition);
      }
    }
  }

  public Competition finishCompetition(int id) {
    return finishCompetition(getCompetition(id));
  }

  @NonNull
  public Competition finishCompetition(@NonNull Competition competition) {
    LOG.info("Running finishing process for {}", competition);
    ScoreSummary competitionScore = getCompetitionsFinalScore(competition);

    if (competitionScore.getScores().isEmpty()) {
      LOG.error("Failed to finished {} correctly, no score could be determined, using John Doe.", competition);
      competition.setWinnerInitials("???");
    }
    else {
      Score score = competitionScore.getScores().getFirst();
      String initials = StringUtils.hasText(score.getPlayerInitials()) ? score.getPlayerInitials() : "???";
      competition.setWinnerInitials(initials);
    }
    competition.setScore(competitionScore.getRaw()); //save the last raw score to the competition itself
    Competition finishedCompetition = save(competition);

    Player player = null;
    if (finishedCompetition.getType().equals(CompetitionType.OFFLINE.name())) {
      player = playerService.getPlayerForInitials(finishedCompetition.getDiscordServerId(), finishedCompetition.getWinnerInitials());
    }

    competitionLifecycleService.notifyCompetitionFinished(finishedCompetition, player, competitionScore);
    return finishedCompetition;
  }

  public List<Competition> getActiveCompetitions() {
    try {
        return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(Instant.now(), Instant.now());
    }
    catch (Exception e) {
      LOG.error("Failed to read active competitions: {}", e.getMessage());
    }
    return Collections.emptyList();
  }

  public List<Competition> getFinishedByDateCompetitions() {
    try {
        return competitionsRepository.findByEndDateLessThanEqual(Instant.now());
    }
    catch (Exception e) {
      LOG.error("Failed to read active competitions: {}", e.getMessage());
    }
    return Collections.emptyList();
  }

  public Competition getActiveCompetition(CompetitionType competitionType) {
      List<Competition> result = competitionsRepository.findByAndWinnerInitialsIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndType(Instant.now(), Instant.now(), competitionType.name());
    if (!result.isEmpty()) {
      return result.getFirst();
    }
    return null;
  }

  public boolean delete(long id) {
    Optional<Competition> c = competitionsRepository.findById(id);
    if (c.isPresent()) {
      assetService.deleteCompetitionBackground(c.get().getGameId());
      competitionsRepository.deleteById(id);
      competitionLifecycleService.notifyCompetitionDeleted(c.get());
      LOG.error("Deleted competition {}", c.get().getName());
      return true;
    }
    else {
      LOG.error("No competition exists for id {}", id);
    }
    return false;
  }

  public List<Competition> getCompetitionForGame(int id) {
    return competitionsRepository.findByGameId(id);
  }

  public Competition getCompetitionForUuid(String uuid) {
    Optional<Competition> competition = competitionsRepository.findByUuid(uuid);
    return competition.orElse(null);
  }

  public List<Competition> getWonCompetitions(CompetitionType cType, String initials) {
    return competitionsRepository.findByWinnerInitialsAndType(initials, cType.name());
  }

  @NonNull
  private ScoreSummary getCompetitionsFinalScore(@NonNull Competition competition) {
    long serverId = competition.getDiscordServerId();
    Game game = gameService.getGame(competition.getGameId());
    if (game == null) {
      return new ScoreSummary();
    }
    return highscoreService.getScoreSummary(serverId, game);
  }

  /**
   * The competition types "DISCORD" and "SUBSCRIPTION" have been removed.
   * Existing competitions of these types are deleted, since they can't be mapped onto a {@link CompetitionType} anymore.
   */
  private void deleteRemovedCompetitionTypes() {
    for (String removedType : List.of("DISCORD", "SUBSCRIPTION")) {
      try {
        List<Competition> competitions = competitionsRepository.findByTypeOrderByEndDateDesc(removedType);
        for (Competition competition : competitions) {
          assetService.deleteCompetitionBackground(competition.getGameId());
          competitionsRepository.deleteById(competition.getId());
          LOG.info("Deleted competition \"{}\" of removed type {}", competition.getName(), removedType);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to delete competitions of removed type {}: {}", removedType, e.getMessage(), e);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    deleteRemovedCompetitionTypes();
    try {
      List<Competition> iScoredSubscriptions = getIScoredSubscriptions();
      LOG.info("---------------------------------- iScored Competitions -----------------------------------------------");
      for (Competition s : iScoredSubscriptions) {
        LOG.info("{} [{}], [{}], ID: {}", s.toString(), s.getUrl(), VPS.getVpsTableUrl(s.getVpsTableId(), s.getVpsTableVersionId()), s.getGameId());
      }
      LOG.info("--------------------------------- /iScored Competitions -----------------------------------------------");
    }
    catch (Exception e) {
      LOG.error("iScored summary failed: {}", e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }


  @EventListener(ApplicationReadyEvent.class)
  public void scheduleCompetitionCheck() {
      scheduler.scheduleAtFixedRate(new CompetitionCheckRunnable(this), Duration.ofMinutes(2));
  }

  public void shutdown() {
    scheduler.shutdown();
    LOG.info("Competition scheduler has been shut down.");
  }
}
