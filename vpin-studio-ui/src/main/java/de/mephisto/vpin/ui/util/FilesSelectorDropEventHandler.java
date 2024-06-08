package de.mephisto.vpin.ui.util;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FilesSelectorDropEventHandler implements EventHandler<DragEvent> {

  private final TextField textField;
  private final Consumer<List<File>> consumer;

  public FilesSelectorDropEventHandler(TextField textField, Consumer<List<File>> consumer) {
    this.textField = textField;
    this.consumer = consumer;
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();
    List<File> result = new ArrayList<>();
    for (File file : files) {
      if (file.isFile()) {
        result.add(file);
      }
    }
    textField.setText(String.join(", ", result.stream().map(f -> f.getName()).collect(Collectors.toList())));
    consumer.accept(result);
  }
}
