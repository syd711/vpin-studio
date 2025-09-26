package de.mephisto.vpin.restclient.backups;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.*;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*********************************************************************************************************************
 * Archiving
 ********************************************************************************************************************/
public class BackupServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final List<BackupDescriptorRepresentation> backupsCached = new ArrayList<>();

  public BackupServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<BackupDescriptorRepresentation> getBackupsForSource(long sourceId) {
    return Arrays.asList(getRestClient().get(API + "backups/" + sourceId, BackupDescriptorRepresentation[].class));
  }

  public List<BackupDescriptorRepresentation> getBackups() {
    if (backupsCached.isEmpty()) {
      backupsCached.addAll(Arrays.asList(getRestClient().get(API + "backups", BackupDescriptorRepresentation[].class)));
    }
    return backupsCached;
  }

  public List<BackupSourceRepresentation> getBackupSources() {
    return Arrays.asList(getRestClient().get(API + "backups/sources", BackupSourceRepresentation[].class));
  }

  public boolean deleteBackup(long sourceId, String filename) {
    backupsCached.clear();
    return getRestClient().delete(API + "backups/" + sourceId + "/" + filename);
  }

  public boolean deleteBackupSource(long id) {
    return getRestClient().delete(API + "backups/source/" + id);
  }

  public BackupSourceRepresentation saveBackupSource(BackupSourceRepresentation source) throws Exception {
    try {
      backupsCached.clear();
      return getRestClient().post(API + "backups/save", source, BackupSourceRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<BackupDescriptorRepresentation> getBackupsForGame(int gameId) {
    return Arrays.asList(getRestClient().get(API + "backups/game/" + gameId, BackupDescriptorRepresentation[].class));
  }

  public boolean invalidateBackupCache() {
    return getRestClient().get(API + "backups/invalidate", Boolean.class);
  }

  public JobDescriptor uploadBackup(File file, int repositoryId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "backups/upload/";
      HttpEntity upload = createUpload(file, repositoryId, null, AssetType.ARCHIVE, listener);
      JobDescriptor body = createUploadTemplate().exchange(url, HttpMethod.POST, upload, JobDescriptor.class).getBody();
      finalizeUpload(upload);
      return body;
    }
    catch (Exception e) {
      LOG.error("Archive upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public Future<JobDescriptor> uploadBackupFuture(File file, int repositoryId, FileUploadProgressListener listener) {
    Callable<JobDescriptor> task = () -> {
      return this.uploadBackup(file, repositoryId, listener);
    };

    return executor.submit(task);
  }

  public boolean backupTable(BackupExportDescriptor exportDescriptor) {
    return getRestClient().post(API + "backups/backup", exportDescriptor, Boolean.class);
  }

  public boolean restoreTable(ArchiveRestoreDescriptor descriptor) {
    return getRestClient().post(API + "backups/restore", descriptor, Boolean.class);
  }

  @Nullable
  public BackupDescriptorRepresentation getBackup(@NonNull GameRepresentation value) {
    List<BackupDescriptorRepresentation> backups = getBackups();
    for (BackupDescriptorRepresentation backup : backups) {
      if (String.valueOf(backup.getTableDetails().getGameFileName()).equals(value.getGameFileName())) {
        return backup;
      }
    }
    return null;
  }
}
