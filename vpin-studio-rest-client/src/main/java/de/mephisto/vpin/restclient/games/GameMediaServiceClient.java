package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

/*********************************************************************************************************************
 * Popper
 ********************************************************************************************************************/
public class GameMediaServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private static final int CACHE_SIZE = 300;
  private static final String API_SEGMENT_MEDIA = "media";

  private List<TableAssetSearch> cache = new ArrayList<>();

  public GameMediaServiceClient(VPinStudioClient client) {
    super(client);
  }


  public boolean deleteMedia(int gameId, VPinScreen screen, String name) {
    return getRestClient().delete(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen.name() + "/" + name);
  }


  public boolean renameMedia(int gameId, VPinScreen screen, String name, String newName) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("oldName", name);
    params.put("newName", newName);
    return getRestClient().put(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen.name(), params, Boolean.class);
  }

  public boolean toFullScreen(int gameId, VPinScreen screen) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("fullscreen", "true");
      return getRestClient().put(API + API_SEGMENT_MEDIA +"/media/" + gameId + "/" + screen.name(), values);
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
      return getRestClient().put(API + API_SEGMENT_MEDIA + "/media/" + gameId + "/" + screen.name(), values);
    }
    catch (Exception e) {
      LOG.error("Adding blank asset failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public GameMediaRepresentation getGameMedia(int gameId) {
    return getRestClient().get(API + API_SEGMENT_MEDIA + "/" + gameId, GameMediaRepresentation.class);
  }


  public JobExecutionResult uploadMedia(File file, int gameId, VPinScreen screen, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + API_SEGMENT_MEDIA + "/upload/" + screen.name();
      HttpEntity upload = createUpload(file, gameId, null, AssetType.POPPER_MEDIA, listener);
      ResponseEntity<JobExecutionResult> exchange = new RestTemplate().exchange(url, HttpMethod.POST, upload, JobExecutionResult.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Popper media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }


  public UploadDescriptor uploadPack(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + API_SEGMENT_MEDIA + "/packupload";
      HttpEntity upload = createUpload(file, gameId, null, AssetType.POPPER_MEDIA, listener);
      ResponseEntity<UploadDescriptor> exchange = new RestTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Popper media upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  //---------------- Assets---------------------------------------------------------------------------------------------

  public synchronized TableAssetSearch getCached(VPinScreen screen, String term) {
    for (TableAssetSearch s : this.cache) {
      if (s.getTerm().equals(term) && s.getScreen().equals(screen)) {
        return s;
      }
    }

    if (!StringUtils.isEmpty(term) && term.trim().contains(" ")) {
      term = term.split(" ")[0];
      for (TableAssetSearch s : this.cache) {
        if (s.getTerm().equals(term) && s.getScreen().equals(screen)) {
          return s;
        }
      }
    }

    return null;
  }

  public synchronized TableAssetSearch searchTableAsset(int gameId, VPinScreen screen, String term) throws Exception {
    term = term.replaceAll("/", "");
    term = term.replaceAll("&", " ");
    term = term.replaceAll(",", " ");

    TableAssetSearch cached = getCached(screen, term);
    if (cached != null) {
      return cached;
    }

    TableAssetSearch search = new TableAssetSearch();
    search.setGameId(gameId);
    search.setTerm(term);
    search.setScreen(screen);
    TableAssetSearch result = getRestClient().post(API + API_SEGMENT_MEDIA + "/assets/search", search, TableAssetSearch.class);
    if (result != null) {
      if (result.getResult().isEmpty() && !StringUtils.isEmpty(term) && term.trim().contains(" ")) {
        String[] split = term.trim().split(" ");
        return searchTableAsset(gameId, screen, split[0]);
      }

      cache.add(result);
      if (cache.size() > CACHE_SIZE) {
        cache.remove(0);
      }
      return result;
    }
    return search;
  }

  public boolean downloadTableAsset(TableAsset tableAsset, VPinScreen screen, GameRepresentation game, boolean append) throws Exception {
    try {
      return getRestClient().post(API + API_SEGMENT_MEDIA + "/assets/download/" + game.getId() + "/" + screen.name() + "/" + append, tableAsset, Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save b2s server settings: " + e.getMessage(), e);
      throw e;
    }
  }

  /**
   * @param tableAsset The TableAsset from which we need the URL
   * @return the URL of the asset, prepended by API segments when it starts with "/"
   */
  public String getUrl(TableAsset tableAsset) {
    String url = tableAsset.getUrl();
    if (url.startsWith("/")) {
      url = getRestClient().getBaseUrl() + API + API_SEGMENT_MEDIA + url;
    }
    return url;
  }

  public void clearCache() {
    getRestClient().clearCache("popper/emulators");
    this.cache.clear();
  }
}
