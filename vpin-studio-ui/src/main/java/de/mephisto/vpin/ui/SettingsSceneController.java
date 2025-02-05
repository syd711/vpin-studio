package de.mephisto.vpin.ui;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

abstract public class SettingsSceneController {

  protected static void switchNode(Node childNode) {
    Node lookup = Studio.stage.getScene().lookup("#root");
    BorderPane main = (BorderPane) lookup;
    StackPane stack = (StackPane) main.getCenter();
    if (childNode != null) {
      if (stack.getChildren().size() == 2) {
        stack.getChildren().remove(1);
      }
      stack.getChildren().add(childNode);
    }
    else {
      if (!stack.getChildren().isEmpty()) {
        stack.getChildren().remove(1);
      }
    }
  }
}
