package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

  public HighscoreCardsServiceClient(VPinStudioClient client) {
    super(client);
  }


  public ByteArrayInputStream getHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    byte[] bytes = getRestClient().readBinary(API + "cards/preview/" + gameId);
    return new ByteArrayInputStream(bytes);
  }

  public ByteArrayInputStream getHighscoreCardPreview(GameRepresentation game, CardTemplate template) {
    int gameId = game.getId();
    byte[] bytes = getRestClient().readBinary(API + "cards/preview/" + gameId + "/" + template.getId());
    if (bytes == null) {
      bytes = new byte[]{};
    }
    return new ByteArrayInputStream(bytes);
  }

  public CardData getHighscoreCardData(GameRepresentation game, CardTemplate template) {
    return getRestClient().get(API + "cards/gamedata/" + game.getId() + "/" + template.getId(), CardData.class);
  }


  public boolean generateHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    return getRestClient().get(API + "cards/generate/" + gameId, Boolean.class);
  }

  public List<String> getHighscoreBackgroundImages() {
    return Arrays.asList(getRestClient().get(API + "cards/backgrounds", String[].class));
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
      HttpEntity upload = createUpload(file, -1, null, AssetType.CARD_BACKGROUND, listener);
      new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      finalizeUpload(upload);
      return true;
    }
    catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
