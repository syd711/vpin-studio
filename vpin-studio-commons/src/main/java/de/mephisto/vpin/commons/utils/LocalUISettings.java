package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LocalUISettings {
  private final static Logger LOG = LoggerFactory.getLogger(LocalUISettings.class);

  public static final String LAST_FOLDER_SELECTION = "lastFolderSelection";
  public static final String LAST_ISCORED_SELECTION = "iscoredUrl";

  private static PropertiesStore store;

  static {
    File propertiesFile = new File("config/settings.properties");
    propertiesFile.getParentFile().mkdirs();
    store = PropertiesStore.create(propertiesFile);
  }

  public static void saveProperty(@NonNull String key, @NonNull String value) {
    store.set(key, value);
  }

  @Nullable
  public static String getProperties(@NonNull String key) {
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
    if(y >= 0) {
      store.set("x", x);
      store.set("y", y);
      store.set("width", width);
      store.set("height", height);
      LOG.info("Saved window position to store.");
    }
  }

  public static void saveLocation(String id, int x, int y, int width, int height) {
    if(y >= 0) {
      store.set(id + "x", x);
      store.set(id + "y", y);
      store.set(id + ".x", x);
      store.set(id + ".y", y);
      store.set(id + ".width", width);
      store.set(id + ".height", height);
    }
    LOG.info("Saved window position to store for " + id);
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

}
