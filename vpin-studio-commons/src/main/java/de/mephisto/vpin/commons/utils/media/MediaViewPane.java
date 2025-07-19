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

  /** Whether Image or Video is rotated, When rotated, invert width and height role */
  private boolean rotated;

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

    if (child instanceof ImageView) {
      ((ImageView) child).setFitWidth(rotated ? height - marginX : width - marginX);
      ((ImageView) child).setFitHeight(rotated ? width - marginY : height - marginY);
      super.layoutInArea(child, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
    else if (child instanceof MediaView) {
      ((MediaView) child).setFitWidth(rotated ? height - marginX : width - marginX);
      ((MediaView) child).setFitHeight(rotated ? width - marginY : height - marginY);
      super.layoutInArea(child, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
    else {
      Bounds bounds = child.getLayoutBounds();
      child.relocate((width - bounds.getWidth()) / 2.0, (height  - bounds.getHeight()) / 2);
    }
  }

  public void setLoading() {
    setCenter(new ProgressIndicator());
  }

  protected boolean isRotated() {
    return rotated;
  }

  protected void setRotated(boolean rotated) {
    this.rotated = rotated;
  }


}