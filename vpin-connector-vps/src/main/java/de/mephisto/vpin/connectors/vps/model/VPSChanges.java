package de.mephisto.vpin.connectors.vps.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VPSChanges {
  private final static Logger LOG = LoggerFactory.getLogger(VPSChange.class);
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

  private List<VPSChange> changes = new ArrayList<>();

  public List<VPSChange> getChanges() {
    return changes;
  }

  public void setChanges(List<VPSChange> changes) {
    this.changes = changes;
  }

  public boolean isEmpty() {
    return changes.isEmpty();
  }

  @Override
  public String toString() {
    return getChanges().stream().map(VPSChange::toString).collect(Collectors.joining(","));
  }
}
