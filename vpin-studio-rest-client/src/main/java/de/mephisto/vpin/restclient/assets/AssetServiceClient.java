package de.mephisto.vpin.restclient.assets;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/*********************************************************************************************************************
 * Assets
 ********************************************************************************************************************/
public class AssetServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String UUID_MAIN_AVATAR = "__MAIN_AVATAR__";

  public AssetServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean uploadDefaultBackgroundFile(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "assets/background";
      HttpEntity upload = createUpload(file, gameId, null, AssetType.DEFAULT_BACKGROUND, listener);
      new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      finalizeUpload(upload);
      return true;
    }
    catch (Exception e) {
      LOG.error("Default background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean deleteGameAssets(int gameId) {
    try {
      getRestClient().delete(API + "assets/background/" + gameId);
      return true;
    }
    catch (Exception e) {
      LOG.error("Default background deletion failed: " + e.getMessage(), e);
      throw e;
    }
  }

  @Nullable
  public ByteArrayInputStream getGameMediaItem(int id, @Nullable VPinScreen screen) {
    return getGameMediaItem(id, screen, null);
  }

  @Nullable
  public ByteArrayInputStream getWheelIcon(int id, boolean skipApng) {
    //------------
    // The Wheel is cached for performance reason
    //goes to the GameMediaResource, 404 is not a bug
    String url = API + "media/" + id + "/" + VPinScreen.Wheel.name() + "?preview=" + skipApng;
    if (!client.getImageCache().containsKey(url)) {
      byte[] bytes = getRestClient().readBinary(url);
      if (bytes == null) {
        bytes = new byte[]{};
      }
      client.getImageCache().put(url, bytes);
    }

    byte[] imageBytes = client.getImageCache().get(url);
    if (imageBytes == null || imageBytes.length == 0) {
      return null;
    }
    return new ByteArrayInputStream(imageBytes);
  }

  @Nullable
  public ByteArrayInputStream getGameMediaItem(int id, @Nullable VPinScreen screen, String name) {
    try {
      if (screen == null) {
        return null;
      }

      if (screen.equals(VPinScreen.Wheel)) {
        return getWheelIcon(id, false);
      }

      String url = API + "media/" + id + "/" + screen.name();
      if (name != null) {
        url += "/" + URLEncoder.encode(name, Charset.defaultCharset());
      }

      byte[] bytes = getRestClient().readBinary(url);
      if (bytes != null) {
        return new ByteArrayInputStream(bytes);
      }
    }
    catch (Exception e) {
      LOG.error("Error reading game media item for " + id + " and " + screen + ": " + e.getMessage(), e);
    }
    return null;
  }

  public AssetRepresentation uploadAsset(File file, long id, int maxSize, AssetType assetType, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "assets/" + id + "/upload/" + maxSize;
      LOG.info("HTTP POST " + url);
      ResponseEntity<AssetRepresentation> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, assetType, listener), AssetRepresentation.class);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Asset upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean isMediaIndexAvailable() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "assets/index/exists", Boolean.class);
  }

  public AssetRequest getMetadata(int gameId, VPinScreen screen, String name) {
    try {
      AssetRequest request = new AssetRequest();
      request.setScreen(screen);
      request.setGameId(gameId);
      request.setName(name);
      return getRestClient().post(API + "assets/metadata", request, AssetRequest.class);
    }
    catch (Exception e) {
      LOG.error("Failed to convert video: " + e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public ByteArrayInputStream getAvatar(boolean forceRefresh) {
    try {
      if (!forceRefresh && client.getImageCache().containsKey(UUID_MAIN_AVATAR)) {
        return new ByteArrayInputStream(client.getImageCache().get(UUID_MAIN_AVATAR));
      }
      byte[] bytes = getRestClient().readBinary(API + "assets/avatar");
      client.getImageCache().put(UUID_MAIN_AVATAR, bytes);
      return new ByteArrayInputStream(bytes);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    if (assetType.equals(AssetType.AVATAR) && client.getImageCache().containsKey(uuid)) {
      return new ByteArrayInputStream(client.getImageCache().get(uuid));
    }

    byte[] bytes = getRestClient().readBinary(API + "assets/data/" + uuid);
    if (bytes == null) {
      return null;
    }

    if (assetType.equals(AssetType.AVATAR)) {
      client.getImageCache().put(uuid, bytes);
    }
    return new ByteArrayInputStream(bytes);
  }
}
