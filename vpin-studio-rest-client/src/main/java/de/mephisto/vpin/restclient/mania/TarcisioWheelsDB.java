package de.mephisto.vpin.restclient.mania;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.system.ScoringDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;

public class TarcisioWheelsDB {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static ObjectMapper objectMapper;
  private final static String WHEEL_DB = "https://www.vpin-mania.net/wheels/wheels.json";

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private static TarcisioWheels wheels = new TarcisioWheels();

  public static InputStream getWheelImage(Class resourceClass, OverlayClient client, String vpsTableId) {
    String wheelImage = getWheelIcon(vpsTableId);
    if (wheelImage == null) {
      return resourceClass.getResourceAsStream("avatar-blank.png");
    }

    return client.getPersistentCachedUrlImage("mania", "https://vpin-mania.net/wheels/" + wheelImage);
  }

  private static void update() {
    try {
      LOG.info("Downloading " + WHEEL_DB);
      java.net.URL url = new URL(WHEEL_DB);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File tmp = File.createTempFile("wheels", ".json");
      tmp.deleteOnExit();

      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      wheels = objectMapper.readValue(in, TarcisioWheels.class);
      in.close();
      fileOutputStream.close();
      LOG.info("Written {}", tmp.getAbsolutePath());
      LOG.info("Tarcisio wheel database loaded with " + wheels.getData().size() + " images.");
      tmp.delete();
    }
    catch (IOException e) {
      LOG.error("Wheel database download failed: " + e.getMessage());
    }
  }

  private static String getWheelIcon(String vpsTableId) {
    if (wheels.getData().isEmpty()) {
      update();
    }

    if (wheels.getData().containsKey(vpsTableId)) {
      return wheels.getData().get(vpsTableId);
    }
    return null;
  }
}
