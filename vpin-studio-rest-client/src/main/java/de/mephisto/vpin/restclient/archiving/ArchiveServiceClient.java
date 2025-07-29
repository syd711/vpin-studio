package de.mephisto.vpin.restclient.archiving;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.*;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*********************************************************************************************************************
 * Archiving
 ********************************************************************************************************************/
public class ArchiveServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public ArchiveServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<ArchiveDescriptorRepresentation> getArchiveDescriptors(long id) {
    return Arrays.asList(getRestClient().get(API + "archives/" + id, ArchiveDescriptorRepresentation[].class));
  }

  public List<ArchiveSourceRepresentation> getArchiveSources() {
    return Arrays.asList(getRestClient().get(API + "archives/sources", ArchiveSourceRepresentation[].class));
  }

  public boolean deleteArchive(long sourceId, String filename) {
    return getRestClient().delete(API + "archives/descriptor/" + sourceId + "/" + filename);
  }

  public boolean deleteArchiveSource(long id) {
    return getRestClient().delete(API + "archives/source/" + id);
  }

  public ArchiveSourceRepresentation saveArchiveSource(ArchiveSourceRepresentation source) throws Exception {
    try {
      return getRestClient().post(API + "archives/save", source, ArchiveSourceRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<ArchiveDescriptorRepresentation> getArchiveDescriptorsForGame(int gameId) {
    return Arrays.asList(getRestClient().get(API + "archives/game/" + gameId, ArchiveDescriptorRepresentation[].class));
  }

  public boolean invalidateArchiveCache() {
    return getRestClient().get(API + "archives/invalidate", Boolean.class);
  }

  public JobDescriptor uploadArchive(File file, int repositoryId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "archives/upload/";
      HttpEntity upload = createUpload(file, repositoryId, null, AssetType.ARCHIVE, listener);
      JobDescriptor body = createUploadTemplate().exchange(url, HttpMethod.POST, upload, JobDescriptor.class).getBody();
      finalizeUpload(upload);
      return body;
    } catch (Exception e) {
      LOG.error("Archive upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public Future<JobDescriptor> uploadArchiveFuture(File file, int repositoryId, FileUploadProgressListener listener) throws Exception {
    Callable<JobDescriptor> task = () -> {
      return this.uploadArchive(file, repositoryId, listener);
    };

    return executor.submit(task);
  }

  public boolean backupTable(BackupDescriptor exportDescriptor) throws Exception {
    return getRestClient().post(API + "archives/backup", exportDescriptor, Boolean.class);
  }

  public boolean restoreTable(ArchiveRestoreDescriptor descriptor) throws Exception {
    return getRestClient().post(API + "archives/restore", descriptor, Boolean.class);
  }
}
