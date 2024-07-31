package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.iscored.models.GameModel;
import de.mephisto.vpin.connectors.iscored.models.GameRoomModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IScored {
  private final static Logger LOG = LoggerFactory.getLogger(IScored.class);

  private static ObjectMapper objectMapper;

  private final static String BASE_URL = "https://www.iscored.info";

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private static Map<String, GameRoom> cache = new HashMap<>();

  public static GameRoom getGameRoom(@NonNull String url) {
    if (!cache.containsKey(url)) {
      cache.put(url, loadGameRoom(url));
    }
    return cache.get(url);
  }

  public static void invalidate() {
    cache.clear();
  }

  public static GameRoom loadGameRoom(@NonNull String url) {
    try {
      long start = System.currentTimeMillis();
      if (!url.toLowerCase().startsWith(BASE_URL.toLowerCase())) {
        throw new UnsupportedOperationException("Invalid iscored.info URL \"" + url + "\"");
      }

      String userName = null;
      if (url.contains("&")) {
        Map<String, String> params = splitQuery(new URL(url));
        if (!params.containsKey("user")) {
          throw new UnsupportedOperationException("Invalid iscored.info URL \"" + url + "\"");
        }

        userName = params.get("user");
      }
      else {
        userName = url.substring(BASE_URL.length() + 1).trim();
      }

      String readUrl = BASE_URL + "/publicCommands.php?c=getRoomInfo&user=" + userName;
      URL gameRoomURL = new URL(readUrl);
      GameRoom gameRoom = new GameRoom();
      gameRoom.setUrl(url);

      String json = loadJson(gameRoomURL);
      if (json != null) {
        GameRoomModel gameRoomModel = objectMapper.readValue(json, GameRoomModel.class);

        gameRoom.setRoomID(gameRoomModel.getRoomID());
        gameRoom.setSettings(gameRoomModel.getSettings());
        gameRoom.setName(gameRoomModel.getSettings().getRoomName());

        URL gamesInfoURL = new URL(BASE_URL + "/publicCommands.php?c=getAllGames&roomID=" + gameRoomModel.getRoomID());
        String gamesInfo = loadJson(gamesInfoURL);
        GameModel[] games = objectMapper.readValue(gamesInfo, GameModel[].class);

        URL gameScoresURL = new URL(BASE_URL + "/publicCommands.php?c=getScores2&roomID=" + gameRoomModel.getRoomID());
        String scoresInfo = loadJson(gameScoresURL);
        Score[] scores = objectMapper.readValue(scoresInfo, Score[].class);

        for (GameModel gameModel : games) {
          IScoredGame game = new IScoredGame();
          game.setId(gameModel.getGameID());
          game.setName(gameModel.getGameName());
          game.setTags(gameModel.getTags());

          for (Score score : scores) {
            if (score.getGame().equals(String.valueOf(game.getId()))) {
              game.getScores().add(score);
            }
          }
          gameRoom.getGames().add(game);
        }

        LOG.info("Loaded game room for user '" + userName + "', found " + gameRoom.getGames().size() + " games. (" + (System.currentTimeMillis() - start) + "ms)");
        return gameRoom;
      }
    } catch (Exception e) {
      LOG.error("Failed to load iScored Game Room: " + e.getMessage());
    }

    return null;
  }

  private static String loadJson(URL url) {
    BufferedInputStream in = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      in = new BufferedInputStream(conn.getInputStream());
      IOUtils.copy(in, out);

      in.close();

      out.flush();
      out.close();
      conn.disconnect();

      return new String(out.toByteArray());
    } catch (Exception e) {
      LOG.error("Loading game room json failed: " + e.getMessage());
    }
    return null;
  }

  public static Map<String, String> splitQuery(URL url) {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String query = url.getQuery();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
    }
    return query_pairs;
  }

  public static boolean submitScore(GameRoom gameRoom, IScoredGame game, String playerName, String playerInitials, long highscore) {
    List<Score> scores = game.getScores();
    for (Score score : scores) {
      if (score.getName() != null && (score.getName().equals(playerName) || score.getName().equals(playerInitials))) {
        try {
          long l = Long.parseLong(score.getScore());
          if (l > highscore) {
            LOG.info("Found existing score: " + score + " and skipped submission of new score value of " + highscore);
            return false;
          }
        } catch (NumberFormatException e) {
          LOG.error("Failed to parse " + score);
          return false;
        }
      }
    }


    LOG.info("Submitting score \"" + playerName + "/" + playerInitials + " [" + highscore + "]\" to game \"" + game.getName() + "\" of game room \"" + gameRoom.getName() + "\"");
    BufferedInputStream in = null;
    try {
      String name = playerName;
      try {
        if (!gameRoom.getSettings().isLongNameInputEnabled()) {
          name = playerInitials;
        }
      }
      catch (Exception e) {
        LOG.error("Error reading long names value from iScored: " + e.getMessage(), e);
      }

      name = URLEncoder.encode(name, StandardCharsets.UTF_8);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      URL url = new URL(BASE_URL + "/publicCommands.php?c=addScore&name=" + name + "&game=" + game.getId() + "&score=" + highscore + "&wins=undefined&losses=undefined&roomID=" + gameRoom.getRoomID());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST"); // PUT is another valid option
      conn.setDoOutput(true);

      in = new BufferedInputStream(conn.getInputStream());
      IOUtils.copy(in, out);

      in.close();

      out.flush();
      out.close();
      conn.disconnect();

      LOG.info("Submitted new highscore to iscored game room \"" + gameRoom.getName() + "\": name=" + name + ", game=" + game.getId() + ", score=" + highscore);
      try {
        LOG.info("iScored returned: " + conn.getResponseCode());
      }
      catch (IOException e) {
        //ignore
      }
      return true;
    } catch (IOException e) {
      LOG.error("Failed to submit iscored highscore: " + e.getMessage(), e);
    }
    return false;
  }

}
