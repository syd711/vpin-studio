package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertiesStore {
  private final static Logger LOG = LoggerFactory.getLogger(PropertiesStore.class);

  protected final Properties properties = new SortedProperties();

  private File propertiesFile;

  public static PropertiesStore createInMemory() {
    return new PropertiesStore();
  }

  @NonNull
  public static PropertiesStore create(File file) {
    PropertiesStore store = new PropertiesStore();
    try {
      store.propertiesFile = file;
      if (store.propertiesFile.exists()) {
        FileInputStream fileInputStream = new FileInputStream(store.propertiesFile);
        store.properties.load(fileInputStream);
        fileInputStream.close();
      }
      else {
        LOG.error("No properties file found " + file.getAbsolutePath());
      }
    } catch (Exception e) {
      LOG.error("Failed to load data store: " + e.getMessage(), e);
    }
    return store;
  }

  @NonNull
  public static PropertiesStore create(String folder, String name) {
    if (!name.endsWith(".properties")) {
      name = name + ".properties";
    }
    File file = new File(folder, name);
    return create(file);
  }

  public List<Object> getEntries() {
    return new ArrayList<>(this.properties.values());
  }

  public void reload() {
    try {
      FileInputStream fileInputStream = new FileInputStream(propertiesFile);
      properties.load(fileInputStream);
      fileInputStream.close();
    } catch (IOException e) {
      LOG.error("Failed to reload " + propertiesFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  public boolean containsKey(String key) {
    return this.properties.containsKey(key);
  }

  public int getInt(String key, int defaultValue) {
    if (properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if (value.length() > 0) {
        return Integer.parseInt(value);
      }
    }
    else {
      set(key, defaultValue);
    }
    return defaultValue;
  }

  public boolean getBoolean(String key) {
    if (properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if (value.length() > 0) {
        return Boolean.parseBoolean(value);
      }
    }
    return false;
  }


  public int getInt(String key) {
    if (properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if (value.length() > 0) {
        return Integer.parseInt(value);
      }
    }
    return -1;
  }

  public float getFloat(String key) {
    if (properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if (value.length() > 0) {
        return Float.parseFloat(value);
      }
    }
    return -1f;
  }

  public String get(String key) {
    return properties.getProperty(key);
  }

  public String getString(String key) {
    return properties.getProperty(key);
  }

  public String getString(String key, String defaultValue) {
    if (properties.containsKey(key) && !StringUtils.isEmpty(properties.getProperty(key))) {
      return properties.getProperty(key);
    }
    else {
      set(key, defaultValue);
    }
    return defaultValue;
  }

  public void set(String key, int value) {
    this.set(key, String.valueOf(value));
  }

  public void set(Map<String, String> values) {
    properties.putAll(values);
    save();
  }

  public void set(String key, String value) {
    properties.setProperty(key, value);
    save();
  }

  private void save() {
    try {
      if (propertiesFile != null) {
        FileOutputStream fileOutputStream = new FileOutputStream(propertiesFile);
        properties.store(fileOutputStream, null);
        fileOutputStream.close();
      }
    } catch (Exception e) {
      LOG.error("Failed to store data store: " + e.getMessage(), e);
    }
  }

  public Properties getProperties() {
    return properties;
  }
}
