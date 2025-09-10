package de.mephisto.vpin.commons.utils.media;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
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

  private double fitWidth = -1;
  private double fitHeight = -1;

  /**
   * The centered child in the Pane, only one at a time
   */
  private Node child;

  /**
   * Whether Image or Video is rotated, When rotated, invert width and height role
   */
  private boolean rotated;

  /**
   * Whether loading is deactivated:
   */
  private boolean noloading = false;

  protected MediaOptions mediaOptions;

  public void setMediaOptions(MediaOptions mediaOptions) {
    this.mediaOptions = mediaOptions;
  }

  /**
   * Set the child in the middle of the Pane
   */
  public void setCenter(Node n) {
    if (child != null) {
      getChildren().remove(child);
    }
    this.child = n;
    if (child != null) {
      getChildren().add(0, child);
    }
  }

  public void setMediaViewSize(double fitWidth, double fitHeight) {
    this.fitWidth = fitWidth;
    this.fitHeight = fitHeight;
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();

    double width = getWidth();
    double height = getHeight();

    double mediaViewWidth =  fitWidth != -1 ? fitWidth : width - marginX;
    double mediaViewHeight = fitHeight != -1 ? fitHeight : height - marginY;

    if (rotated) {
      double forSwitch = mediaViewWidth;
      mediaViewWidth = mediaViewHeight;
      mediaViewHeight = forSwitch;
    }

    if (child instanceof ImageView) {
      ImageView imageView = ((ImageView) child);
      imageView.setFitWidth(mediaViewWidth);
      imageView.setFitHeight(mediaViewHeight);
      super.layoutInArea(child, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
    else if (child instanceof MediaView) {
      MediaView mediaView = ((MediaView) child);
      mediaView.setFitWidth(mediaViewWidth);
      mediaView.setFitHeight(mediaViewHeight);
      super.layoutInArea(child, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
    else if (child != null) {
      child.resize(mediaViewWidth, mediaViewHeight);
      super.layoutInArea(child, 0, 0, width, height, 0, HPos.CENTER, VPos.CENTER);
    }
  }

  public void setNoLoading(boolean noloading) {
    this.noloading = noloading;
  }

  public void setLoading() {
    setCenter(noloading ? null : new ProgressIndicator());
  }

  protected boolean isRotated() {
    return rotated;
  }

  public void setRotated(boolean rotated) {
    this.rotated = rotated;
  }
}