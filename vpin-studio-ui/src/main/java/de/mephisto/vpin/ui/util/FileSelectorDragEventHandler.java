package de.mephisto.vpin.ui.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FileSelectorDragEventHandler implements EventHandler<DragEvent> {

  private final Node node;
  private boolean multiSelection;
  private List<String> suffixes;

  public FileSelectorDragEventHandler(Node node, String... suffix) {
    this(node, false, suffix);
  }

  public FileSelectorDragEventHandler(Node node, boolean multiSelection, String... suffix) {
    this.node = node;
    this.multiSelection = multiSelection;
    this.suffixes = Arrays.asList(suffix);
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();

    Set<DataFormat> contentTypes = event.getDragboard().getContentTypes();
    if (contentTypes.isEmpty()) {
      return;
    }

    //files may be empty for drag from a zip file
    if (!multiSelection && !files.isEmpty() && files.size() > 1) {
      return;
    }

    if (suffixes != null) {
      for (File file : files) {
        if (file.length() == 0) {
          continue;
        }

        String extension = FilenameUtils.getExtension(file.getName());
        if (!suffixes.contains(extension)) {
          return;
        }
      }
    }

    if (event.getGestureSource() != node) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    else {
      event.consume();
    }
  }
}
