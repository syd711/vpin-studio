package de.mephisto.vpin.ui.util;

import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class StudioFileChooser {

  private final FileChooser fileChooser;

  public StudioFileChooser() {
    fileChooser = new FileChooser();
    File lastFolderSelection = LocalUISettings.getLastFolderSelection();
    if (lastFolderSelection != null && !lastFolderSelection.isFile()) {
      fileChooser.setInitialDirectory(lastFolderSelection);
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
    if (file != null) {
      LocalUISettings.saveLastFolderLocation(file);
    }
    return file;
  }

  public List<File> showOpenMultipleDialog(Stage stage) {
    return fileChooser.showOpenMultipleDialog(stage);
  }
}
