package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.GameMedia;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class VPinStudioClient implements ObservedPropertyChangeListener {
  private final static String API = "api/v1/";

  private Map<String,ObservedProperties> observedProperties = new HashMap<>();

  public byte[] getDirectB2SImage(int gameId) {
//    http://localhost:8089/assets/directb2s/7/cropped/RATIO_4x3
    //http://localhost:8089/games/7
//    http://localhost:8089/generator/overlay
    return RestClient.getInstance().readBinary(API + "assets/directb2s/" + gameId + "/raw");
  }

  public GameRepresentation getGame(int id) {
    return RestClient.getInstance().get(API + "games/" + id, GameRepresentation.class);
  }

  public List<GameRepresentation> getGames() {
    return Arrays.asList(RestClient.getInstance().get(API + "games", GameRepresentation[].class));
  }

  public byte[] getGameMedia(GameRepresentation game, GameMedia gameMedia) {
    String url = API + "games/" + game.getId() + "/media/" + gameMedia.name();
    return RestClient.getInstance().readBinary(url);
  }

  public InputStream getHighscoreCard(int id) {
    try {
      URL url = new URL("/generator/overlay");
      HttpURLConnection con =(HttpURLConnection)url.openConnection();
      return con.getInputStream();
    } catch (IOException e) {

    }
    return null;
  }

  public ObservedProperties getProperties(String propertiesName) {
    if(!observedProperties.containsKey(propertiesName)) {
      Map<String,Object> result = RestClient.getInstance().get(API + "properties/" + propertiesName, Map.class);
      Properties properties = new Properties();
      properties.putAll(result);
      ObservedProperties observedProperties = new ObservedProperties(propertiesName, properties);
      observedProperties.addObservedPropertyChangeListener(this);
      this.observedProperties.put(propertiesName, observedProperties);
    }

    return this.observedProperties.get(propertiesName);
  }

  public void setBundleProperty(String bundle, String key, String value) {
    Map<String,String> model = new HashMap<>();
    model.put(key, value);
    RestClient.getInstance().put(API + "properties/" + bundle, model);
  }

  @Override
  public void changed(@NonNull String propertiesName, @NonNull String key, @Nullable String updatedValue) {
    setBundleProperty(propertiesName, key, updatedValue);
  }
}
