package de.mephisto.vpin.server.competitions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompetitionCheckRunnable implements Runnable {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionCheckRunnable.class);

  private CompetitionService competitionService;

  public CompetitionCheckRunnable(CompetitionService competitionService) {
    this.competitionService = competitionService;
  }

  @Override
  public void run() {
    LOG.info("Running automated competition status check.");
    this.competitionService.runFinishedCompetitionsCheck();
  }
}