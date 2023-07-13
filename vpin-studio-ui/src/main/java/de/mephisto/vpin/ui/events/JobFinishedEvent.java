package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.jobs.JobType;

public class JobFinishedEvent {
  private JobType jobType;
  private int gameId;

  public JobFinishedEvent(JobType jobType) {
    this.jobType = jobType;
  }

  public JobFinishedEvent(JobType jobType, int gameId) {
    this.jobType = jobType;
    this.gameId = gameId;
  }

  public void setJobType(JobType jobType) {
    this.jobType = jobType;
  }

  public int getGameId() {
    return gameId;
  }

  public JobType getJobType() {
    return jobType;
  }
}
