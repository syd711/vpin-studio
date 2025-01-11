package de.mephisto.vpin.restclient.dmd;

import java.util.Objects;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class DMDInfo {
  private int gameId;
  private String gameRom;

  /** Aspect ratio from dmddevice.ini */ 
  private boolean forceAspectRatio;
  /** Selected aspect ratio */ 
  private DMDAspectRatio aspectRatio;
  /** whether save uses registry or ini */ 
  private boolean useRegistry;

  /** Whether dmd position is a per table one (true) or a global settings (false) */
  private boolean locallySaved;

  private double x;
  private double y;
  private double width;
  private double height;

  /** additional marging used to autoposition the dmd */
  private int margin;

  /** The screen where the DMD is displayed */  
  private VPinScreen onScreen;
  /** Width of the screen hosting the dmd */
  private double screenWidth;
  /** Height of the screen hosting the dmd */
  private double screenHeight;
  private boolean imageCentered;

  /** when dmd is positioned on grill, dmd size is 0x0, then option to position on dmd should be turned off */ 
  private boolean dmdScreenSet;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getGameRom() {
		return gameRom;
	}

	public void setGameRom(String gameRom) {
		this.gameRom = gameRom;
	}

	public boolean isForceAspectRatio() {
    return forceAspectRatio;
  }

  public void setForceAspectRatio(boolean forceAspectRatio) {
    this.forceAspectRatio = forceAspectRatio;
  }

  public DMDAspectRatio getAspectRatio() {
    return aspectRatio;
  }

  public void setAspectRatio(DMDAspectRatio aspectRatio) {
    this.aspectRatio = aspectRatio;
  }

  public boolean isUseRegistry() {
		return useRegistry;
	}

	public void setUseRegistry(boolean useRegistry) {
		this.useRegistry = useRegistry;
	}

  public boolean isLocallySaved() {
		return locallySaved;
	}

	public void setLocallySaved(boolean locallySaved) {
		this.locallySaved = locallySaved;
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

  public VPinScreen getOnScreen() {
    return onScreen;
  }

  public void setOnScreen(VPinScreen onScreen) {
		this.onScreen = onScreen;
	}

	public double getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(double screenWidth) {
		this.screenWidth = screenWidth;
	}

	public double getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(double screenHeight) {
		this.screenHeight = screenHeight;
	}

	public boolean isImageCentered() {
		return imageCentered;
	}

	public void setImageCentered(boolean imageCentered) {
		this.imageCentered = imageCentered;
	}

  public boolean isDmdScreenSet() {
    return dmdScreenSet;
  }

  public void setDmdScreenSet(boolean dmdScreenSet) {
    this.dmdScreenSet = dmdScreenSet;
  }

  //-----------------------

  public double getCenterX() {
    return x + width / 2;
  }
  public double getCenterY() {
    return y + height / 2;
  }

  public boolean isOnPlayfield() {
    return onScreen != null && VPinScreen.PlayField.equals(onScreen);
  }

  public boolean isOnBackglass() {
    return onScreen != null && VPinScreen.BackGlass.equals(onScreen);
  }

  public boolean isOnDMD() {
    return onScreen != null && VPinScreen.DMD.equals(onScreen);
  }

  public void centerOnScreen() {
    setX(getScreenWidth() / 2 - getWidth() / 2);
    setY(getScreenHeight() / 2 - getHeight() / 2);
  }


  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    DMDInfo that = (DMDInfo) object;
    return gameId == that.gameId && x == that.x && y == that.y && width == that.width && height == that.height && Objects.equals(onScreen, that.onScreen);
  }

  @Override
  public String toString() {
    return "[" + x + "/" + y + " - " + width + "x" + height + " @ " + onScreen + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, x, y, width, height);
  }

  public void adjustAspectRatio() {
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
