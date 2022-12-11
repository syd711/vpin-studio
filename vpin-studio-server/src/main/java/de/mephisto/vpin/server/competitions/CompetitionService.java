package de.mephisto.vpin.server.competitions;

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
    new Thread(() -> {
      for (CompetitionChangeListener listener : this.listeners) {
        listener.competitionCreated(c);
      }
    }).start();
  }

  public void notifyCompetitionChanged(Competition c) {
    new Thread(() -> {
      for (CompetitionChangeListener listener : this.listeners) {
        listener.competitionChanged(c);
      }
    }).start();
  }

  public void notifyCompetitionFinished(Competition c) {
    new Thread(() -> {
      for (CompetitionChangeListener listener : this.listeners) {
        listener.competitionFinished(c);
      }
    }).start();
  }

  public void notifyCompetitionDeleted(Competition c) {
    new Thread(() -> {
      for (CompetitionChangeListener listener : this.listeners) {
        listener.competitionDeleted(c);
      }
    }).start();
  }

  public List<Competition> getCompetitions() {
    return competitionsRepository.findAll();
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
      runFinishedCompetitionsCheck();
    }
    return getCompetition(c.getId());
  }

  public void runFinishedCompetitionsCheck() {
    List<Competition> openCompetitions = getCompetitionToBeFinished();
    for (Competition openCompetition : openCompetitions) {
      finishCompetition(openCompetition);
    }
  }

  public void finishCompetition(Competition competition) {
    ScoreSummary highscores = highscoreService.getHighscores(competition.getGameId());
    if (highscores.getScores().isEmpty()) {
      LOG.error("Failed to finished " + competition + " correctly, no score could be determined, using John Doe.");
      competition.setWinnerInitials("???");
    }
    else {
      Score score = highscores.getScores().get(0);
      competition.setWinnerInitials(score.getPlayerInitials());

      Optional<Player> playerForInitials = playerService.getPlayerForInitials(score.getPlayerInitials());
      if (playerForInitials.isPresent()) {
        LOG.info(playerForInitials.get() + " is announced as winner of " + competition);
        competition.setWinner(playerForInitials.get());
      }
    }
    save(competition);
    notifyCompetitionFinished(competition);
  }

  public List<Competition> getActiveOfflineCompetitions() {
    return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(new Date(), new Date());
  }

  public void delete(long id) {
    Optional<Competition> c = competitionsRepository.findById(id);
    if (c.isPresent()) {
      competitionsRepository.deleteById(id);
      notifyCompetitionDeleted(c.get());
    }
    else {
      LOG.error("No competition exists for id " + id);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCheckRunnable(this), 1000 * 60 * 60);
  }
}
