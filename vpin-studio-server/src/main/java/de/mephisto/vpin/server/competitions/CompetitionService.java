package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import de.mephisto.vpin.server.iscored.IScoredService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompetitionService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionService.class);

  @Autowired
  private CompetitionsRepository competitionsRepository;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private HighscoreParsingService highscoreParser;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private ThreadPoolTaskScheduler scheduler;

  @Autowired
  private GameService gameService;

  @Autowired
  private IScoredService iScoredService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private CompetitionValidator competitionValidator;

  private final List<CompetitionChangeListener> listeners = new ArrayList<>();

  public void addCompetitionChangeListener(CompetitionChangeListener c) {
    this.listeners.add(c);
  }

  public void notifyCompetitionCreation(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionCreated(c);
    }
  }

  public void notifyCompetitionStarted(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionStarted(c);
    }
  }

  public void notifyCompetitionChanged(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionChanged(c);
    }
  }

  public void notifyCompetitionDeleted(@NonNull Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionDeleted(c);
    }
  }

  public List<Competition> getOfflineCompetitions() {
    return competitionsRepository
        .findByTypeOrderByEndDateDesc(CompetitionType.OFFLINE.name())
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
  }

  public List<Competition> getDiscordCompetitions() {
    return competitionsRepository
        .findByTypeOrderByEndDateDesc(CompetitionType.DISCORD.name())
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
  }

  public List<Competition> getSubscriptions() {
    return competitionsRepository
        .findByTypeOrderByEndDateDesc(CompetitionType.SUBSCRIPTION.name())
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
  }


  public List<Competition> getIScoredSubscriptions() {
    List<Competition> collect = competitionsRepository
        .findByTypeOrderByEndDateDesc(CompetitionType.ISCORED.name())
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
    return autoFixCompetitionGames(collect);
  }

  public List<Competition> getSubscriptions(String rom) {
    return competitionsRepository
        .findByTypeAndRomOrderByName(CompetitionType.SUBSCRIPTION.name(), rom)
        .stream().map(c -> competitionValidator.validate(c))
        .collect(Collectors.toList());
  }

  public List<Player> getDiscordCompetitionPlayers(long competitionId) {
    Competition competition = this.getCompetition(competitionId);
    if (competition != null) {
      return discordService.getCompetitionPlayers(competition.getDiscordServerId(), competition.getDiscordChannelId());
    }
    return Collections.emptyList();
  }

  public Competition getCompetition(long id) {
    Optional<Competition> competition = competitionsRepository.findById(id);
    return competition.orElse(null);
  }

  public List<Competition> getFinishedCompetitions(int limit) {
    List<Competition> competitions = competitionsRepository.findByWinnerInitialsIsNotNull();
    if (competitions.size() > limit) {
      return competitions.subList(0, limit);
    }
    return competitions;
  }

  public List<Competition> getCompetitionToBeFinished() {
    try {
      return competitionsRepository.findByWinnerInitialsIsNullAndEndDateLessThanEqualOrderByEndDate(new Date());
    }
    catch (Exception e) {
      LOG.error("Failed to read competitions: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  public ScoreList getCompetitionScores(long id) {
    Competition competition = getCompetition(id);
    competition.getGameId();

    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
      Date start = competition.getStartDate();
      Date end = competition.getEndDate();
      int gameId = competition.getGameId();
      long serverId = competition.getDiscordServerId();
      return highscoreService.getScoresBetween(gameId, start, end, serverId);
    }
    else if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      long serverId = competition.getDiscordServerId();
      long channelId = competition.getDiscordChannelId();
      ScoreSummary highscore = discordService.getScoreSummary(highscoreParser, competition.getUuid(), serverId, channelId);
      ScoreList scoreList = new ScoreList();
      scoreList.setLatestScore(highscore);
      scoreList.setScores(Collections.singletonList(highscore));
      return scoreList;
    }

    return null;
  }

  @NonNull
  public ScoreSummary getCompetitionScore(long competitionId) {
    Competition competition = getCompetition(competitionId);
    long serverId = competition.getDiscordServerId();
    long channelId = competition.getDiscordChannelId();

    String type = competition.getType();
    if (type.equals(CompetitionType.DISCORD.name()) || type.equals(CompetitionType.SUBSCRIPTION.name())) {
      return discordService.getScoreSummary(highscoreParser, competition.getUuid(), serverId, channelId);
    }

    return highscoreService.getScoreSummary(serverId, gameService.getGame(competition.getGameId()));
  }


  public Competition save(Competition c) {
    boolean isNew = c.getId() == null;
    if (c.getType() == null) {
      c.setType(CompetitionType.OFFLINE.name());
    }
    Competition updated = competitionsRepository.saveAndFlush(c);
    LOG.info("Saved " + updated);
    if (isNew) {
      notifyCompetitionCreation(updated);
    }
    else {
      notifyCompetitionChanged(updated);
    }
    return getCompetition(c.getId());
  }

  /**
   * As the name says: it checks for finished and to be started competitions:
   * Important: finish competitions first as we have to "release" the channel topic for the next one.
   */
  public void runCompetitionsFinishedAndStartedCheck() {
    //check all competitions for their finish state, this includes Discord ones, since the date can't be changed
    List<Competition> openCompetitions = getCompetitionToBeFinished();
    if (!openCompetitions.isEmpty()) {
      LOG.info("Running automated competition status check, found " + openCompetitions.size() + " candidates.");
    }
    for (Competition openCompetition : openCompetitions) {
      LOG.info("Finishing " + openCompetition);
      finishCompetition(openCompetition);
    }

    //check if competition have become active, initialize them
    List<Competition> activeCompetitions = getActiveCompetitions();
    for (Competition activeCompetition : activeCompetitions) {
      if (activeCompetition.isActive() && !activeCompetition.isStarted()) {
        LOG.info("Starting " + activeCompetition);
        notifyCompetitionStarted(activeCompetition);

        //update state
        activeCompetition.setStarted(true);
        competitionsRepository.saveAndFlush(activeCompetition);
      }

      if (activeCompetition.isActive() && activeCompetition.isStarted() && activeCompetition.getType().equals(CompetitionType.DISCORD.name())) {
        long serverId = activeCompetition.getDiscordServerId();
        long channelId = activeCompetition.getDiscordChannelId();
        boolean active = discordService.isCompetitionActive(serverId, channelId, activeCompetition.getUuid());
        if (!active) {
          LOG.info("Found active competition " + activeCompetition + ", trying to resolve winner data.");
          ScoreSummary competitionScore = getCompetitionsFinalScore(activeCompetition);
          if (competitionScore.getScores().isEmpty()) {
            activeCompetition.setWinnerInitials("???");
          }
          else {
            Score score = competitionScore.getScores().get(0);
            String initials = !StringUtils.isEmpty(score.getPlayerInitials()) ? score.getPlayerInitials() : "???";
            activeCompetition.setWinnerInitials(initials);
          }
          competitionsRepository.saveAndFlush(activeCompetition);
          LOG.info("Finished " + activeCompetition + ", winner is '" + activeCompetition.getWinnerInitials() + "'");
        }
      }
    }
  }

  public Competition finishCompetition(int id) {
    return finishCompetition(getCompetition(id));
  }

  @NonNull
  public Competition finishCompetition(@NonNull Competition competition) {
    LOG.info("Running finishing process for " + competition);
    ScoreSummary competitionScore = getCompetitionsFinalScore(competition);

    if (competitionScore.getScores().isEmpty()) {
      LOG.error("Failed to finished " + competition + " correctly, no score could be determined, using John Doe.");
      competition.setWinnerInitials("???");
    }
    else {
      Score score = competitionScore.getScores().get(0);
      String initials = !StringUtils.isEmpty(score.getPlayerInitials()) ? score.getPlayerInitials() : "???";
      competition.setWinnerInitials(initials);
    }
    competition.setScore(competitionScore.getRaw()); //save the last raw score to the competition itself
    Competition finishedCompetition = save(competition);

    Player player = null;
    if (finishedCompetition.getType().equals(CompetitionType.OFFLINE.name())) {
      player = playerService.getPlayerForInitials(finishedCompetition.getDiscordServerId(), finishedCompetition.getWinnerInitials());
    }
    else if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      player = discordService.getPlayerByInitials(finishedCompetition.getDiscordServerId(), finishedCompetition.getWinnerInitials());
    }

    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionFinished(finishedCompetition, player, competitionScore);
    }
    return finishedCompetition;
  }

  public List<Competition> getActiveCompetitions() {
    try {
      return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(new Date(), new Date());
    }
    catch (Exception e) {
      LOG.error("Failed to read active competitions: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  public Competition getActiveCompetition(CompetitionType competitionType) {
    List<Competition> result = competitionsRepository.findByAndWinnerInitialsIsNullAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndType(new Date(), new Date(), competitionType.name());
    if (!result.isEmpty()) {
      return result.get(0);
    }
    return null;
  }

  public boolean delete(long id) {
    Optional<Competition> c = competitionsRepository.findById(id);
    if (c.isPresent()) {
      competitionsRepository.deleteById(id);
      notifyCompetitionDeleted(c.get());
      return true;
    }
    else {
      LOG.error("No competition exists for id " + id);
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
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      return discordService.getScoreSummary(this.highscoreParser, competition.getUuid(), serverId, competition.getDiscordChannelId());
    }
    return highscoreService.getScoreSummary(serverId, gameService.getGame(competition.getGameId()));
  }

  private List<Competition> autoFixCompetitionGames(List<Competition> collect) {
    List<Competition> validatedCompetitions = new ArrayList<>();
    for (Competition competition : collect) {
      int gameId = competition.getGameId();
      if (gameId > 0) {
        Game game = gameService.getGame(gameId);
        if (game != null) {
          String extTableId = game.getExtTableId();
          String extTableVersionId = game.getExtTableVersionId();
          VpsTable tableById = vpsService.getTableById(extTableId);
          if (tableById != null && !StringUtils.isEmpty(extTableVersionId)) {
            VpsTableVersion tableVersionById = tableById.getTableVersionById(extTableVersionId);
            if (tableVersionById == null) {
              competition.setGameId(-1);
              Competition save = save(competition);
              LOG.info("Resetted game of " + competition + ", because no matching VPS table version was found");
              validatedCompetitions.add(save);
              continue;
            }
          }
          else {
            competition.setGameId(-1);
            Competition save = save(competition);
            LOG.info("Resetted game of " + competition + ", because no matching VPS table was found");
            validatedCompetitions.add(save);
            continue;
          }
        }
      }
      else {
        Game game = gameService.getGameByVpsTable(competition.getVpsTableId(), competition.getVpsTableVersionId());
        if (game != null) {
          competition.setGameId(game.getId());
          Competition save = save(competition);
          validatedCompetitions.add(save);
          LOG.info("Auto-applied game of " + competition);
          continue;
        }
      }

      validatedCompetitions.add(competition);
    }
    return validatedCompetitions;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCheckRunnable(this), 1000 * 60 * 2);

    try {
      List<Competition> iScoredSubscriptions = getIScoredSubscriptions();
      LOG.info("---------------------------------- iScored Competitions -----------------------------------------------");
      for (Competition s : iScoredSubscriptions) {
        LOG.info(s.toString() + " (" + gameService.getGame(s.getGameId()) + ") [" + s.getUrl() + "], [" + VPS.getVpsTableUrl(s.getVpsTableId(), s.getVpsTableVersionId()) + "]");
      }
      LOG.info("--------------------------------- /iScored Competitions -----------------------------------------------");
    }
    catch (Exception e) {
      LOG.error("iScored summary failed: {}", e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
