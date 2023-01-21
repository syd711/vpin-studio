package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.CompetitionType;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    for (CompetitionChangeListener listener : this.listeners) {
      Optional<Player> playerForInitials = playerService.getPlayerForInitials(c.getWinnerInitials());
      if(playerForInitials.isPresent()) {
        listener.competitionFinished(c, playerForInitials.get());
      }
      else {
        listener.competitionFinished(c, null);
      }
    }
  }

  public void notifyCompetitionDeleted(Competition c) {
    for (CompetitionChangeListener listener : this.listeners) {
      listener.competitionDeleted(c);
    }
  }

  public List<Competition> getOfflineCompetitions() {
    return competitionsRepository.findByType(CompetitionType.OFFLINE.name());
  }

  public List<Competition> getDiscordCompetitions() {
    return competitionsRepository.findByType(CompetitionType.DISCORD.name());
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
    Date start = competition.getStartDate();
    Date end = competition.getEndDate();
    int gameId = competition.getGameId();
    return highscoreService.getScoresBetween(gameId, start, end);
  }

  public Competition save(Competition c) {
    boolean isNew = c.getId() == null;
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
    ScoreSummary highscores = highscoreService.getHighscores(competition.getGameId(), null);
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
    notifyCompetitionFinished(competition);
    return save;
  }

  public List<Competition> getActiveCompetitions() {
    return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(new Date(), new Date());
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

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCheckRunnable(this), 1000 * 60 * 60);
  }

  public List<Competition> findCompetitionForGame(int id) {
    return competitionsRepository.findByGameId(id);
  }
}
