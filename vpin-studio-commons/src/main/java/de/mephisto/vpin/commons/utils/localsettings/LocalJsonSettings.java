package de.mephisto.vpin.commons.utils.localsettings;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.utils.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

abstract public class LocalJsonSettings {
  private final static Logger LOG = LoggerFactory.getLogger(LocalJsonSettings.class);

  public final static ObjectMapper objectMapper = new ObjectMapper();

  File settingsFile;

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  public static <T extends LocalJsonSettings> T load(String configName, Class configClazz) {
    FileInputStream in = null;
    try {
      File dbFile = getConfigFile(configName);
      T t = null;
      if (dbFile.exists()) {
        in = new FileInputStream(dbFile);
        t = (T) objectMapper.readValue(in, configClazz);
      }
      else {
        t = (T) configClazz.getDeclaredConstructor().newInstance();
      }

      t.settingsFile = dbFile;
      return t;
    }
    catch (Exception e) {
      LOG.error("Failed to json: " + e.getMessage());
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
      }
      catch (IOException e) {
        //ignore
      }
    }
    return null;
  }

  private static File getConfigFile(String configName) {
    String settingsFileName = configName + ".json";
    File basePath = Updater.getWriteableBaseFolder();
    File parent = new File(basePath, "config/");
    if (!parent.exists()) {
      parent.mkdirs();
    }

    return new File(parent, settingsFileName);
  }

  public void save() {
    try {
      objectMapper.writeValue(settingsFile, this);
    }
    catch (IOException e) {
      LOG.error("Failed to write {}: {}", settingsFile.getName(), e.getMessage(), e);
    }
  }
}
