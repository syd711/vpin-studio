package de.mephisto.vpin.server.backup.adapters;

import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;

/**
 * Interface to be implemented by the different archiving types.
 */
public interface TableBackupAdapter extends Job {

  ArchiveDescriptor createBackup();
}
