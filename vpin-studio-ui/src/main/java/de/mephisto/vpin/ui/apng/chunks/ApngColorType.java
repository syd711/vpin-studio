package de.mephisto.vpin.ui.apng.chunks;

import java.util.HashMap;
import java.util.Map;

public enum ApngColorType
{
  GREYSCALE(0, 1),        // Type 0, 1 component
  TRUECOLOR(2, 3),        // Type 2, 3 components
  INDEXED(3, 1),          // Type 3, 1 byte component
  GREYSCALE_ALPHA(4, 2),  // Type 4, 2 components
  TRUECOLOR_ALPHA(6, 4);  // Type 6, 4 components

  protected static final Map<Integer, ApngColorType> colorTypes = new HashMap<>();

  static {
    colorTypes.put(0, GREYSCALE);
    colorTypes.put(2, TRUECOLOR);
    colorTypes.put(3, INDEXED);
    colorTypes.put(4, GREYSCALE_ALPHA);
    colorTypes.put(6, TRUECOLOR_ALPHA);
  }

  protected final int colorType;
  protected final int componentsPerPixel;

	public static ApngColorType byType(int type) {
		ApngColorType colorType = colorTypes.get(type);
		if (colorType != null) {
			return colorType;
		}
		throw new RuntimeException("Unsupported Colortype " + type);
	}
  
  ApngColorType(int type, int componentsPerPixel) {
    this.colorType = type;
    this.componentsPerPixel = componentsPerPixel;
  }

  /**
   * Gets the number of components per pixel
   */
  public int getComponentsPerPixel() {
    return componentsPerPixel;
  }

  /**
   * has the color an alpha channel.
   */
  public boolean hasAlpha() {
    return (colorType & 0x04) > 0;
  }
}
