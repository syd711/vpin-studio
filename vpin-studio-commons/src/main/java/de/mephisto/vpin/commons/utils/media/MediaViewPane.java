package de.mephisto.vpin.commons.utils.media;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaView;

/**
 * A Pane to wrap an ImageView or a MediaView and force fitWidth and fitHeight from parent dimensions
 * Also manage rotated images or videos
 */
public class MediaViewPane extends Pane {

  private int marginX = 0;
  private int marginY = 0;

  /**
   * The centered child in the Pane, only one at a time
   */
  private Node child;

  /**
   * Set the child in the middle of the Pane
   */
  public void setCenter(Node n) {
    if (child != null) {
      getChildren().remove(child);
    }
    this.child = n;
    if (child != null) {
      getChildren().add(child);
    }
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();

    double width = getWidth();
    double height = getHeight();

    double fitWidth = false ? height - marginX : width - marginX;
    double fitHeight = false ? width - marginY : height - marginY;

    if (child instanceof ImageView) {
      ImageView imageView = ((ImageView) child);
      double f = imageView.getFitWidth();
      imageView.setFitWidth(fitWidth);
      imageView.setFitHeight(fitHeight);
      super.layoutInArea(child, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
    else if (child instanceof MediaView) {
      ((MediaView) child).setFitWidth(fitWidth);
      ((MediaView) child).setFitHeight(fitHeight);
      super.layoutInArea(child, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
    else {
      Bounds bounds = child.getLayoutBounds();
      child.relocate((width - bounds.getWidth()) / 2.0, (height - bounds.getHeight()) / 2);
    }
  }

  public void setLoading() {
    setCenter(new ProgressIndicator());
  }
}