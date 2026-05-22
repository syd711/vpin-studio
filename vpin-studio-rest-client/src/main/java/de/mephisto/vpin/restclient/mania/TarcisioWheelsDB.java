package de.mephisto.vpin.restclient.mania;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class TarcisioWheelsDB {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static ObjectMapper objectMapper;
  private final static String WHEEL_DB = "https://www.vpin-mania.net/wheels/wheels.json";

  static {
    objectMapper = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
        .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
        .build();
  }

  private static TarcisioWheels wheels = new TarcisioWheels();

  public static InputStream getWheelImage(Class resourceClass, VPinStudioClient client, String vpsTableId) {
    String wheelImage = getWheelIcon(vpsTableId);
    if (wheelImage == null) {
      return resourceClass.getResourceAsStream("avatar-blank.png");
    }

    return client.getPersistentCachedUrlImage("mania", "https://vpin-mania.net/wheels/" + wheelImage);
  }

  private static void update() {
    try {
      LOG.info("Downloading {}", WHEEL_DB);
      URL url = URI.create(WHEEL_DB).toURL();
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
      LOG.info("Tarcisio wheel database loaded with {} images.", wheels.getData().size());
      tmp.delete();
    }
    catch (IOException e) {
      LOG.error("Wheel database download failed: {}", e.getMessage());
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
