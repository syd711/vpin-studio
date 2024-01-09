package de.mephisto.vpin.restclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JsonSettings<T> {
  private final static Logger LOG = LoggerFactory.getLogger(JsonSettings.class);
  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static <T> T fromJson(Class<T> clazz, String json) throws Exception {
    try {
      T t = objectMapper.readValue(json, clazz);
      if (t != null) {
        return t;
      }
    } catch (Exception e) {
      LOG.warn("Error parsing settings json '" + json + "': " + e.getMessage());
    }
    return clazz.getDeclaredConstructor().newInstance();
  }

  public String toJson() throws JsonProcessingException {
    return objectMapper.writeValueAsString(this);
  }
}
