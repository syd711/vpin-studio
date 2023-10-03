package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.tables.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


/*********************************************************************************************************************
 * Highscore Cards
 ********************************************************************************************************************/
public class HighscoreCardsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  HighscoreCardsServiceClient(VPinStudioClient client) {
    super(client);
  }


  public ByteArrayInputStream getHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    byte[] bytes = getRestClient().readBinary(API + "cards/preview/" + gameId);
    return new ByteArrayInputStream(bytes);
  }

  public boolean generateHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    return getRestClient().get(API + "cards/generate/" + gameId, Boolean.class);
  }

  public boolean generateHighscoreCardSample(GameRepresentation game) {
    int gameId = game.getId();
    return getRestClient().get(API + "cards/generatesample/" + gameId, Boolean.class);
  }

  public List<String> getHighscoreBackgroundImages() {
    return Arrays.asList(getRestClient().get(API + "cards/backgrounds", String[].class));
  }

  public ByteArrayInputStream getOverlayBackgroundImage(String name) {
    if (!client.getImageCache().containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = getRestClient().readBinary(API + "overlay/background/" + encodedName);
      client.getImageCache().put(name, bytes);
    }

    byte[] imageBytes = client.getImageCache().get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  public ByteArrayInputStream getHighscoreBackgroundImage(String name) {
    if (!client.getImageCache().containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = getRestClient().readBinary(API + "cards/background/" + encodedName);
      client.getImageCache().put(name, bytes);
    }

    byte[] imageBytes = client.getImageCache().get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  public boolean uploadHighscoreBackgroundImage(File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "cards/backgroundupload";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.CARD_BACKGROUND, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean uploadTable(File file, TableUploadDescriptor tableUploadDescriptor, int gameId, int emuId, FileUploadProgressListener listener) {
    try {
      String url = getRestClient().getBaseUrl() + API + "games/upload/table";
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("mode", tableUploadDescriptor.name());
      map.add("gameId", gameId);
      map.add("emuId", emuId);
      createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(map, file, -1, null, AssetType.TABLE, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
