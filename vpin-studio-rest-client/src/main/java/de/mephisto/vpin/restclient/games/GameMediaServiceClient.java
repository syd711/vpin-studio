package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
public class GameMediaServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String API_SEGMENT_MEDIA = "media";

  public GameMediaServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean deleteMedia(int gameId, VPinScreen screen, String name) {
    return getRestClient().delete(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen + "/" + name);
  }

  public boolean deleteMedia(int gameId) {
    return getRestClient().delete(API + API_SEGMENT_MEDIA + "/media/" + gameId);
  }

  public boolean setDefaultMedia(int gameId, VPinScreen screen, String name) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("setDefault", name);
    return getRestClient().put(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen, params, Boolean.class);
  }

  public boolean renameMedia(int gameId, VPinScreen screen, String name, String newName) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("oldName", name);
    params.put("newName", newName);
    return getRestClient().put(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen, params, Boolean.class);
  }

  public boolean toFullScreen(int gameId, VPinScreen screen) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("fullscreen", "true");
      return getRestClient().put(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen, values);
    }
    catch (Exception e) {
      LOG.error("Applying fullscreen mode failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean addBlank(int gameId, VPinScreen screen) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("blank", "true");
      return getRestClient().put(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen, values);
    }
    catch (Exception e) {
      LOG.error("Adding blank asset failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public FrontendMediaRepresentation getGameMedia(int gameId) {
    return getRestClient().get(API + API_SEGMENT_MEDIA + "/" + gameId, FrontendMediaRepresentation.class);
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

  public boolean copyMedia(VPinScreen screen, FrontendMediaItemRepresentation media) {
    AssetCopy copy = new AssetCopy();
    copy.setItem(media);
    copy.setTarget(screen);
    return getRestClient().post(API + API_SEGMENT_MEDIA + "/assets/copy", copy, Boolean.class);
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
  public String getUrl(TableAsset tableAsset, int gameId) {
    String url = tableAsset.getUrl();
    if (url.startsWith("/")) {
      // add API and do an URK encoding that will be decoded by spring-boot
      url = getRestClient().getBaseUrl() + API + API_SEGMENT_MEDIA + "/assets/d/" + tableAsset.getScreen() + "/" + tableAsset.getSourceId() + "/" + gameId + "/"
          + URLEncoder.encode(url.substring(1), StandardCharsets.UTF_8);
    }
    return url;
  }
}
