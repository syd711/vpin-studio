package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.games.descriptors.BackupExportDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.frontend.FrontendService;
import edu.umd.cs.findbugs.annotations.NonNull;

public class TableBackupJob implements Job {
  private final FrontendService frontendService;
  private final ArchiveSourceAdapter sourceAdapter;
  private final TableBackupAdapter tableBackupAdapter;
  private final BackupExportDescriptor backupExportDescriptor;
  private int gameId;

  public TableBackupJob(@NonNull FrontendService frontendService,
                        @NonNull ArchiveSourceAdapter sourceAdapter,
                        @NonNull TableBackupAdapter tableBackupAdapter,
                        @NonNull BackupExportDescriptor backupExportDescriptor,
                        int gameId) {
    this.frontendService = frontendService;
    this.sourceAdapter = sourceAdapter;
    this.tableBackupAdapter = tableBackupAdapter;
    this.backupExportDescriptor = backupExportDescriptor;
    this.gameId = gameId;
  }

  public void execute(JobDescriptor result) {
    tableBackupAdapter.execute(result);
    if (!result.isErrorneous()) {
      if (backupExportDescriptor.isRemoveFromPlaylists()) {
        frontendService.deleteFromPlaylists(gameId);
      }
      sourceAdapter.invalidate();
    }
  }

  @Override
  public boolean isCancelable() {
    return false;
  }
}
