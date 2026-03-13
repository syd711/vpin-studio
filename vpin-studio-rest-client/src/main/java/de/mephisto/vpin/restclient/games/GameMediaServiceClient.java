package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************************************
 * Game Media
 ********************************************************************************************************************/
//TODO rename as MediaServiceClient as this is not just for Game anymore, move in assets package ?
public class GameMediaServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String API_SEGMENT_MEDIA = "media";
  private static final String API_SEGMENT_PLAYLISTMEDIA = "playlistmedia";

  private final Map<Integer, FrontendMediaRepresentation> cache = new HashMap<>();

  public GameMediaServiceClient(VPinStudioClient client) {
    super(client);
  }

  //--------------------------------

  public FrontendMediaRepresentation getMedia(int objectId, boolean playlistMode) {
    return playlistMode ? getPlaylistMedia(objectId) : getGameMedia(objectId);
  }

  public FrontendMediaRepresentation getGameMedia(int gameId) {
    return getRestClient().get(API + API_SEGMENT_MEDIA + "/" + gameId, FrontendMediaRepresentation.class);
  }

  public FrontendMediaRepresentation getPlaylistMedia(int playlistId) {
    FrontendMediaRepresentation frontendMediaRepresentation = getRestClient().get(API + API_SEGMENT_PLAYLISTMEDIA + "/" + playlistId, FrontendMediaRepresentation.class);
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

  public void clearPlaylistMediaCache() {
    this.cache.clear();
  }

  //--------------------------------

  private String getSegment(boolean playlistMode) {
    return playlistMode ? API_SEGMENT_PLAYLISTMEDIA : API_SEGMENT_MEDIA;
  }

  public AssetMetaData getMetadata(int objectId, boolean playlistMode, VPinScreen screen, String name) {
    return getRestClient().get(API + getSegment(playlistMode) + "/metadata/" + objectId + "/" + screen + "/" + name, AssetMetaData.class);
  }

  public boolean deleteMedia(int objectId, boolean playlistMode, VPinScreen screen, String name) {
    return getRestClient().delete(API + getSegment(playlistMode) + "/media/" + objectId + "/" + screen + "/" + name);
  }

  public boolean deleteMedia(int objectId, boolean playlistMode) {
    return getRestClient().delete(API + getSegment(playlistMode) + "/media/" + objectId);
  }

  public boolean setDefaultMedia(int objectId, boolean playlistMode, VPinScreen screen, String name) {
    Map<String, Object> params = new HashMap<>();
    params.put("setDefault", name);
    return getRestClient().put(API + getSegment(playlistMode) + "/media/" + objectId + "/" + screen, params, Boolean.class);
  }

  public boolean renameMedia(int objectId, boolean playlistMode, VPinScreen screen, String name, String newName) {
    Map<String, Object> params = new HashMap<>();
    params.put("oldName", name);
    params.put("newName", newName);
    return getRestClient().put(API + getSegment(playlistMode) + "/media/" + objectId + "/" + screen, params, Boolean.class);
  }

  public boolean toFullScreen(int objectId, boolean playlistMode, VPinScreen screen) {
    Map<String, Object> values = new HashMap<>();
    values.put("fullscreen", "true");
    return getRestClient().put(API + getSegment(playlistMode) + "/media/" + objectId + "/" + screen, values);
  }

  public boolean addBlank(int objectId, boolean playlistMode, VPinScreen screen) {
    Map<String, Object> values = new HashMap<>();
    values.put("blank", "true");
    return getRestClient().put(API + getSegment(playlistMode) + "/media/" + objectId + "/" + screen, values);
  }

  public boolean copyMedia(int objectId, boolean playlistMode, VPinScreen screen, String name, VPinScreen targetScreen) {
    Map<String, Object> values = new HashMap<>();
    values.put("copy", name);
    values.put("target", targetScreen);
    return getRestClient().put(API + getSegment(playlistMode) + "/media/" + objectId + "/" + screen, values);
  }

  //-------------------------

  public JobDescriptor uploadMedia(File file, int objectId, boolean playlistMode, VPinScreen screen, boolean append, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + getSegment(playlistMode) + "/upload/" + screen.name() + "/" + append;
      HttpEntity<MultiValueMap<String, Object>> upload = createUpload(file, objectId, null, AssetType.FRONTEND_MEDIA, listener);
      ResponseEntity<JobDescriptor> exchange = new RestTemplate().exchange(url, HttpMethod.POST, upload, JobDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  //---------------- Assets---------------------------------------------------------------------------------------------

  public TableAssetSource getTableAssetsConf() {
    return getRestClient().get(API + API_SEGMENT_MEDIA + "/assets/search/conf", TableAssetSource.class);
  }

  public synchronized TableAssetSearch searchTableAsset(@Nullable TableAssetSource source, int gameId, VPinScreen screen, String term) throws Exception {
    term = term.replaceAll("/", "");
    term = term.replaceAll("&", " ");
    term = term.replaceAll(",", " ");

    TableAssetSearch search = new TableAssetSearch();
    search.setGameId(gameId);
    search.setTerm(term);
    search.setScreen(screen);
    search.setAssetSourceId(source != null ? source.getId() : null);
    TableAssetSearch result = getRestClient().post(API + API_SEGMENT_MEDIA + "/assets/search", search, TableAssetSearch.class);
    if (result != null) {
      if (result.getResult().isEmpty() && !StringUtils.isEmpty(term) && term.trim().contains(" ")) {
        String[] split = term.trim().split(" ");
        return searchTableAsset(source, gameId, screen, split[0]);
      }
      return result;
    }
    return search;
  }

  public boolean downloadTableAsset(TableAsset tableAsset, VPinScreen screen, GameRepresentation game, boolean append) throws Exception {
    try {
      return getRestClient().post(API + API_SEGMENT_MEDIA + "/assets/download/" + game.getId() + "/" + screen + "/" + append, tableAsset, Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to download asset: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean downloadPlaylistAsset(TableAsset tableAsset, VPinScreen screen, PlaylistRepresentation playlist, boolean append) throws Exception {
    try {
      return getRestClient().post(API + API_SEGMENT_PLAYLISTMEDIA + "/" + playlist.getId() + "/" + screen.name() + "/" + append, tableAsset, Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to download asset: " + e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Returns the error message or null in case of success
   */
  public boolean testConnection(@NonNull String assetSourceId) throws Exception {
    return getRestClient().get(API + API_SEGMENT_MEDIA + "/assets/" + assetSourceId + "/test", Boolean.class);
  }

  public boolean invalidateMediaCache(@NonNull String assetSourceId) {
    return getRestClient().get(API + API_SEGMENT_MEDIA + "/assets/" + assetSourceId + "/invalidateMediaCache", Boolean.class);
  }

  /**
   * @param tableAsset The TableAsset from which we need the URL
   * @return the URL of the asset, prepended by API segments when it starts with "/" (usefull fo pinballX)
   */
  public String getUrl(TableAsset tableAsset, int objectId) {
    String url = tableAsset.getUrl();
    if (url.startsWith("/")) {
      // add API and do an URK encoding that will be decoded by spring-boot
      url = getRestClient().getBaseUrl() + API + API_SEGMENT_MEDIA + "/assets/d/" + tableAsset.getScreen() + "/" + tableAsset.getSourceId() + "/" + objectId + "/"
          + URLEncoder.encode(url.substring(1), StandardCharsets.UTF_8);
    }
    return url;
  }
}
