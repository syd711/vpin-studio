package de.mephisto.vpin.restclient.vpxz;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.TableDetails;
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
import java.lang.invoke.MethodHandles;
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
public class VPXZServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private final List<VPXZDescriptorRepresentation> vpxMobileCached = new ArrayList<>();

  private boolean dirty = true;

  public VPXZServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<VPXZDescriptorRepresentation> getVPXZForSource(long sourceId) {
    return Arrays.asList(getRestClient().get(API + "vpxz/" + sourceId, VPXZDescriptorRepresentation[].class));
  }

  public List<VPXZDescriptorRepresentation> getVPXZ() {
    if (vpxMobileCached.isEmpty() && dirty) {
      vpxMobileCached.addAll(Arrays.asList(getRestClient().get(API + "vpxz", VPXZDescriptorRepresentation[].class)));
      dirty = false;
    }
    return vpxMobileCached;
  }

  public List<VPXZSourceRepresentation> getVPXZSources() {
    return Arrays.asList(getRestClient().get(API + "vpxz/sources", VPXZSourceRepresentation[].class));
  }

  public boolean deleteVPXZ(long sourceId, String filename) {
    clearCache();
    return getRestClient().delete(API + "vpxmobile/" + sourceId + "/" + filename);
  }

  private void clearCache() {
    vpxMobileCached.clear();
    dirty = true;
  }

  public boolean deleteVPXZSource(long id) {
    return getRestClient().delete(API + "vpxz/source/" + id);
  }

  public VPXZSourceRepresentation saveVPXZSource(VPXZSourceRepresentation source) throws Exception {
    try {
      clearCache();
      return getRestClient().post(API + "vpxz/save", source, VPXZSourceRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<VPXZDescriptorRepresentation> getVPXZForGame(int gameId) {
    return Arrays.asList(getRestClient().get(API + "vpxz/game/" + gameId, VPXZDescriptorRepresentation[].class));
  }

  public boolean invalidateVPXZCache() {
    clearCache();
    return getRestClient().get(API + "vpxz/invalidate", Boolean.class);
  }

  public JobDescriptor uploadVPXZ(File file, int repositoryId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "vpxz/upload/";
      HttpEntity upload = createUpload(file, repositoryId, null, AssetType.ARCHIVE, listener);
      JobDescriptor body = createUploadTemplate().exchange(url, HttpMethod.POST, upload, JobDescriptor.class).getBody();
      finalizeUpload(upload);
      return body;
    }
    catch (Exception e) {
      LOG.error("VPXZ upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public Future<JobDescriptor> uploadVPXZFuture(File file, int repositoryId, FileUploadProgressListener listener) {
    Callable<JobDescriptor> task = () -> {
      return this.uploadVPXZ(file, repositoryId, listener);
    };

    return executor.submit(task);
  }

  public boolean vpxMobileTable(BackupExportDescriptor exportDescriptor) {
    return getRestClient().post(API + "vpxz/create", exportDescriptor, Boolean.class);
  }

  @Nullable
  public VPXZDescriptorRepresentation getBackup(@NonNull GameRepresentation value) {
    List<VPXZDescriptorRepresentation> backups = getVPXZ();
    for (VPXZDescriptorRepresentation backup : backups) {
      TableDetails tableDetails = backup.getTableDetails();
      if (tableDetails != null && String.valueOf(tableDetails.getGameFileName()).equals(value.getGameFileName())) {
        return backup;
      }
    }
    return null;
  }
}
