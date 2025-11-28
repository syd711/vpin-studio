package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.iscored.models.GameModel;
import de.mephisto.vpin.connectors.iscored.models.GameRoomModel;
import de.mephisto.vpin.connectors.iscored.models.GameScoreModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IScored {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

  @Nullable
  public static GameRoom getGameRoom(@Nullable String url, boolean forceReload) {
    if (url == null) {
      return null;
    }

    if (!cache.containsKey(url) || forceReload) {
      GameRoom gameRoom = loadGameRoom(url);
      if (gameRoom != null) {
        cache.put(url, gameRoom);
      }
    }

    if (cache.containsKey(url)) {
      return cache.get(url);
    }

    return null;
  }

  private static GameRoom loadGameRoom(@NonNull String url) {
    try {
      long start = System.currentTimeMillis();

      // parse and align room URL 
      URL roomurl = new URL(url);
      String baseUrl = getBaseURL(roomurl);
      Map<String, Object> params = getBaseParams(roomurl);

      URL gameRoomURL = composeURL(baseUrl + "/roomCommands.php?c=getRoomInfo", params);
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
          URL gamesInfoURL = composeURL(baseUrl + "/api/" + params.get("user"), params);
          String gamesInfo = loadJson(gamesInfoURL);
          GameModel[] games = objectMapper.readValue(gamesInfo, GameModel[].class);

          URL allScoresUrl = composeURL(baseUrl + "/roomCommands.php?c=getAllGamesAndScores&roomID=" + gameRoomModel.getRoomID(), params);
          String allScoresJson = loadJson(allScoresUrl);
          GameScoreModel[] allScores = objectMapper.readValue(allScoresJson, GameScoreModel[].class);

          for (GameModel gameModel : games) {
            IScoredGame game = new IScoredGame();
            game.setGameRoomUrl(gameRoom.getUrl());
            game.setId(gameModel.getGameID());
            game.setName(gameModel.getGameName());
            game.setTags(gameModel.getTags());
            game.setHidden(gameModel.getHidden());
            game.setScores(getScoresFor(game.getId(), allScores));
            gameRoom.getGames().add(game);
          }

          Collections.sort(gameRoom.getGames(), new Comparator<IScoredGame>() {
            @Override
            public int compare(IScoredGame o1, IScoredGame o2) {
              return o1.getName().compareTo(o2.getName());
            }
          });


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

  private static Map<String, Object> getBaseParams(URL roomUrl) {
    Map<String, Object> params = splitQuery(roomUrl);
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

  private static URL composeURL(String baseUrl, Map<String, Object> params) throws MalformedURLException {
    String newUrl = baseUrl;
    for (Map.Entry<String, Object> entry : params.entrySet()) {
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

  public static Map<String, Object> splitQuery(URL url) {
    Map<String, Object> query_pairs = new LinkedHashMap<>();
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

      if (gameRoom.getSettings().isPublicScoreEnteringEnabled()) {
        postWithPublicUrl(result, gameRoom, game, name, highscore);
      }
      else if(gameRoom.getSettings().isApiWritingEnabled()) {
        postWithIScoredApi(result, gameRoom, game, name, highscore);
      }
      else {
        LOG.error("Failed to submit iScored highscore, neither public entering of write API is enabled.");
        result.setMessage("Failed to submit iScored highscore, neither public entering of write API is enabled.");
      }
    }
    catch (IOException e) {
      LOG.error("Failed to submit iScored highscore: " + e.getMessage(), e);
      result.setMessage("Failed to submit iScored highscore: " + e.getMessage());
    }
    return result;
  }

  private static void postWithIScoredApi(IScoredResult result, GameRoom gameRoom, IScoredGame game, String encodedPlayerName, long highscore) throws IOException {
    HttpURLConnection conn = null;
    try {

      String gameRoomName = URLEncoder.encode(gameRoom.getName(), StandardCharsets.UTF_8);
      String gameName = URLEncoder.encode(game.getName(), StandardCharsets.UTF_8);

      String baseUrl = "https://www.iscored.info/api/" + gameRoomName + "/" + gameName + "/submitScore";
      Map<String, Object> params = new HashMap<>();
      params.put("playerName", encodedPlayerName);
      params.put("score", highscore);

      URL url = composeURL(baseUrl, params);
      LOG.info("URL Post: {}", url);

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST"); // PUT is another valid option
      conn.setDoOutput(true);
      // Set headers (optional)
//      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//      conn.setRequestProperty("Charset", "UTF-8");
//
//      String paramString = "playerName="+ encodedPlayerName + "&score=" + highscore;
//      // Write parameters to the request body
//      try (OutputStream os = conn.getOutputStream()) {
//        os.write(paramString.getBytes("UTF-8"));
//        os.flush();
//      }


      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream())) {
        IOUtils.copy(in, out);
      }

      out.flush();
      out.close();

      LOG.info("Submitted new highscore with public endpoint to iScored game room \"" + gameRoom.getName() + "\": encodedPlayerName=" + encodedPlayerName + ", game=" + game.getId() + ", score=" + highscore);
      result.setMessage("Submitted new highscore with public endpoint to iScored game room \"" + gameRoom.getName() + "\": encodedPlayerName=" + encodedPlayerName + ", game=" + game.getId() + ", score=" + highscore);

      int responseCode = conn.getResponseCode();
      LOG.info("iScored returned: " + responseCode);
      result.setReturnCode(responseCode);
      result.setSent(true);
    }
    finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }


  private static void postWithPublicUrl(IScoredResult result, GameRoom gameRoom, IScoredGame game, String name, long highscore) throws IOException {
    HttpURLConnection conn = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      URL roomurl = new URL(gameRoom.getUrl());
      String baseUrl = getBaseURL(roomurl);
      Map<String, Object> params = getBaseParams(roomurl);

      URL url = composeURL(baseUrl + "/roomCommands.php?c=addScore&name=" + name + "&game=" + game.getId() + "&score=" + highscore + "&wins=undefined&losses=undefined&roomID=" + gameRoom.getRoomID(), params);

      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST"); // PUT is another valid option
      conn.setDoOutput(true);

      try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream())) {
        IOUtils.copy(in, out);
      }

      out.flush();
      out.close();

      LOG.info("Submitted new highscore with public endpoint to iScored game room \"" + gameRoom.getName() + "\": name=" + name + ", game=" + game.getId() + ", score=" + highscore);
      result.setMessage("Submitted new highscore with public endpoint to iScored game room \"" + gameRoom.getName() + "\": name=" + name + ", game=" + game.getId() + ", score=" + highscore);

      int responseCode = conn.getResponseCode();
      LOG.info("iScored returned: " + responseCode);
      result.setReturnCode(responseCode);
      result.setSent(true);
    }
    finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

}
