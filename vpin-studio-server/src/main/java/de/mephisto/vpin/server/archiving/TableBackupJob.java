package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.descriptors.BackupDescriptor;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.popper.PinUPConnector;
import edu.umd.cs.findbugs.annotations.NonNull;

public class TableBackupJob implements Job {
  private final PinUPConnector pinUPConnector;
  private final ArchiveSourceAdapter sourceAdapter;
  private final TableBackupAdapter tableBackupAdapter;
  private final BackupDescriptor backupDescriptor;
  private int gameId;

  public TableBackupJob(@NonNull PinUPConnector pinUPConnector,
                        @NonNull ArchiveSourceAdapter sourceAdapter,
                        @NonNull TableBackupAdapter tableBackupAdapter,
                        @NonNull BackupDescriptor backupDescriptor,
                        int gameId) {
    this.pinUPConnector = pinUPConnector;
    this.sourceAdapter = sourceAdapter;
    this.tableBackupAdapter = tableBackupAdapter;
    this.backupDescriptor = backupDescriptor;
    this.gameId = gameId;
  }

  @Override
  public double getProgress() {
    return tableBackupAdapter.getProgress();
  }

  @Override
  public String getStatus() {
    return tableBackupAdapter.getStatus();
  }

  public JobExecutionResult execute() {
    JobExecutionResult result = tableBackupAdapter.execute();
    if (!result.isErrorneous()) {
      if(backupDescriptor.isRemoveFromPlaylists()) {
        pinUPConnector.deleteFromPlaylists(gameId);
      }
      sourceAdapter.invalidate();
    }
    return result;
  }
}
