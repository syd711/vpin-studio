package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class VPinStudioClient implements ObservedPropertyChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private final static String API = "api/v1/";

  private Map<String, ObservedProperties> observedProperties = new HashMap<>();

  private Map<String, byte[]> imageCache = new HashMap<>();

  private VPinStudioClient() {

  }

  public static VPinStudioClient create() {
    return new VPinStudioClient();
  }

  public ByteArrayInputStream getDirectB2SImage(GameRepresentation game) {
    byte[] bytes = RestClient.getInstance().readBinary(API + "directb2s/" + game.getId());
    return new ByteArrayInputStream(bytes);
  }

  public String getURL(String segment) {
    if (!segment.startsWith("http") && !segment.contains(API)) {
      return RestClient.getInstance().getBaseUrl() + API + segment;
    }
    return segment;
  }

  public PreferenceEntryRepresentation getPreference(String key) {
    return RestClient.getInstance().get(API + "preferences/" + key, PreferenceEntryRepresentation.class);
  }

  public boolean setPreferences(Map<String, Object> values) {
    try {
      return RestClient.getInstance().put(API + "preferences", values);
    } catch (Exception e) {
      LOG.error("Failed to set preferences: " + e.getMessage(), e);
    }
    return false;
  }

  public GameRepresentation getGame(int id) {
    try {
      return RestClient.getInstance().get(API + "games/" + id, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read game " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public GameRepresentation saveGame(GameRepresentation game) {
    try {
      return RestClient.getInstance().post(API + "games/save", game, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
    }
    return null;
  }

  public GameMediaRepresentation getGameMedia(int id) {
    return RestClient.getInstance().get(API + "poppermedia/" + id, GameMediaRepresentation.class);
  }

  public List<GameRepresentation> getGames() {
    return Arrays.asList(RestClient.getInstance().get(API + "games", GameRepresentation[].class));
  }

  public ByteArrayInputStream getHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    byte[] bytes = RestClient.getInstance().readBinary(API + "generator/card/" + gameId);
    return new ByteArrayInputStream(bytes);
  }

  public boolean generateHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    return RestClient.getInstance().get(API + "generator/cards/" + gameId, Boolean.class);
  }

  public boolean scanGame(GameRepresentation game) {
    int gameId = game.getId();
    return RestClient.getInstance().get(API + "games/scan/" + gameId, Boolean.class);
  }

  public List<String> getHighscoreBackgroundImages() {
    return Arrays.asList(RestClient.getInstance().get(API + "generator/backgrounds", String[].class));
  }

  public List<String> getCompetitionBadges() {
    return Arrays.asList(RestClient.getInstance().get(API + "generator/backgrounds", String[].class));
  }

  public ByteArrayInputStream getHighscoreBackgroundImage(String name) {
    try {
      if (!imageCache.containsKey(name)) {
        String encodedName = URLEncoder.encode(name, "utf8");
        byte[] bytes = RestClient.getInstance().readBinary(API + "generator/background/" + encodedName);
        imageCache.put(name, bytes);
      }

      byte[] imageBytes = imageCache.get(name);
      return new ByteArrayInputStream(imageBytes);
    } catch (UnsupportedEncodingException e) {
      LOG.error("Failed to read highscore background image: " + e.getMessage(), e);
    }
    return null;
  }

  public boolean uploadHighscoreBackgroundImage(File file) throws Exception {
    String url = "http://localhost:8099/api/v1/generator/backgroundupload";


//    try (CloseableHttpClient client = HttpClients.createDefault()) {
//      HttpPost post = new HttpPost(url);
//      HttpEntity entity = MultipartEntityBuilder.create().addPart("file", new FileBody(file)).build();
//      post.setEntity(entity);
//
//      try (CloseableHttpResponse response = client.execute(post)) {
//        // ...
//      }
//    }
    return false; //RestClient.getInstance().exchange(API + "generator/backgroundupload", HttpMethod.POST, createUpload(file, -1, null), Boolean.class);
  }

  public boolean uploadDirectB2SFile(File file, String uploadType, int gameId) throws Exception {
    return RestClient.getInstance().exchange(API + "generator/directb2supload", HttpMethod.POST, createUpload(file, gameId, uploadType), Boolean.class);
  }

  private static HttpEntity createUpload(File file, int gameId, String uploadType) throws Exception {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String boundary = Long.toHexString(System.currentTimeMillis());
    headers.set("Content-Type", "multipart/form-data; boundary=" + boundary);
    FileSystemResource rsr = new FileSystemResource(file);

    byte[] bFile = new byte[(int) file.length()];
    FileInputStream fileInputStream = new FileInputStream(file);
    fileInputStream.read(bFile);
    fileInputStream.close();

    ByteArrayResource contentsAsResource = new ByteArrayResource(bFile) {
      @Override
      public String getFilename() {
        return file.getName();
      }
    };

    map.add("file", rsr);
    map.add("gameId", gameId);
    map.add("uploadType", uploadType);
    return new HttpEntity<>(map, headers);
  }

  public ObservedProperties getProperties(String propertiesName) {
    if (!observedProperties.containsKey(propertiesName)) {
      Map<String, Object> result = RestClient.getInstance().get(API + "properties/" + propertiesName, Map.class);
      Properties properties = new Properties();
      properties.putAll(result);
      ObservedProperties observedProperties = new ObservedProperties(propertiesName, properties);
      observedProperties.setObserver(this);
      this.observedProperties.put(propertiesName, observedProperties);
    }

    return this.observedProperties.get(propertiesName);
  }

  @Override
  public void changed(String propertiesName, String key, Optional<String> updatedValue) {
    try {
      Map<String, Object> model = new HashMap<>();
      model.put(key, updatedValue.get());
      Boolean result = RestClient.getInstance().put(API + "properties/" + propertiesName, model);
      ObservedProperties obsprops = this.observedProperties.get(propertiesName);
      obsprops.notifyChange(key, updatedValue.get());
    } catch (Exception e) {
      LOG.error("Failed to upload changed properties: " + e.getMessage(), e);
    }
  }
}
