package de.mephisto.vpin.ui.util;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;

public class FxUtil {

  public static Node getChild(Pane node, List<Integer> positions) {
    Pane parent = node;
    for (Integer pos : positions) {
      parent = (Pane) parent.getChildren().get(pos);
    }
    return parent;
  }
}
