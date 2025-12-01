package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class StudioFolderChooser {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private DirectoryChooser folderChooser;

  public StudioFolderChooser() {
    try {
      folderChooser = new DirectoryChooser();
      File lastFolderSelection = LocalUISettings.getLastFolderSelection();
      if (lastFolderSelection != null && lastFolderSelection.exists() && !lastFolderSelection.isFile()) {
        folderChooser.setInitialDirectory(lastFolderSelection);
      }
    }
    catch (Exception e) {
      LOG.error("Error creating folder chooser: " + e.getMessage(), e);
    }
  }

  public void setTitle(String title) {
    this.folderChooser.setTitle(title);
  }

  public void setInitialDirectory(File folder) {
    folderChooser.setInitialDirectory(folder);
  }

  public File showOpenDialog(Stage stage) {
    try {
      File file = folderChooser.showDialog(stage);
      if (file != null) {
        LocalUISettings.saveLastFolderLocation(file);
      }

      return file;
    }
    catch (Exception e) {
      LOG.error("Error saving file location: " + e.getMessage(), e);
    }
    return null;
  }
}
