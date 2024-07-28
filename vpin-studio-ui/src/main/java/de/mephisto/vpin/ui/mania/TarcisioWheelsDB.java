package de.mephisto.vpin.ui.mania;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.mania.TarcisioWheels;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.ui.Studio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class TarcisioWheelsDB {
  private final static Logger LOG = LoggerFactory.getLogger(ScoringDB.class);

  private final static ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private static TarcisioWheels wheels = new TarcisioWheels();

  public static InputStream getWheelImage(String vpsTableId) {
    String wheelImage = getWheelIcon(vpsTableId);
    if (wheelImage == null) {
      return Studio.class.getResourceAsStream("avatar-blank.png");
    }

    return Studio.client.getPersistentCachedUrlImage("mania", "https://vpin-mania.net/wheels/" + wheelImage);
  }

  private static String getWheelIcon(String vpsTableId) {
    if (wheels.getData().isEmpty()) {
      try {
        InputStream in = TarcisioWheelsDB.class.getResourceAsStream("wheels.json");
        wheels = objectMapper.readValue(in, TarcisioWheels.class);
        LOG.info("Tarcisio wheel database loaded with " + wheels.getData().size() + " images.");
      }
      catch (Exception e) {
        LOG.error("Failed to read wheels database: " + e.getMessage(), e);
      }
    }

    if (wheels.getData().containsKey(vpsTableId)) {
      return wheels.getData().get(vpsTableId);
    }

    return null;
  }
}
