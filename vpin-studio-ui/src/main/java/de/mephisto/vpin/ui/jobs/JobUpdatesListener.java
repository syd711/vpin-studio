package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;

import java.util.List;

public interface JobUpdatesListener {

  void jobsRefreshed(List<JobDescriptor> activeJobs);
}
