package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.List;

public class FxUtil {

  public static void setBusy(boolean busy) {
    Platform.runLater(() -> {
      if (busy) {
        Studio.stage.getScene().setCursor(Cursor.WAIT);
      }
      else {
        Studio.stage.getScene().setCursor(Cursor.DEFAULT);
      }
    });
  }

  public static Node getChild(Pane node, List<Integer> positions) {
    Pane parent = node;
    for (Integer pos : positions) {
      parent = (Pane) parent.getChildren().get(pos);
    }
    return parent;
  }
}
