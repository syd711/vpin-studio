package de.mephisto.vpin.server.achievements.migration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Read-only access to the VPX Achievement Watcher's Firebase database for the one-time migration.
 * <p>
 * "Claim once, then read": the only write is the Watcher's ownership claim ({@code owners/<pid>} + {@code uids/<uid>}),
 * which the database rules only accept with the correct recovery key. It proves that the user owns the Player ID.
 * No achievement data is ever written to Firebase.
 */
public class FirebaseLegacyClient {
  private static final Logger LOG = LoggerFactory.getLogger(FirebaseLegacyClient.class);

  /**
   * Web API key and database of the Achievement Watcher's Firebase project (public values from the Watcher sources,
   * the key only names the project).
   */
  public static final String API_KEY = "AIzaSyAZ9SeiU90dSS0rHK1tETOkA07YvcqAMK8";
  public static final String DATABASE_URL = "https://vpx-achievements-watcher-lb-default-rtdb.europe-west1.firebasedatabase.app";
  private static final String AUTH_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=";

  public enum ClaimResult {OK, WRONG_KEY, OFFLINE}

  private final ObjectMapper mapper = JsonMapper.builder().build();
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  private final String databaseUrl;
  private final String apiKey;

  private String uid;
  private String idToken;

  public FirebaseLegacyClient() {
    this(DATABASE_URL, API_KEY);
  }

  public FirebaseLegacyClient(@NonNull String databaseUrl, @NonNull String apiKey) {
    this.databaseUrl = databaseUrl.replaceAll("/+$", "");
    this.apiKey = apiKey;
  }

  /**
   * Anonymous sign-in, like every Watcher installation does.
   */
  public boolean signIn() {
    try {
      HttpRequest request = HttpRequest.newBuilder(URI.create(AUTH_URL + apiKey))
          .timeout(Duration.ofSeconds(15))
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString("{\"returnSecureToken\":true}"))
          .build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        LOG.warn("Firebase sign-in failed: HTTP {}", response.statusCode());
        return false;
      }
      JsonNode json = mapper.readTree(response.body());
      uid = json.path("localId").asString(null);
      idToken = json.path("idToken").asString(null);
      return uid != null && idToken != null;
    }
    catch (Exception e) {
      LOG.warn("Firebase sign-in failed: {}", e.getMessage());
      return false;
    }
  }

  /**
   * The Watcher's {@code claim(pid, key)}: binds the Player ID to this sign-in. The rules accept it only with the
   * Player ID's recovery key (or for an ID that has never been claimed).
   */
  @NonNull
  public ClaimResult claim(@NonNull String pid, @NonNull String recoveryKey) {
    if (idToken == null && !signIn()) {
      return ClaimResult.OFFLINE;
    }
    try {
      ObjectNode body = mapper.createObjectNode();
      ObjectNode owner = body.putObject("owners/" + pid.toLowerCase());
      owner.put("uid", uid);
      owner.put("key", recoveryKey.trim());
      body.put("uids/" + uid, pid.toLowerCase());
      HttpRequest request = HttpRequest.newBuilder(URI.create(databaseUrl + "/.json?auth=" + enc(idToken)))
          .timeout(Duration.ofSeconds(15))
          .header("Content-Type", "application/json")
          .method("PATCH", HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
          .build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return ClaimResult.OK;
      }
      if (response.statusCode() == 401 || response.statusCode() == 403) {
        return ClaimResult.WRONG_KEY;
      }
      LOG.warn("Firebase claim failed: HTTP {}", response.statusCode());
      return ClaimResult.OFFLINE;
    }
    catch (Exception e) {
      LOG.warn("Firebase claim failed: {}", e.getMessage());
      return ClaimResult.OFFLINE;
    }
  }

  /**
   * Reads a node (player nodes are publicly readable).
   */
  @Nullable
  public JsonNode read(@NonNull String path) {
    try {
      String url = databaseUrl + "/" + path.replaceAll("^/+", "") + ".json" + (idToken != null ? "?auth=" + enc(idToken) : "");
      HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        LOG.warn("Firebase read of {} failed: HTTP {}", path, response.statusCode());
        return null;
      }
      JsonNode json = mapper.readTree(response.body());
      return json == null || json.isNull() ? null : json;
    }
    catch (Exception e) {
      LOG.warn("Firebase read of {} failed: {}", path, e.getMessage());
      return null;
    }
  }

  private static String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }
}
