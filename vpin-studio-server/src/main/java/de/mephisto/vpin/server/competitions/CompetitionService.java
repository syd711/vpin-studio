package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.ScoreList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
  private ThreadPoolTaskScheduler scheduler;

  public List<Competition> getCompetitions() {
    return competitionsRepository.findAll();
  }

  public Competition getCompetition(long id) {
    Optional<Competition> competition = competitionsRepository.findById(id);
    return competition.orElse(null);
  }

  public List<Competition> getFinishedCompetitions(int limit) {
    List<Competition> competitions = competitionsRepository.findByEndDateLessThanEqualOrderByEndDate(new Date());
    competitions.sort(Comparator.comparing(Competition::getEndDate));
    if (competitions.size() > limit) {
      return competitions.subList(0, limit);
    }
    return competitions;
  }

  public ScoreList getCompetitionScores(long id) {
    Competition competition = getCompetition(id);
    Date start = competition.getStartDate();
    Date end = competition.getEndDate();
    int gameId = competition.getGameId();
    return highscoreService.getScoresBetween(gameId, start, end);
  }

  public Competition save(Competition c) {
    Competition updated = competitionsRepository.saveAndFlush(c);
    LOG.info("Saved " + updated);
    return getCompetition(c.getId());
  }

  public List<Competition> getActiveOfflineCompetitions() {
    return competitionsRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(new Date(), new Date());
  }

  public void delete(long id) {
    Optional<Competition> c = competitionsRepository.findById(id);
    if(c.isPresent()) {
      competitionsRepository.deleteById(id);
    }
    else {
      LOG.error("No competition exists for id " + id);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCleanupRunnableTask(), 1000 * 60 * 60);
  }
}
