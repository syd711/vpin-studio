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

import javax.annotation.Nullable;


/*********************************************************************************************************************
 * Highscore Cards
 ********************************************************************************************************************/
public class HighscoreCardsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public HighscoreCardsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public CardResolution getCardResolution(CardTemplateType templateType) {
    return getRestClient().get(API + "cards/resolution/" + templateType, CardResolution.class);
  }

  public String getHighscoreCardUrl(GameRepresentation game, CardTemplateType templateType) {
    int gameId = game.getId();
    return getRestClient().getBaseUrl() + API + "cards/preview/" + gameId + "/" + templateType;
  }

  public @Nullable ByteArrayInputStream getHighscoreCardPreview(GameRepresentation game, CardTemplateType templateType) {
    byte[] bytes = getRestClient().readBinary(API + "cards/preview/" + game.getId() + "/" + templateType);
    return bytes != null ? new ByteArrayInputStream(bytes) : null;
  }

  public ByteArrayInputStream getHighscoreCardPreview(GameRepresentation game, CardTemplate template) {
    byte[] bytes = getRestClient().readBinary(API + "cards/preview/" + game.getId() + "/" + template.getId());
    return new ByteArrayInputStream(bytes != null ? bytes : new byte[] {});
  }

  public CardData getHighscoreCardData(GameRepresentation game, CardTemplate template) {
    return getRestClient().get(API + "cards/gamedata/" + game.getId() + "/" + template.getId(), CardData.class);
  }


  public boolean generateHighscoreCard(GameRepresentation game, CardTemplateType templateType) {
    int gameId = game.getId();
    return getRestClient().get(API + "cards/generate/" + gameId + "/" + templateType, Boolean.class);
  }

  public byte[] getHighscoreImage(GameRepresentation game, CardTemplate template, String image) {
    return getRestClient().readBinary(API + "cards/image/" + image + "/" + game.getId() + "/" + template.getId());
  }

  //-------------------------------------------

  public List<String> getCardsBackgroundImages() {
    return Arrays.asList(getRestClient().get(API + "cards/backgrounds", String[].class));
  }

  public List<String> getCardsFrameImages() {
    return Arrays.asList(getRestClient().get(API + "cards/frames", String[].class));
  }

  public byte[] getCardsBackgroundImage(String name) {
    return doGetCardsImage("cards/background", name);
  }

  public byte[] getCardsFrameImage(String name) {
    return doGetCardsImage("cards/frame", name);
  }

  private byte[] doGetCardsImage(String path, String name) {
    if (name == null) {
      return null;
    }

    if (!client.getImageCache().containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = getRestClient().readBinary(API + path + "/" + encodedName);
      client.getImageCache().put(name, bytes);
    }

    return client.getImageCache().get(name);
  }

  public boolean uploadCardsBackgroundImage(File file, FileUploadProgressListener listener) throws Exception {
    return doUploadImage(file, listener, "cards/backgroundUpload");
  }
  public boolean uploadCardsFrameImage(File file, FileUploadProgressListener listener) throws Exception {
    return doUploadImage(file, listener, "cards/frameUpload");
  }

  private boolean doUploadImage(File file, FileUploadProgressListener listener, String path) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + path;
      HttpEntity<?> upload = createUpload(file, -1, null, AssetType.CARD_ASSET, listener);
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
