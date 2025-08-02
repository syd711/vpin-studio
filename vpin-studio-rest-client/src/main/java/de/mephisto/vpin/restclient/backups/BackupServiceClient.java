package de.mephisto.vpin.restclient.backups;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.*;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
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

  public BackupServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<BackupDescriptorRepresentation> getArchiveDescriptors(long id) {
    return Arrays.asList(getRestClient().get(API + "backups/" + id, BackupDescriptorRepresentation[].class));
  }

  public List<BackupSourceRepresentation> getArchiveSources() {
    return Arrays.asList(getRestClient().get(API + "backups/sources", BackupSourceRepresentation[].class));
  }

  public boolean deleteArchive(long sourceId, String filename) {
    return getRestClient().delete(API + "backups/descriptor/" + sourceId + "/" + filename);
  }

  public boolean deleteArchiveSource(long id) {
    return getRestClient().delete(API + "backups/source/" + id);
  }

  public BackupSourceRepresentation saveArchiveSource(BackupSourceRepresentation source) throws Exception {
    try {
      return getRestClient().post(API + "backups/save", source, BackupSourceRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<BackupDescriptorRepresentation> getArchiveDescriptorsForGame(int gameId) {
    return Arrays.asList(getRestClient().get(API + "backups/game/" + gameId, BackupDescriptorRepresentation[].class));
  }

  public boolean invalidateArchiveCache() {
    return getRestClient().get(API + "backups/invalidate", Boolean.class);
  }

  public JobDescriptor uploadArchive(File file, int repositoryId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "backups/upload/";
      HttpEntity upload = createUpload(file, repositoryId, null, AssetType.ARCHIVE, listener);
      JobDescriptor body = createUploadTemplate().exchange(url, HttpMethod.POST, upload, JobDescriptor.class).getBody();
      finalizeUpload(upload);
      return body;
    } catch (Exception e) {
      LOG.error("Archive upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public Future<JobDescriptor> uploadArchiveFuture(File file, int repositoryId, FileUploadProgressListener listener) {
    Callable<JobDescriptor> task = () -> {
      return this.uploadArchive(file, repositoryId, listener);
    };

    return executor.submit(task);
  }

  public boolean backupTable(BackupExportDescriptor exportDescriptor) {
    return getRestClient().post(API + "backups/backup", exportDescriptor, Boolean.class);
  }

  public boolean restoreTable(ArchiveRestoreDescriptor descriptor) {
    return getRestClient().post(API + "backups/restore", descriptor, Boolean.class);
  }
}
