package de.mephisto.vpin.ui.util;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class FileSelectorDropEventHandler implements EventHandler<DragEvent> {

  private TextField textField;
  private final Consumer<File> consumer;

  public FileSelectorDropEventHandler(TextField textField, Consumer<File> consumer) {
    this.textField = textField;
    this.consumer = consumer;
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();
    for (File file : files) {
      if (file.isFile()) {
        textField.setText(file.getAbsolutePath());
        consumer.accept(file);
        return;
      }
    }
  }
}
