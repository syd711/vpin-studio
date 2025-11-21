package de.mephisto.vpin.connectors.wovp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.wovp.models.Challenges;
import de.mephisto.vpin.connectors.wovp.models.Participant;
import de.mephisto.vpin.connectors.wovp.models.Participants;
import de.mephisto.vpin.connectors.wovp.models.Search;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Wovp {
  private final static Logger LOG = LoggerFactory.getLogger(Wovp.class);

  private final static ObjectMapper objectMapper;

  public static final String URL = "https://worldofvirtualpinball.com/api/whsc/v1/";

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private final String apiKey;

  public static Wovp create(@NonNull String apiKey) {
    return new Wovp(apiKey);
  }


  private Wovp(String apiKey) {
    this.apiKey = apiKey;
  }

  public Challenges getChallenges() throws Exception {
    Search search = new Search();
    String json = objectMapper.writeValueAsString(search);
    return doPost(json, Challenges.class);
  }

  public String validateKey() {
    if (apiKey != null) {
      HttpClient client = HttpClient.newBuilder().build();
      String json = "{\"apikey\": \"" + apiKey + "\"}";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(URL + "validate-apikey"))
          .header("Content-Type", "application/json")
          .header("X-Client-ID", "vpin-studio")
          .POST(HttpRequest.BodyPublishers.ofString(json))
          .build();

      try {
        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseJson = response.body();

        if (response.statusCode() != 200) {
          return responseJson;
        }

        return null;
      }
      catch (Exception e) {
        LOG.error("Failed to validate wovp API key: {}", e.getMessage(), e);
        return "Failed to validate wovp API key: " + e.getMessage();
      }
    }
    return "No API key set.";
  }

  private <T> T doPost(String json, Class<T> clazz) throws Exception {
    HttpClient client = HttpClient.newBuilder().build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(URL + "challenges/search"))
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
        throw new UnsupportedOperationException("WOVP return status code " + response.statusCode() + " [" + responseJson + "]");
      }

      return objectMapper.readValue(responseJson, clazz);
    }
    catch (Exception e) {
      LOG.error("Failed to send wovp request: {}", e.getMessage(), e);
      throw e;
    }
  }

}
