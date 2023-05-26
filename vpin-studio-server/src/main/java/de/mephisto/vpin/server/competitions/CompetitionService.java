package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
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
    List<Competition> competitions = competitionsRepository.findByWinnerInitialsIsNotNull();
    if (competitions.size() > limit) {
      return competitions.subList(0, limit);
    }
    return competitions;
  }

  public List<Competition> getCompetitionToBeFinished() {
    return competitionsRepository.findByWinnerInitialsIsNullAndEndDateLessThanEqualOrderByEndDate(new Date());
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
        LOG.info("Discord competition \"" + competition + "\" did not contain any highscore, seems no one played yet?");
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
    return getCompetition(c.getId());
  }

  /**
   * As the name says: it checks for finished and to be started competitions:
   * Important: finish competitions first as we have to "release" the channel topic for the next one.
   */
  public void runCompetitionsFinishedAndStartedCheck() {
    LOG.info("Running automated competition status check.");

    //check all competitions for their finish state, this includes Discord ones, since the date can't be changed
    List<Competition> openCompetitions = getCompetitionToBeFinished();
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
    return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(new Date(), new Date());
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

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCheckRunnable(this), 1000 * 60 * 2);
  }

  @NonNull
  private ScoreSummary getCompetitionsFinalScore(@NonNull Competition competition) {
    long serverId = competition.getDiscordServerId();
    if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      ScoreList scoreList = discordService.getScoreList(this.highscoreParser, competition.getUuid(), serverId, competition.getDiscordChannelId());
      ScoreSummary latestScore = scoreList.getLatestScore();
      if (latestScore == null) {
        latestScore = new ScoreSummary(Collections.emptyList(), new Date());
      }
      return latestScore;
    }
    return highscoreService.getScoreSummary(serverId, competition.getGameId(), null);
  }
}
