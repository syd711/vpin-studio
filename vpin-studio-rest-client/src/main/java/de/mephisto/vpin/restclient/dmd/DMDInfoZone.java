package de.mephisto.vpin.restclient.dmd;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class DMDInfoZone {

  /** The screen where the DMD is displayed */  
  private VPinScreen onScreen;
  
  protected int x;
  protected int y;
  protected int width;
  protected int height;

  /** additional marging used to autoposition the dmd */
  private int margin;

  public DMDInfoZone() {
  }

  public DMDInfoZone(VPinScreen onScreen, int x, int y, int width, int height) {
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

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getMargin() {
    return margin;
  }

  public void setMargin(int margin) {
    this.margin = margin;
  }

  //-----------------------

  public int getCenterX() {
    return x + width / 2;
  }
  public int getCenterY() {
    return y + height / 2;
  }


  public void adjustAspectRatio(DMDAspectRatio aspectRatio) {
    if (aspectRatio != null && aspectRatio.isKeepRatio()) {
      if (width > height * aspectRatio.getValue()) {
        // adjust width
        x += (width - aspectRatio.getValue() * height) / 2;
        width = (int) (aspectRatio.getValue() * height);
      }
      else {
        // adjust height
        y += (height - width / aspectRatio.getValue()) / 2;
        height = (int) (width / aspectRatio.getValue());
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
