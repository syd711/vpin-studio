package de.mephisto.vpin.ui.util;

import javafx.application.Platform;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Region;

public class LockedTableCell<T, S> extends TableCell<T, S> {

  {

    Platform.runLater(() -> {

      try {
        ScrollBar scrollBar = (ScrollBar) getTableView()
            .queryAccessibleAttribute(AccessibleAttribute.HORIZONTAL_SCROLLBAR);
        // set fx:id of TableColumn and get region of column header by #id
        Region headerNode = (Region) getTableView().lookup("#" + getTableColumn().getId());
        scrollBar.valueProperty().addListener((ob, o, n) -> {
          double doubleValue = n.doubleValue();

          // move header and cell with translateX & bring it front
          headerNode.setTranslateX(doubleValue);
          headerNode.toFront();

          this.setTranslateX(doubleValue);
          this.toFront();

        });
      }
      catch (Exception e) {
        e.printStackTrace();
      }

    });

  }

}