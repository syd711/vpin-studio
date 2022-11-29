package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreRepository;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.highscores.HighscoreVersionRepository;
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
  private HighscoreRepository highscoreRepository;

  @Autowired
  private HighscoreVersionRepository highscoreVersionRepository;

  @Autowired
  private ThreadPoolTaskScheduler scheduler;

  public List<Competition> getCompetitions() {
    return competitionsRepository.findAll();
  }

  public Competition getCompetition(long id) {
    Optional<Competition> competition = competitionsRepository.findById(id);
    return competition.orElse(null);
  }

  public List<Object> getCompetitionHighscores(long id) {
    List<Object> result = new ArrayList<>();
    Optional<Competition> competition = competitionsRepository.findById(id);
    if(competition.isPresent()) {
      Competition c = competition.get();
      Date start = c.getStartDate();
      Date end = c.getEndDate();

      Optional<Highscore> highscore = highscoreRepository.findByGameIdAndUpdatedAtBetween(c.getGameId(), start, end);
      if(highscore.isPresent()) {
        result.add(highscore);
      }

      List<HighscoreVersion> byGameIdAndCreatedAtBetween = highscoreVersionRepository.findByGameIdAndCreatedAtBetween(c.getGameId(), start, end);
      result.addAll(byGameIdAndCreatedAtBetween);
    }
    return result;
  }

  public Competition save(Competition c) {
    Competition updated = competitionsRepository.saveAndFlush(c);
    LOG.info("Saved " + updated);
    return getCompetition(c.getId());
  }

  public Competition getActiveOfflineCompetition() {
    List<Competition> activeCompetitions = competitionsRepository.findActiveCompetitions();
    if (!activeCompetitions.isEmpty()) {
      return activeCompetitions.get(0);
    }
    return null;
  }

  public void delete(long id) {
    competitionsRepository.deleteById(id);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    scheduler.scheduleAtFixedRate(new CompetitionCleanupRunnableTask(), 1000*60*60);
  }
}
