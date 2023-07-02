package de.mephisto.vpin.ui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;


public class VisibilityHoverListener implements ChangeListener<Boolean> {
  private Node node;

  public VisibilityHoverListener(Node node) {
    this.node = node;
  }

  @Override
  public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    node.setVisible(newValue);
  }
}
