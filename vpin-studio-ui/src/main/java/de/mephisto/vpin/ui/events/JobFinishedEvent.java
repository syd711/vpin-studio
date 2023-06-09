package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.jobs.JobType;

public class JobFinishedEvent {
  private JobType jobType;

  public JobFinishedEvent(JobType jobType) {
    this.jobType = jobType;
  }

  public JobType getJobType() {
    return jobType;
  }
}
