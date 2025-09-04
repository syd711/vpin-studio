package de.mephisto.vpin.server.backups.adapters;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.backups.BackupSource;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Interface to be implemented by the different archiving types.
 */
public interface TableBackupAdapter extends Job {

  default void execute(@NonNull JobDescriptor jobDescriptor) {
    createBackup(jobDescriptor);
  }
  
  void createBackup(@NonNull JobDescriptor result);
}
