package de.mephisto.vpin.ui.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import java.util.function.Predicate;


public class VisibilityHoverListener implements ChangeListener<Boolean> {
  private final Node node;
  private final Predicate<Boolean> showPredicate;

  public VisibilityHoverListener(Node node, Predicate<Boolean> showPredicate) {
    this.node = node;
    this.showPredicate = showPredicate;
  }

  @Override
  public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    if(showPredicate.test(newValue)) {
      node.setVisible(newValue);
    }
    else {
      node.setVisible(false);
    }
  }
}
