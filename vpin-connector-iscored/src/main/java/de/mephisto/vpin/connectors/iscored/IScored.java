package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.iscored.models.GameModel;
import de.mephisto.vpin.connectors.iscored.models.GameRoomModel;
import de.mephisto.vpin.connectors.iscored.models.GameScoreModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IScored {
  private final static Logger LOG = LoggerFactory.getLogger(IScored.class);

  private final static ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private final static Map<String, GameRoom> cache = new ConcurrentHashMap<>();

  public static boolean isIScoredGameRoomUrl(String dashboardUrl) {
    return dashboardUrl.toLowerCase().startsWith("https://www.iScored.info/".toLowerCase());
  }

  public static GameRoom getGameRoom(@NonNull String url, boolean forceReload) {
    if (!cache.containsKey(url) || forceReload) {
      GameRoom gameRoom = loadGameRoom(url);
      cache.put(url, gameRoom);
    }
    return cache.get(url);
  }

  private static GameRoom loadGameRoom(@NonNull String url) {
    try {
      long start = System.currentTimeMillis();

      // parse and align room URL 
      URL roomurl = new URL(url);
      String baseUrl = getBaseURL(roomurl);
      Map<String, String> params = getBaseParams(roomurl);

      URL gameRoomURL = composeURL(baseUrl, params, "/roomCommands.php?c=getRoomInfo");
      GameRoom gameRoom = new GameRoom();
      gameRoom.setUrl(url);

      String json = loadJson(gameRoomURL);
      if (json != null) {
        GameRoomModel gameRoomModel = objectMapper.readValue(json, GameRoomModel.class);

        gameRoom.setRoomID(gameRoomModel.getRoomID());
        gameRoom.setSettings(gameRoomModel.getSettings());
        gameRoom.setName(gameRoomModel.getSettings().getRoomName());

        if (gameRoom.getSettings().isApiReadingEnabled()) {
          LOG.info("READ API enabled, using API endpoint for game infos.");
          URL gamesInfoURL = composeURL(baseUrl, params, "/api/" + params.get("user"));
          String gamesInfo = loadJson(gamesInfoURL);
          GameModel[] games = objectMapper.readValue(gamesInfo, GameModel[].class);

          URL allScoresUrl = composeURL(baseUrl, params, "/roomCommands.php?c=getAllGamesAndScores&roomID=" + gameRoomModel.getRoomID());
          String allScoresJson = loadJson(allScoresUrl);
          GameScoreModel[] allScores = objectMapper.readValue(allScoresJson, GameScoreModel[].class);

          for (GameModel gameModel : games) {
            IScoredGame game = new IScoredGame();
            game.setId(gameModel.getGameID());
            game.setName(gameModel.getGameName());
            game.setTags(gameModel.getTags());
            game.setHidden(gameModel.getHidden());
            game.setScores(getScoresFor(game.getId(), allScores));
            gameRoom.getGames().add(game);
          }

          LOG.info("Loaded game room from URL '" + url + "', found " + gameRoom.getGames().size() + " games. (" + (System.currentTimeMillis() - start) + "ms)");
          return gameRoom;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load iScored Game Room: {}", e.getMessage());
    }

    return null;
  }

  private static List<Score> getScoresFor(int id, GameScoreModel[] allScores) {
    for (GameScoreModel allScore : allScores) {
      if (allScore.getGameID() == id) {
        if (allScore.getScores() != null) {
          return allScore.getScores();
        }
      }
    }
    return Collections.emptyList();
  }

  private static String getBaseURL(URL roomUrl) {
    String baseUrl = roomUrl.getProtocol() + "://" + roomUrl.getAuthority();
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }
    return baseUrl;
  }

  private static Map<String, String> getBaseParams(URL roomUrl) {
    Map<String, String> params = splitQuery(roomUrl);
    if (!params.containsKey("user")) {
      String userName = roomUrl.getPath();
      if (userName.startsWith("/")) {
        userName = userName.substring(1);
      }
      if (userName == null || userName.isEmpty()) {
        throw new UnsupportedOperationException("Invalid gameroom URL (no user provider) \"" + roomUrl + "\"");
      }
      params.put("user", userName);
    }
    return params;
  }

  private static URL composeURL(String baseUrl, Map<String, String> params, String path) throws MalformedURLException {
    String newUrl = baseUrl + path;
    for (Map.Entry<String, String> entry : params.entrySet()) {
      newUrl += (newUrl.contains("?") ? "&" : "?") + entry.getKey();
      if (entry.getValue() != null) {
        newUrl += "=" + entry.getValue();
      }
    }
    return new URL(newUrl);
  }

  private static String loadJson(URL url) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream())) {
        IOUtils.copy(in, out);
      }

      out.flush();
      out.close();
      conn.disconnect();

      LOG.info("iScored: " + url);
      return new String(out.toByteArray());
    }
    catch (Exception e) {
      LOG.error("Loading game room json failed: " + e.getMessage());
    }
    return null;
  }

  public static Map<String, String> splitQuery(URL url) {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String query = url.getQuery();
    if (query != null) {
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        int idx = pair.indexOf("=");
        if (idx < 0) {
          query_pairs.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), null);
        }
        else {
          query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
        }
      }
    }
    return query_pairs;
  }

  public static IScoredResult submitScore(GameRoom gameRoom, IScoredGame game, String playerName, String playerInitials, long highscore) {
    IScoredResult result = new IScoredResult();

    if (game.isGameLocked()) {
      LOG.info("The submission for this table is locked on iScored.");
      result.setMessage("The submission for this table is locked on iScored.");
      result.setReturnCode(200);
      return result;
    }

    List<Score> scores = game.getScores();
    if (scores != null) {
      for (Score score : scores) {
        if (score.getName() != null && (score.getName().equals(playerName) || score.getName().equals(playerInitials))) {
          try {
            if (game.isSingleScore()) {
              LOG.info("Found existing iScored score and skipped submission of new score value, because single score mode is enabled.");
              result.setMessage("Found existing iScored score and skipped submission of new score value, because single score mode is enabled.");
              result.setReturnCode(200);
              return result;
            }

            if (!game.isMultiScore()) {
              long l = score.getScore();
              if (l > highscore) {
                LOG.info("Found existing iScored score: " + score + " and skipped submission of new score value of " + highscore);
                result.setMessage("Found existing iScored score: " + score + " and skipped submission of new score value of " + highscore);
                result.setReturnCode(200);
                return result;
              }
            }
          }
          catch (NumberFormatException e) {
            LOG.error("Failed to parse score value \"" + score + "\" for iScored submission: " + e.getMessage());
            result.setMessage("Failed to parse score value \"" + score + "\" for iScored submission: " + e.getMessage());
            result.setReturnCode(500);
            return result;
          }
        }
      }
    }
    else {
      LOG.info("No existing scores found for game \"" + game.getName() + "\", skipped checks.");
    }

    LOG.info("Submitting iScored score \"" + playerName + "/" + playerInitials + " [" + highscore + "]\" to game \"" + game.getName() + "\" of game room \"" + gameRoom.getName() + "\"");
    HttpURLConnection conn = null;
    try {
      String name = playerName;
      try {
        if (!gameRoom.getSettings().isLongNameInputEnabled()) {
          name = playerInitials;
        }
      }
      catch (Exception e) {
        LOG.error("Error reading long names value from iScored: " + e.getMessage(), e);
        result.setMessage("Error reading long names value from iScored: " + e.getMessage());
        return result;
      }

      name = URLEncoder.encode(name, StandardCharsets.UTF_8);

      ByteArrayOutputStream out = new ByteArrayOutputStream();

      URL roomurl = new URL(gameRoom.getUrl());
      String baseUrl = getBaseURL(roomurl);
      Map<String, String> params = getBaseParams(roomurl);

      URL url = composeURL(baseUrl, params, "/roomCommands.php?c=addScore&name=" + name + "&game=" + game.getId() + "&score=" + highscore + "&wins=undefined&losses=undefined&roomID=" + gameRoom.getRoomID());

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST"); // PUT is another valid option
      conn.setDoOutput(true);

      try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream())) {
        IOUtils.copy(in, out);
      }

      out.flush();
      out.close();

      LOG.info("Submitted new highscore to iScored game room \"" + gameRoom.getName() + "\": name=" + name + ", game=" + game.getId() + ", score=" + highscore);
      result.setMessage("Submitted new highscore to iScored game room \"" + gameRoom.getName() + "\": name=" + name + ", game=" + game.getId() + ", score=" + highscore);
      try {
        int responseCode = conn.getResponseCode();
        LOG.info("iScored returned: " + responseCode);
        result.setReturnCode(responseCode);
        result.setSent(true);
      }
      catch (IOException e) {
        //ignore
      }
    }
    catch (IOException e) {
      LOG.error("Failed to submit iScored highscore: " + e.getMessage(), e);
      result.setMessage("Failed to submit iScored highscore: " + e.getMessage());
    }
    finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
    return result;
  }

}
