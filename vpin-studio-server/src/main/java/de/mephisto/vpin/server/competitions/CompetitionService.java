package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompetitionService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionService.class);

  @Autowired
  private CompetitionsRepository competitionsRepository;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private HighscoreParser highscoreParser;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private ThreadPoolTaskScheduler scheduler;

  private final List<CompetitionChangeListener> listeners = new ArrayList<>();

  public void addCompetitionChangeListener(CompetitionChangeListener c) {
    this.listeners.add(c);
  }

  public void notifyCompetitionCreation(Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionCreated(c);
    }
  }

  public void notifyCompetitionStarted(Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionStarted(c);
    }
  }

  public void notifyCompetitionChanged(Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionChanged(c);
    }
  }

  public void notifyCompetitionDeleted(Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionDeleted(c);
    }
  }

  public List<Competition> getOfflineCompetitions() {
    return competitionsRepository.findByTypeOrderByEndDateDesc(CompetitionType.OFFLINE.name());
  }

  public List<Competition> getDiscordCompetitions() {
    return competitionsRepository.findByTypeOrderByEndDateDesc(CompetitionType.DISCORD.name());
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
    List<Competition> competitions = competitionsRepository.findByWinnerInitialsIsNotNullAndEndDateLessThanEqualOrderByEndDate(DateUtil.today());
    if (competitions.size() > limit) {
      return competitions.subList(0, limit);
    }
    return competitions;
  }

  public List<Competition> getCompetitionToBeFinished() {
    return competitionsRepository.findByWinnerInitialsIsNullAndEndDateLessThanEqualOrderByEndDate(DateUtil.today());
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
      return discordService.getScoreList(highscoreParser, competition.getUuid(), serverId, channelId);
    }

    return null;
  }

  @NonNull
  public ScoreSummary getCompetitionScore(long competitionId) {
    Competition competition = getCompetition(competitionId);
    long serverId = competition.getDiscordServerId();
    long channelId = competition.getDiscordChannelId();

    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      ScoreList scoreList = discordService.getScoreList(highscoreParser, competition.getUuid(), serverId, channelId);
      ScoreSummary latestScore = scoreList.getLatestScore();
      if (latestScore == null) {
        LOG.info("Discord competition \"" + competition +  "\" did not contain any highscore, seems no one played yet?");
        return new ScoreSummary(Collections.emptyList(), competition.getUpdatedAt());
      }
      return latestScore;
    }

    return highscoreService.getScoreSummary(serverId, competition.getGameId(), null);
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
    runCompetitionsFinishedAndStartedCheck();
    return getCompetition(c.getId());
  }

  public void runCompetitionsFinishedAndStartedCheck() {
    LOG.info("Running automated competition status check.");

    //check if competition have become active, initialize them
    List<Competition> plannedCompetitions = getActiveCompetitions();
    for (Competition plannedCompetition : plannedCompetitions) {
      if (plannedCompetition.isActive() && !plannedCompetition.isStarted()) {
        LOG.info("Starting " + plannedCompetition);
        notifyCompetitionStarted(plannedCompetition);

        //update state
        plannedCompetition.setStarted(true);
        competitionsRepository.saveAndFlush(plannedCompetition);
      }
    }

    //check all competitions for their finish state, this includes Discord ones, since the date can't be changed
    List<Competition> openCompetitions = getCompetitionToBeFinished();
    for (Competition openCompetition : openCompetitions) {
      LOG.info("Finishing " + openCompetition);
      finishCompetition(openCompetition);
    }
  }

  public Competition finishCompetition(int id) {
    return finishCompetition(getCompetition(id));
  }

  @NonNull
  public Competition finishCompetition(@NonNull Competition competition) {
    long serverId = competition.getDiscordServerId();
    ScoreSummary scoreSummary = highscoreService.getScoreSummary(serverId, competition.getGameId(), null);
    if (scoreSummary.getScores().isEmpty()) {
      LOG.error("Failed to finished " + competition + " correctly, no score could be determined, using John Doe.");
      competition.setWinnerInitials("???");
    }
    else {
      Score score = scoreSummary.getScores().get(0);
      competition.setWinnerInitials(score.getPlayerInitials());
    }
    competition.setEndDate(new Date()); //always the current date
    competition.setScore(scoreSummary.getRaw()); //save the last raw score to the competition itself
    Competition finishedCompetition = save(competition);

    Player player = null;
    if (finishedCompetition.getType().equals(CompetitionType.OFFLINE.name())) {
      player = playerService.getPlayerForInitials(finishedCompetition.getDiscordServerId(), finishedCompetition.getWinnerInitials());
    }
    else if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      player = discordService.getPlayerByInitials(finishedCompetition.getDiscordServerId(), finishedCompetition.getWinnerInitials());
    }

    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionFinished(finishedCompetition, player, scoreSummary);
    }
    return finishedCompetition;
  }

  public List<Competition> getActiveCompetitions() {
    return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(DateUtil.today(), DateUtil.today());
  }

  public Competition getActiveCompetitionForGame(int gameId) {
    Optional<Competition> competition = competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndGameId(DateUtil.today(), DateUtil.today(), gameId);
    return competition.orElse(null);
  }

  public Competition getActiveCompetition(CompetitionType competitionType) {
    List<Competition> result = competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndType(DateUtil.today(), DateUtil.today(), competitionType.name());
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

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCheckRunnable(this), 1000 * 60 * 60);
  }
}
