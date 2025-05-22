package de.mephisto.vpin.commons.utils.localsettings;

import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.commons.utils.Updater;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalUISettings {
  private final static Logger LOG = LoggerFactory.getLogger(LocalUISettings.class);

  public static final String LAST_FOLDER_SELECTION = "lastFolderSelection";
  public static final String DROP_IN_FOLDER = "dropInFolder";
  public static final String DROP_IN_FOLDER_ENABLED = "dropInFolderEnabled";

  private static PropertiesStore store;

  private static List<LocalSettingsChangeListener> listeners;

  private static Map<String, Object> jsonSettingsCache = new HashMap<>();
  private static File propertiesFile;

  static {
    initialize();
  }

  private static void initialize() {
    File basePath = Updater.getWriteableBaseFolder();
    propertiesFile = new File(basePath, "config/settings.properties");
    propertiesFile.getParentFile().mkdirs();
    store = PropertiesStore.create(propertiesFile);

    listeners = new ArrayList<>();
  }

  public static <T> T getTablePreference(Class<?> clazz) {
    try {
      String clazzName = clazz.getSimpleName();
      if (!jsonSettingsCache.containsKey(clazzName)) {
        BaseTableSettings baseTableSettings = LocalJsonSettings.load(clazz.getSimpleName(), BaseTableSettings.class);
        jsonSettingsCache.put(clazzName, baseTableSettings);
      }
      return (T) jsonSettingsCache.get(clazzName);
    }
    catch (Exception e) {
      LOG.error("Failed to read preferences: {}", e.getMessage(), e);
    }
    return null;
  }

  public static void addListener(LocalSettingsChangeListener listener) {
    listeners.add(listener);
  }

  public static void saveProperty(@NonNull String key, @Nullable String value) {
    store.set(key, value);
    for (LocalSettingsChangeListener listener : listeners) {
      listener.localSettingsChanged(key, value);
    }
  }

  @Nullable
  public static String getProperties(@NonNull String key) {
    if (store.containsKey(key)) {
      return store.get(key);
    }
    return null;
  }

  public static boolean getBoolean(@NonNull String key) {
    if (store.containsKey(key)) {
      String s = store.get(key);
      return Boolean.valueOf(s);
    }
    return false;
  }

  public static String getString(@NonNull String key) {
    if (store.containsKey(key)) {
      return store.get(key);
    }
    return null;
  }

  public static void saveLastFolderLocation(@Nullable File file) {
    if (file != null) {
      if (file.isFile()) {
        file = file.getParentFile();
      }
      store.set(LAST_FOLDER_SELECTION, file.getAbsolutePath());
    }
  }

  @Nullable
  public static File getLastFolderSelection() {
    if (store.containsKey(LAST_FOLDER_SELECTION)) {
      return new File(store.get(LAST_FOLDER_SELECTION));
    }
    return null;
  }

  public static void saveLocation(int x, int y, int width, int height) {
    if (y >= 0) {
      store.set("x", x);
      store.set("y", y);
      store.set("width", width);
      store.set("height", height);
      LOG.info("Saved window position to store.");
    }
  }

  public static void saveLocation(String id, int x, int y, int width, int height) {
    if (y >= 0 && id != null) {
      store.set(id + ".x", x);
      store.set(id + ".y", y);
      store.set(id + ".width", width);
      store.set(id + ".height", height);
      LOG.info("Saved window position to store for " + id);
    }
  }

  public static Rectangle getPosition() {
    Rectangle rectangle = new Rectangle();
    rectangle.setX(store.getInt("x", -1));
    rectangle.setY(store.getInt("y", -1));
    rectangle.setWidth(store.getInt("width", -1));
    rectangle.setHeight(store.getInt("height", -1));
    return rectangle;
  }

  public static Rectangle getPosition(String id) {
    if (store.containsKey(id + ".x")) {
      Rectangle rectangle = new Rectangle();
      rectangle.setX(store.getInt(id + ".x", -1));
      rectangle.setY(store.getInt(id + ".y", -1));
      rectangle.setWidth(store.getInt(id + ".width", -1));
      rectangle.setHeight(store.getInt(id + ".height", -1));
      return rectangle;
    }

    return null;
  }

  public static void setModal(String stateId, boolean modal) {
    store.set(stateId + "_modality", String.valueOf(modal));
  }

  public static boolean isModal(String stateId) {
    return store.getBoolean(stateId + "_modality");
  }

  public static boolean isMaximizeable(String stateId) {
    if (stateId == null) {
      return true;
    }

    return !stateId.equalsIgnoreCase("dialog-table-data");
  }

  public static void reset() {
    store.getProperties().clear();
    if (!propertiesFile.delete()) {
      LOG.error("Reset failed.");
    }
    else {
      LOG.info("Deleted {}", propertiesFile.getAbsolutePath());
    }
  }
}
