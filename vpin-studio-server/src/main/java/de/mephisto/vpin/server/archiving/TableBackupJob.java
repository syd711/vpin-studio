package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.games.descriptors.BackupDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.frontend.FrontendService;
import edu.umd.cs.findbugs.annotations.NonNull;

public class TableBackupJob implements Job {
  private final FrontendService frontendService;
  private final ArchiveSourceAdapter sourceAdapter;
  private final TableBackupAdapter tableBackupAdapter;
  private final BackupDescriptor backupDescriptor;
  private int gameId;

  public TableBackupJob(@NonNull FrontendService frontendService,
                        @NonNull ArchiveSourceAdapter sourceAdapter,
                        @NonNull TableBackupAdapter tableBackupAdapter,
                        @NonNull BackupDescriptor backupDescriptor,
                        int gameId) {
    this.frontendService = frontendService;
    this.sourceAdapter = sourceAdapter;
    this.tableBackupAdapter = tableBackupAdapter;
    this.backupDescriptor = backupDescriptor;
    this.gameId = gameId;
  }

  public void execute(JobDescriptor result) {
    tableBackupAdapter.execute(result);
    if (!result.isErrorneous()) {
      if (backupDescriptor.isRemoveFromPlaylists()) {
        frontendService.deleteFromPlaylists(gameId);
      }
      sourceAdapter.invalidate();
    }
  }
}
