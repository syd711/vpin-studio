package de.mephisto.vpin.server.competitions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitionService {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionService.class);

  @Autowired
  private CompetitionsRepository competitionsRepository;

  public List<Competition> getCompetitions() {
    return competitionsRepository.findAll();
  }

  public Competition getCompetition(long id) {
    Optional<Competition> competition = competitionsRepository.findById(id);
    return competition.orElse(null);
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

  public void deleteCompetition(long id) {
    competitionsRepository.deleteById(id);
  }
}
