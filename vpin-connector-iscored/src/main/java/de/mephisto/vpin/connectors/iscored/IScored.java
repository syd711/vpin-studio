package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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

  public static GameRoom loadGameRoom(@NonNull String url) throws Exception {
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
      userName = url.substring(BASE_URL.length()+1).trim();
    }

    LOG.info("Loading game room for user '" + userName + "'");

    URL gameRoomInfoURL = new URL(BASE_URL + "/publicCommands.php?c=getRoomInfo&user=" + userName);

    String json = loadGameRoomInfoJson(gameRoomInfoURL);
    if (json != null) {
      return objectMapper.readValue(json, GameRoom.class);
    }
    return null;
  }

  private static String loadGameRoomInfoJson(URL url) {
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

  public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
    String query = url.getQuery();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
    }
    return query_pairs;
  }
}
