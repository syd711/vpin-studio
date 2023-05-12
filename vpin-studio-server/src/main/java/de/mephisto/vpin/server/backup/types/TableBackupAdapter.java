package de.mephisto.vpin.server.backup.types;

import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Interface to be implemented by the different archiving types.
 */
public interface TableBackupAdapter extends Job {

  ArchiveDescriptor createBackup();
}
