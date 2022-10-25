package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

public class VPinStudioClient implements ObservedPropertyChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private final static String API = "api/v1/";

  private static Map<String, ObservedProperties> observedProperties = new HashMap<>();

  private static Map<String, byte[]> imageCache = new HashMap<>();

  public ByteArrayInputStream getDirectB2SImage(GameRepresentation game) {
    byte[] bytes = RestClient.getInstance().readBinary(API + "directb2s/" + game.getId());
    return new ByteArrayInputStream(bytes);
  }

  public String getURL(@NonNull String segment) {
    if (!segment.startsWith("http") && !segment.contains(API)) {
      return RestClient.getInstance().getBaseUrl() + API + segment;
    }
    return segment;
  }

  public String getStringPreference(String key) {
    return RestClient.getInstance().get(API + "preferences/" + key, String.class);
  }

  public boolean getBooleanPreference(String key) {
    return RestClient.getInstance().get(API + "preferences/" + key, Boolean.class);
  }

  public boolean setPreferences(Map<String, Object> values) {
    return RestClient.getInstance().put(API + "preferences", values);
  }

  public GameRepresentation getGame(int id) {
    return RestClient.getInstance().get(API + "games/" + id, GameRepresentation.class);
  }

  public GameRepresentation saveGame(GameRepresentation game) {
    return RestClient.getInstance().post(API + "games/save", game, GameRepresentation.class);
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

  public boolean uploadHighscoreBackgroundImage(File file) throws IOException {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

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
    map.add("file", contentsAsResource);
    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
    return RestClient.getInstance().exchange(API + "generator/upload", HttpMethod.POST, requestEntity, Boolean.class);
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
  public void changed(@NonNull String propertiesName, @NonNull String key, @Nullable String updatedValue) {
    Map<String, Object> model = new HashMap<>();
    model.put(key, updatedValue);
    Boolean result = RestClient.getInstance().put(API + "properties/" + propertiesName, model);
    ObservedProperties observedProperties = VPinStudioClient.observedProperties.get(propertiesName);
    observedProperties.notifyChange(key, updatedValue);
  }
}
