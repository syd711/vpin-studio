package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.umd.cs.findbugs.annotations.NonNull;

public class GameRoom {

  private static ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @NonNull
  private final String url;

  GameRoom(@NonNull String url) {
    this.url = url;
  }

  void load() {

  }
}
