package de.mephisto.vpin.server.archiving.adapters;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;

/**
 * Interface to be implemented by the different archiving types.
 */
public interface TableBackupAdapter extends Job {

  void createBackup(JobDescriptor result);
}
