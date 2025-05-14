package de.mephisto.vpin.ui.util;

import javafx.scene.Parent;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
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

  public static void setImageDisabled(ImageView imageview, boolean disabled) {
    if (disabled) {
      imageview.setEffect(new GaussianBlur());
      imageview.setOpacity(0.2);
    } else {
      imageview.setEffect(null);
      imageview.setOpacity(1.0);
    }
  }
}
