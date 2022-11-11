package de.mephisto.vpin.server.competitions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetitionService  {
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
    Competition competition = getCompetition(c.getId());
    competitionsRepository.saveAndFlush(competition);
    LOG.info("Saved " + competition);
    return getCompetition(c.getId());
  }
}
