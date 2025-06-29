package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class StudioFileChooser {
  private final static Logger LOG = LoggerFactory.getLogger(StudioFileChooser.class);

  private FileChooser fileChooser;

  public StudioFileChooser() {
    try {
      fileChooser = new FileChooser();
      File lastFolderSelection = LocalUISettings.getLastFolderSelection();
      if (lastFolderSelection != null && lastFolderSelection.exists() && !lastFolderSelection.isFile()) {
        fileChooser.setInitialDirectory(lastFolderSelection);
      }
    } catch (Exception e) {
      LOG.error("Error creating file chooser: " + e.getMessage(), e);
    }
  }

  public void setTitle(String title) {
    this.fileChooser.setTitle(title);
  }

  public void setInitialFileName(String name) {
    this.fileChooser.setInitialFileName(name);
  }

  public ObservableList<FileChooser.ExtensionFilter> getExtensionFilters() {
    return this.fileChooser.getExtensionFilters();
  }

  public File showOpenDialog(Stage stage) {
    try {
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        LocalUISettings.saveLastFolderLocation(file);
      }

      return file;
    } catch (Exception e) {
      LOG.error("Error saving file location: " + e.getMessage(), e);
    }
    return null;
  }

  public List<File> showOpenMultipleDialog(Stage stage) {
    return fileChooser.showOpenMultipleDialog(stage);
  }
}
