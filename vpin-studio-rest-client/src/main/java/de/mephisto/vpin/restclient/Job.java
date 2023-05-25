package de.mephisto.vpin.restclient;

public interface Job {

  JobExecutionResult execute();

  double getProgress();

  String getStatus();
}
