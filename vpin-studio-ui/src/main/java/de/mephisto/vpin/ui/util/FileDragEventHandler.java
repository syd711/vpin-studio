package de.mephisto.vpin.ui.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

public class FileDragEventHandler implements EventHandler<DragEvent> {

  private Node node;

  public FileDragEventHandler(Node node) {
    this.node = node;
  }

  @Override
  public void handle(DragEvent event) {
    if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    else {
      event.consume();
    }
  }
}
