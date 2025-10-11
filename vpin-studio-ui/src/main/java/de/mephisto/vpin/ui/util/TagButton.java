package de.mephisto.vpin.ui.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class TagButton extends HBox {

  public TagButton(@NonNull String tag, @NonNull String color) {
    setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12; -fx-padding: 2 8 2 8;-fx-cursor: pointer;"); // not hand yet
    setSpacing(3);

    Label tagText = new Label(tag);
    tagText.getStyleClass().add("default-text");
    getChildren().addAll(tagText);

//    this.setOnMouseClicked(new EventHandler<MouseEvent>() {
//      @Override
//      public void handle(MouseEvent event) {
//        System.out.println(tag);
//      }
//    });
  }
}
