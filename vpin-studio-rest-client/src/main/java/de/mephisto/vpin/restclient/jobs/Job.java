package de.mephisto.vpin.restclient.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;

public interface Job {
  void execute(JobDescriptor jobDescriptor);

  default void cancel(JobDescriptor jobDescriptor) {
  }

  default boolean isCancelable() {
    return true;
  }
}
