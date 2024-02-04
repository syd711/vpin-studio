package de.mephisto.vpin.ui.util;

import javafx.scene.Parent;
import javafx.scene.media.MediaView;

import java.util.ArrayList;
import java.util.List;

public class JFXHelper {
  public static List<MediaView> getMediaPlayers(Parent root) {
    List<MediaView> nodes = new ArrayList<>();
    addAllDescendents(root, nodes);
    return nodes;
  }

  private static void addAllDescendents(Parent parent, List<MediaView> nodes) {
    for (Object node : parent.getChildrenUnmodifiable()) {
      if(node instanceof MediaView) {
        nodes.add((MediaView) node);
      }
      if (node instanceof Parent) {
        addAllDescendents((Parent)node, nodes);
      }
    }
  }
}
