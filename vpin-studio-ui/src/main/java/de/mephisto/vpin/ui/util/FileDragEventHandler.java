package de.mephisto.vpin.ui.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileDragEventHandler implements EventHandler<DragEvent> {

  private final Node node;
  private final boolean singleSelectionOnly;
  private List<String> suffixes;

  public FileDragEventHandler(Node node) {
    this.node = node;
    this.singleSelectionOnly = false;
  }

  public FileDragEventHandler(Node node, boolean singleSelectionOnly, String... suffix) {
    this.node = node;
    this.singleSelectionOnly = singleSelectionOnly;
    this.suffixes = Arrays.asList(suffix);
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();
    if(files == null || files.isEmpty() || (files.size() > 1 && singleSelectionOnly)) {
      return;
    }

    for (File file : files) {
      String extension = FilenameUtils.getExtension(file.getName());
      if(!suffixes.contains(extension)) {
        return;
      }
    }

    if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    else {
      event.consume();
    }
  }
}
