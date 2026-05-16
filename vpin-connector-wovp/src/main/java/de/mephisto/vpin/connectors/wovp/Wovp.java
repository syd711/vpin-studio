package de.mephisto.vpin.connectors.wovp;

import de.mephisto.vpin.connectors.wovp.models.*;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Wovp {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static ObjectMapper objectMapper;

  public static final String URL = "https://worldofvirtualpinball.com/api/whsc/v1/";
  public static final String SCORE_PHOTO_URL = URL + "scores/submit-photo";
  public static final String SCORE_SUBMIT_URL = URL + "scores/submit";
  public static final String VALIDATION_URL = URL + "validate-apikey";
  public static final String CHALLENGES_URL = URL + "challenges/search";

  private static Map<String, WovpPlayer> players = new HashMap<>();

  static {
    objectMapper = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
        .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
        .build();
  }

  private final String apiKey;
  private static Challenges challenges;
  private static Date refreshDate = null;

  public static Wovp create(@NonNull String apiKey) {
    return new Wovp(apiKey);
  }


  private Wovp(String apiKey) {
    this.apiKey = apiKey;
  }

  public static List<WovpPlayer> getPlayers() {
    return new ArrayList<>(players.values());
  }

  public static WovpPlayer getPlayer(@NonNull String id) {
    return players.get(id);
  }

  public static void clearCache() {
    players.clear();
  }

  public Challenges getChallenges(boolean forceReload) throws Exception {
    if (challenges == null || forceReload || refreshDate == null || !DateUtils.isSameDay(new Date(), refreshDate)) {
      refreshDate = new Date();
      Search search = new Search();
      String json = objectMapper.writeValueAsString(search);
      challenges = doPost(json, Challenges.class, CHALLENGES_URL);
    }
    return challenges;
  }

  public void submitScore(@NonNull File screenshots, @NonNull String challengeId, long score, @NonNull ScoreSubmitMetadata scoreSubmitMetadata) throws Exception {
    long start = System.currentTimeMillis();
    UploadResponse uploadResponse = submitPhoto(screenshots);
    if (uploadResponse != null && uploadResponse.getData().getErrors().isEmpty()) {
      LOG.info("Resolved temporary photo id {}", uploadResponse.getData().getPhotoTempId());
      ScoreSubmit scoreSubmit = new ScoreSubmit();
      scoreSubmit.setMetadata(scoreSubmitMetadata);
      scoreSubmit.setScore(score);
      scoreSubmit.setPlayingPlatform(0);
      scoreSubmit.setChallengeId(challengeId);
      scoreSubmit.setPlayingPlatform(scoreSubmitMetadata.getPlatform());
      scoreSubmit.setPhotoTempId(uploadResponse.getData().getPhotoTempId());

      String json = objectMapper.writeValueAsString(scoreSubmit);
      doPost(json, UploadResponse.class, SCORE_SUBMIT_URL);
      long duration = System.currentTimeMillis() - start;
      LOG.info("WOVP score submission finished, submitted score of {}, took {}ms.", score, duration);
    }
  }

  private UploadResponse submitPhoto(File screenshot) throws Exception {
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httppost = new HttpPost(SCORE_PHOTO_URL);

      HttpEntity entity = MultipartEntityBuilder.create()
          .addBinaryBody("file", screenshot, ContentType.APPLICATION_OCTET_STREAM, screenshot.getName())
          .build();

      httppost.setEntity(entity);
      httppost.setHeader("X-Client-ID", "vpin-studio");
      httppost.setHeader("Authorization", "Bearer " + apiKey);

        return httpclient.execute(httppost, response -> {
            HttpEntity responseEntity = response.getEntity();
            String body = EntityUtils.toString(responseEntity, "UTF-8");

            if (response.getCode() != 200) {
                throw new UnsupportedOperationException("WOVP image upload failed with code " + response.getCode());
            }

            return objectMapper.readValue(body, UploadResponse.class);
        });
    }
    catch (Exception e) {
      LOG.error("Failed to post score image to wovp: {}", e.getMessage(), e);
      throw e;
    }
  }

  public ApiKeyValidationResponse validateKey() {
    ApiKeyValidationResponse r = new ApiKeyValidationResponse();
    if (apiKey != null) {
      HttpClient client = HttpClient.newBuilder().build();
      String json = "{\"apikey\": \"" + apiKey + "\"}";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(VALIDATION_URL))
          .header("Content-Type", "application/json")
          .header("X-Client-ID", "vpin-studio")
          .POST(HttpRequest.BodyPublishers.ofString(json))
          .build();

      try {
        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseJson = response.body();

        if (response.statusCode() != 200) {
          r.setSuccess(false);
          return r;
        }

        r = objectMapper.readValue(responseJson, ApiKeyValidationResponse.class);

        WovpPlayer player = new WovpPlayer();
        player.setId(r.getUserId());
        player.setName(r.getName());
        player.setApiKey(apiKey);
        players.put(player.getId(), player);

        LOG.info("Validated WOVP player \"{}\"", player.getName());
        return r;
      }
      catch (Exception e) {
        LOG.error("Failed to validate WOVP API key: {}", e.getMessage(), e);
      }
    }

    r.setSuccess(false);
    return r;
  }

  private <T> T doPost(String json, Class<T> clazz, String url) throws Exception {
    HttpClient client = HttpClient.newBuilder().build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .header("X-Client-ID", "vpin-studio")
        .header("Authorization", "Bearer " + apiKey)
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();

    try {
      // Send the request and get the response
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String responseJson = response.body();
      if (response.statusCode() != 200) {
        throw new UnsupportedOperationException("WOVP return status code " + response.statusCode() + " [" + responseJson + "/" + response.body() + "]");
      }

      return objectMapper.readValue(responseJson, clazz);
    }
    catch (Exception e) {
      LOG.error("Failed to send wovp request: {}", e.getMessage(), e);
      throw e;
    }
  }
}
