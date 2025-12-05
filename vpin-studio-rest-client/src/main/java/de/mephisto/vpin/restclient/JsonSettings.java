package de.mephisto.vpin.restclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public abstract class JsonSettings {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public final static ObjectMapper objectMapper = new ObjectMapper();

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
      LOG.warn("Error parsing settings json \"{}\" for class \"{}\": {}. Creating a plain new instance instead.", json, clazz, e.getMessage());
    }
    return clazz.getDeclaredConstructor().newInstance();
  }

  public String toJson() throws JsonProcessingException {
    return objectMapper.writeValueAsString(this);
  }

  public abstract String getSettingsName();
}
