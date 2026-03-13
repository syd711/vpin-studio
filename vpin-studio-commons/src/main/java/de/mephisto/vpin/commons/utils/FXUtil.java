package de.mephisto.vpin.commons.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;

public class FXUtil {
  public static <T> T findParentById(Parent parent, String id) {
    if (parent == null) {
      return null;
    }

    if (parent.getId() != null && parent.getId().equalsIgnoreCase(id)) {
      return (T) parent;
    }
    return findParentById(parent.getParent(), id);
  }

  public static <T> T findChildByID(Parent parent, String id) {

    String nodeId;/*from w  w  w.j av a 2s.c om*/

    if (parent instanceof TitledPane) {
      TitledPane titledPane = (TitledPane) parent;
      Node content = titledPane.getContent();
      nodeId = content.idProperty().get();

      if (nodeId != null && nodeId.equals(id)) {
        return (T) content;
      }

      if (content instanceof Parent) {
        T child = findChildByID((Parent) content, id);
        if (child != null) {
          return child;
        }
      }
    }

    for (Node node : parent.getChildrenUnmodifiable()) {
      nodeId = node.idProperty().get();
      if (nodeId != null && nodeId.equals(id)) {
        return (T) node;
      }

      if (node instanceof SplitPane) {
        SplitPane splitPane = (SplitPane) node;
        for (Node itemNode : splitPane.getItems()) {
          nodeId = itemNode.idProperty().get();

          if (nodeId != null && id.equals(id)) {
            return (T) itemNode;
          }

          if (itemNode instanceof Parent) {
            T child = findChildByID((Parent) itemNode, id);

            if (child != null) {
              return child;
            }
          }
        }
      }
      else if (node instanceof Accordion) {
        Accordion accordion = (Accordion) node;
        for (TitledPane titledPane : accordion.getPanes()) {
          nodeId = titledPane.idProperty().get();

          if (nodeId != null && nodeId.equals(id)) {
            return (T) titledPane;
          }

          T child = findChildByID(titledPane, id);

          if (child != null) {
            return child;
          }
        }
      }
      else if (node instanceof Parent) {
        T child = findChildByID((Parent) node, id);

        if (child != null) {
          return child;
        }
      }
    }
    return null;
  }
}
