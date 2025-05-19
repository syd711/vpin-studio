package de.mephisto.vpin.connectors.iscored;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class IScoredUtil {

  @Nullable
  public static String getQueryParams(String url, String key) {
    Map<String, String> params = new HashMap<>();
    try {
      String[] urlParts = url.split("\\?");
      if (urlParts.length > 1) {
        String query = urlParts[1];
        for (String param : query.split("&")) {
          String[] pair = param.split("=");
          String paramKey = URLDecoder.decode(pair[0], "UTF-8");
          String paramValue = null;
          if (pair.length > 1) {
            paramValue = URLDecoder.decode(pair[1], "UTF-8");
          }
          params.put(paramKey, paramValue);
        }
      }
    }
    catch (Exception ex) {
      //ignore
    }

    if (params.containsKey(key)) {
      return params.get(key);
    }
    return null;
  }

}
