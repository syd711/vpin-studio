package de.mephisto.vpin.restclient.dmd;

public class DMDInfoZone {

  protected double x;
  protected double y;
  protected double width;
  protected double height;

  public DMDInfoZone() {
  }

  public DMDInfoZone(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public double getCenterX() {
    return x + width / 2;
  }
  public double getCenterY() {
    return y + height / 2;
  }

  public void adjustAspectRatio(DMDAspectRatio aspectRatio) {
    if (aspectRatio != null && aspectRatio.isKeepRatio()) {
      if (width / height > aspectRatio.getValue()) {
        // adjust width
        x += (width - aspectRatio.getValue() * height) / 2;
        width = aspectRatio.getValue() * height;
      }
      else {
        // adjust height
        y += (height - width / aspectRatio.getValue()) / 2;
        height = width / aspectRatio.getValue();
      }
    }
  }

}
