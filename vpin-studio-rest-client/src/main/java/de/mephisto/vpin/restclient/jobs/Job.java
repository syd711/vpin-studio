package de.mephisto.vpin.restclient.jobs;

public interface Job {

  JobExecutionResult execute();

  double getProgress();

  String getStatus();
}
