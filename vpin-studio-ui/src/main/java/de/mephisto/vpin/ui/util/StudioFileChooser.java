package de.mephisto.vpin.ui.util;

import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class StudioFileChooser {
  private final static Logger LOG = LoggerFactory.getLogger(StudioFileChooser.class);

  private final FileChooser fileChooser;

  public StudioFileChooser() {
    fileChooser = new FileChooser();
    try {
      File lastFolderSelection = LocalUISettings.getLastFolderSelection();
      if (lastFolderSelection != null && !lastFolderSelection.isFile()) {
        fileChooser.setInitialDirectory(lastFolderSelection);
      }
    } catch (Exception e) {
      LOG.error("Error creating file chooser: " + e.getMessage(), e);
    }
  }

  public void setTitle(String title) {
    this.fileChooser.setTitle(title);
  }

  public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
    return this.fileChooser.getExtensionFilters();
  }

  public File showOpenDialog(Stage stage) {
    File file = fileChooser.showOpenDialog(stage);
    try {
      if (file != null) {
        LocalUISettings.saveLastFolderLocation(file);
      }
    } catch (Exception e) {
      LOG.error("Error saving file location: " + e.getMessage(), e);
    }
    return file;
  }

  public List<File> showOpenMultipleDialog(Stage stage) {
    return fileChooser.showOpenMultipleDialog(stage);
  }
}
