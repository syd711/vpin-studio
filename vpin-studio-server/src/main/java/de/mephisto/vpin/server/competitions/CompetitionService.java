package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
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

  public void notifyCompetitionChanged(Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionChanged(c);
    }
  }

  public void notifyCompetitionFinished(Competition c) {
    Player player = null;
    if (c.getType().equals(CompetitionType.OFFLINE.name())) {
      player = playerService.getPlayerForInitials(c.getDiscordServerId(), c.getWinnerInitials());
    }
    else if (c.getType().equals(CompetitionType.DISCORD.name())) {
      player = discordService.getPlayerByInitials(c.getDiscordServerId(), c.getWinnerInitials());
    }

    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionFinished(c, player);
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
    List<Competition> competitions = competitionsRepository.findByWinnerInitialsIsNotNullAndEndDateLessThanEqualOrderByEndDate(new Date());
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


  public ScoreSummary getCompetitionScore(long competitionId) {
    Competition competition = getCompetition(competitionId);
    long serverId = competition.getDiscordServerId();
    long channelId = competition.getDiscordChannelId();

    if (competition.getType().equals(CompetitionType.OFFLINE.name())) {
      return highscoreService.getScoreSummary(serverId, competition.getGameId(), null);
    }
    else if (competition.getType().equals(CompetitionType.DISCORD.name())) {
      ScoreList scoreList = discordService.getScoreList(highscoreParser, competition.getUuid(), serverId, channelId);
      return scoreList.getLatestScore();
    }

    LOG.error("No competition found for highscore retrieval with competitionId " + competitionId);
    return null;
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
    runFinishedCompetitionsCheck();
    return getCompetition(c.getId());
  }

  public void runFinishedCompetitionsCheck() {
    List<Competition> openCompetitions = getCompetitionToBeFinished();
    for (Competition openCompetition : openCompetitions) {
      finishCompetition(openCompetition);
    }
  }

  public Competition finishCompetition(int id) {
    return finishCompetition(getCompetition(id));
  }

  public Competition finishCompetition(Competition competition) {
    long serverId = competition.getDiscordServerId();
    long channelId = competition.getDiscordChannelId();

    ScoreSummary highscores = highscoreService.getScoreSummary(serverId, competition.getGameId(), null);
    if (highscores.getScores().isEmpty()) {
      LOG.error("Failed to finished " + competition + " correctly, no score could be determined, using John Doe.");
      competition.setWinnerInitials("???");
    }
    else {
      Score score = highscores.getScores().get(0);
      competition.setWinnerInitials(score.getPlayerInitials());
    }
    competition.setEndDate(new Date()); //always the current date
    Competition save = save(competition);

    notifyCompetitionFinished(save);
    return save;
  }

  public List<Competition> getActiveCompetitions() {
    return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(new Date(), new Date());
  }

  public Competition getActiveCompetition(CompetitionType competitionType) {
    List<Competition> result = competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndType(new Date(), new Date(), competitionType.name());
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

  public boolean isCompeted(int id) {
    List<Competition> competedByGameId = competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndGameId(new Date(), new Date(), id);
    return !competedByGameId.isEmpty();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCheckRunnable(this), 1000 * 60 * 60);
  }
}
