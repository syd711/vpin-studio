package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.jobs.JobType;

public class JobFinishedEvent {
  private JobType jobType;
  private int gameId;
  private boolean cancelled;
  private final boolean finished;

  public JobFinishedEvent(JobType jobType, int gameId, boolean cancelled, boolean finished) {
    this.jobType = jobType;
    this.gameId = gameId;
    this.cancelled = cancelled;
    this.finished = finished;
  }

  public boolean isJobFinished() {
    return finished;
  }

  public boolean isJobCancelled() {
    return cancelled;
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
