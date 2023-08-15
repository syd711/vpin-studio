package de.mephisto.vpin.server.archiving.adapters;

import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;

/**
 * Interface to be implemented by the different archiving types.
 */
public interface TableBackupAdapter extends Job {

  JobExecutionResult createBackup();
}
