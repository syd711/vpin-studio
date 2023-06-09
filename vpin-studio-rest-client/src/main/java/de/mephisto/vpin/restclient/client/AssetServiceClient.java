package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.FileUploadProgressListener;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.AssetRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;

/*********************************************************************************************************************
 * Assets / Popper
 ********************************************************************************************************************/
public class AssetServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  AssetServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean uploadDefaultBackgroundFile(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "assets/background";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, null, AssetType.DEFAULT_BACKGROUND, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Default background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean regenerateGameAssets(int gameId) {
    try {
      getRestClient().delete(API + "assets/background/" + gameId);
      return true;
    } catch (Exception e) {
      LOG.error("Default background deletion failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    String url = API + "poppermedia/" + id + "/" + screen.name();
    if (!client.getImageCache().containsKey(url) && screen.equals(PopperScreen.Wheel)) {
      byte[] bytes = getRestClient().readBinary(url);
      if (bytes == null) {
        bytes = new byte[]{};
      }
      client.getImageCache().put(url, bytes);
    }

    if (screen.equals(PopperScreen.Wheel)) {
      byte[] imageBytes = client.getImageCache().get(url);
      return new ByteArrayInputStream(imageBytes);
    }

    byte[] bytes = getRestClient().readBinary(url);
    return new ByteArrayInputStream(bytes);
  }

  public AssetRepresentation uploadAsset(File file, long id, int maxSize, AssetType assetType, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "assets/" + id + "/upload/" + maxSize;
      LOG.info("HTTP POST " + url);
      ResponseEntity<AssetRepresentation> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, assetType, listener), AssetRepresentation.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Asset upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    if (assetType.equals(AssetType.AVATAR) && client.getImageCache().containsKey(uuid)) {
      return new ByteArrayInputStream(client.getImageCache().get(uuid));
    }

    byte[] bytes = getRestClient().readBinary(API + "assets/data/" + uuid);
    if (bytes == null) {
      throw new UnsupportedOperationException("No data found for asset with UUID " + uuid);
    }

    if (assetType.equals(AssetType.AVATAR)) {
      client.getImageCache().put(uuid, bytes);
    }
    return new ByteArrayInputStream(bytes);
  }
}
