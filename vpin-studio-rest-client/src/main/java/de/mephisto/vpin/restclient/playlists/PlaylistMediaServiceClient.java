package de.mephisto.vpin.restclient.playlists;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************************************
 * Playlist Media
 ********************************************************************************************************************/
public class PlaylistMediaServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String API_SEGMENT_MEDIA = "playlistmedia";

  private final Map<Integer, FrontendMediaRepresentation> cache = new HashMap<>();

  public PlaylistMediaServiceClient(VPinStudioClient client) {
    super(client);
  }

  public FrontendMediaRepresentation getPlaylistMedia(int playlistId) {
    FrontendMediaRepresentation frontendMediaRepresentation = getRestClient().get(API + API_SEGMENT_MEDIA + "/" + playlistId, FrontendMediaRepresentation.class);
    if (frontendMediaRepresentation != null) {
      cache.put(playlistId, frontendMediaRepresentation);
    }
    return frontendMediaRepresentation;
  }

  public FrontendMediaRepresentation getPlaylistMediaCached(int playlistId) {
    if (cache.containsKey(playlistId)) {
      return cache.get(playlistId);
    }
    return getPlaylistMedia(playlistId);
  }

  public boolean deleteMedia(int gameId, VPinScreen screen, String name) {
    return getRestClient().delete(API + "playlistmedia/" + gameId + "/" + screen.name() + "/" + name);
  }

  public boolean downloadPlaylistAsset(TableAsset tableAsset, VPinScreen screen, PlaylistRepresentation playlist, boolean append) throws Exception {
    try {
      return getRestClient().post(API + "playlistmedia/" + playlist.getId() + "/" + screen.name() + "/" + append, tableAsset, Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to download asset: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean addBlank(int gameId, VPinScreen screen) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("blank", "true");
      return getRestClient().put(API + API_SEGMENT_MEDIA + "/" + gameId + "/" + screen.name(), values);
    }
    catch (Exception e) {
      LOG.error("Adding blank asset failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public JobDescriptor uploadMedia(File file, int gameId, VPinScreen screen, boolean append, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + API_SEGMENT_MEDIA + "/upload/" + screen.name() + "/" + append;
      HttpEntity upload = createUpload(file, gameId, null, AssetType.FRONTEND_MEDIA, listener);
      ResponseEntity<JobDescriptor> exchange = new RestTemplate().exchange(url, HttpMethod.POST, upload, JobDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public void clearCache() {
    this.cache.clear();
  }
}
