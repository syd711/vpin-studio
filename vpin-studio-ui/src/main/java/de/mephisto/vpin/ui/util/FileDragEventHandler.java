package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.DnDOverlayController;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class FileDragEventHandler implements EventHandler<DragEvent> {

  private final Node node;
  private final boolean singleSelectionOnly;
  private List<String> suffixes;

  protected DnDOverlayController overlayController;
  
  private Predicate<DragEvent> dragFilter;

  public static FileDragEventHandler install(Pane loaderStack, Node node, boolean singleSelectionOnly, String... suffix) {
    FileDragEventHandler handler = new FileDragEventHandler(loaderStack, node, singleSelectionOnly, suffix);
    node.setOnDragOver(handler);
    return handler;
  }

  public FileDragEventHandler(Pane loaderStack, Node node, boolean singleSelectionOnly, String... suffix) {
    this.node = node;
    this.singleSelectionOnly = singleSelectionOnly;
    this.suffixes = Arrays.asList(suffix);

    this.overlayController = DnDOverlayController.load(loaderStack, node, singleSelectionOnly);
    overlayController.setMessage("Drop Media here.");
    overlayController.setMessageFontsize(14);
  }


  @Override
  public void handle(DragEvent event) {
    if (dragFilter != null && !dragFilter.test(event)) {
      return;
    }

    List<File> files = event.getDragboard().getFiles();

    Set<DataFormat> contentTypes = event.getDragboard().getContentTypes();
    if (contentTypes.isEmpty()) {
      return;
    }

    boolean containsMedia = !files.isEmpty();

    //files may be empty for drag from a zip file
    if (!files.isEmpty() && singleSelectionOnly && files.size() > 1) {
      return;
    }

    if (suffixes != null) {
      for (File file : files) {
        if (file.length() == 0) {
          continue;
        }

        String extension = FilenameUtils.getExtension(file.getName());
        if (!suffixes.contains(extension.toLowerCase())) {
          return;
        }
      }
    }

    if (event.getGestureSource() != node && containsMedia) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    else {
      event.consume();
    }
    overlayController.showOverlay();
  }

  public FileDragEventHandler setOnDragFilter(Predicate<DragEvent> dragFilter) {
    this.dragFilter  = dragFilter;
    return this;
  }

  public FileDragEventHandler setOnDragDropped(EventHandler<DragEvent> handler) {
    overlayController.setOnDragDropped(e -> {
      overlayController.hideOverlay();
      handler.handle(e);
    });
    return this;
  }

  public FileDragEventHandler setEmbeddedMode(boolean sidebarMode) {
    if (sidebarMode) {
      overlayController.setMessage(null);
    }
    return this;
  }
}