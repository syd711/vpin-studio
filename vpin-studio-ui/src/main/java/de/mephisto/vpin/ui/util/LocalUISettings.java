package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.PropertiesStore;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LocalUISettings {
  private final static Logger LOG = LoggerFactory.getLogger(LocalUISettings.class);

  public static final String LAST_FOLDER_SELECTION = "lastFolderSelection";

  private static PropertiesStore store;

  static {
    File propertiesFile = new File("config/settings.properties");
    propertiesFile.getParentFile().mkdirs();
    store = PropertiesStore.create(propertiesFile);
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
    store.set("x", x);
    store.set("y", y);
    store.set("width", width);
    store.set("height", height);
    LOG.info("Saved window position to store.");
  }

  public static Rectangle getPosition() {
    Rectangle rectangle = new Rectangle();
    rectangle.setX(store.getInt("x", -1));
    rectangle.setY(store.getInt("y", -1));
    rectangle.setWidth(store.getInt("width", -1));
    rectangle.setHeight(store.getInt("height", -1));
    return rectangle;
  }

}
