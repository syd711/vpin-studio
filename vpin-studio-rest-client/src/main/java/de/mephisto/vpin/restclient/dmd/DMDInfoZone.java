package de.mephisto.vpin.restclient.dmd;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class DMDInfoZone {

  /** The screen where the DMD is displayed */  
  private VPinScreen onScreen;
  
  protected double x;
  protected double y;
  protected double width;
  protected double height;

  /** additional marging used to autoposition the dmd */
  private int margin;

  public DMDInfoZone() {
  }

  public DMDInfoZone(VPinScreen onScreen, double x, double y, double width, double height) {
    this.onScreen = onScreen;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public VPinScreen getOnScreen() {
    return onScreen;
  }

  public void setOnScreen(VPinScreen onScreen) {
		this.onScreen = onScreen;
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

  public int getMargin() {
    return margin;
  }

  public void setMargin(int margin) {
    this.margin = margin;
  }

  //-----------------------

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

  public boolean isOnPlayfield() {
    return onScreen != null && VPinScreen.PlayField.equals(onScreen);
  }

  public boolean isOnBackglass() {
    return onScreen != null && VPinScreen.BackGlass.equals(onScreen);
  }

  public boolean isOnFullDmd() {
    return onScreen != null && VPinScreen.Menu.equals(onScreen);
  }

  @Override
  public String toString() {
    return "[" + x + "/" + y + " - " + width + "x" + height + " @ " + onScreen + "]";
  }

}
