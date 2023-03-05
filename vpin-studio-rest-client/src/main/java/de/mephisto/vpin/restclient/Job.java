package de.mephisto.vpin.restclient;

public interface Job {

  boolean execute();

  double getProgress();

  String getStatus();
}
