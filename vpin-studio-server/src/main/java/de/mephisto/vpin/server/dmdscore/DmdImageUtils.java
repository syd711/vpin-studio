package de.mephisto.vpin.server.dmdscore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmdImageUtils {

  private final static Logger LOG = LoggerFactory.getLogger(DmdImageUtils.class);

  public static byte[] toPlane(final byte[] planes, final int planeSize, final int width, final int height) {
    if (planesAreValid(planes, planeSize, width, height)) {
      return joinPlanes(planes, planeSize, width, height);
    }
    LOG.warn("Planes data was not valid, planeSize: {}, dim: {} x {}, planesLength: {}", planeSize, width, height, planes.length);
    return null;
  }
 /**
   * Sanity check that we have a valid set of planes data compared to expected values
   */
  private static boolean planesAreValid(final byte[] planes, final int planeSize, final int width, final int height) {
  return (width * height) % 8 == 0 &&
      planes.length % planeSize == 0 &&
      planes.length / planeSize == (width * height) / 8;
  }

  private static byte[] joinPlanes(final byte[] planes, final int planeSize, final int width, int height) {
    final byte[] plane = new byte[width * height];
    final int bytes = width * height / 8;

    // A bit plane is a byte array with the same dimensions as the original frame,
		// but since it's bits, a pixel can be either one or zero, so they are packed
		// into bytes.
		// This makes it more efficient to transfer than one byte per pixel, where only
		// 2 or 4 bits are used.

    // From my understanding....
    // The frame is made up of a width x height / 8 (number of bits in a byte) items across
    // planeSize number of planes.
    // To work out the colour of any one of the width x height pixels we need to grab the same
    // column value from each plane and then split them into bits to get a combined value that
    // represents the colour palette lookup value, most significant bit first.
    // For 2 planes we have a colour palette of 4, for 4 planes we have 16.
    for (int bytePos = 0; bytePos < bytes; bytePos++) {
        for (int bitPos = 7; bitPos >= 0; bitPos--) {
            for (int planePos = 0; planePos < planeSize; planePos++) {
                final int bit = theBit(planes[bytes * planePos + bytePos], bitPos);
                plane[bytePos * 8 + bitPos] |= (bit << planePos);
            }
        }
    }
    return plane;
  }

  private static int theBit(final byte b, final int pos) {
    return (b == 0 ? 0 : (b & (1 << pos)) != 0 ? 1 : 0);
  }

  //------------------------------------------

  public static int[] toRawImage(final byte[] plane, final int[] palette, final int width, final int height) {
    if (plane != null) {
      final int[] rawImage = new int[width * height];
      for (int y = 0; y < height; y++) {
        int yWidth = y * width;
        for (int x = 0; x < width; x++) {
          rawImage[y * width + x] = palette[plane[yWidth + x]];
        }
      }
      return rawImage;
    }
    return null;
  }

  public static int[] _toRawImageFromRgb24(final byte[] colours, final int width, final int height) {
    if (colours.length % 3 == 0) {
      final int[] rawImage = new int[width * height];
      for (int y = 0; y < height; y++) {
          int yWidth = y * width;
          for (int x = 0; x < width; x++) {
              final int index = (yWidth + x) * 3;
              // RGB24 is in BGR order
              final int b = (0xFF & colours[index]);
              final int g = (0xFF & colours[index + 1]);
              final int r = (0xFF & colours[index + 2]);
              rawImage[y * width + x] = rgb(r, g, b);
          }
      }
      return rawImage;
    } else {
      LOG.error("Planes length not a multiple of 3 in RGB24: %s", colours.length);
      return null;
    }
  }

  //----------------------------------

  public static int[] paletteFromColor(int color, int numberOfColours) {
    int[] palette = new int[numberOfColours];
    
    float[] hsl = RGBToHSL(red(color), green(color), blue(color));
    final float[] newHsl = new float[3];
    newHsl[0] = hsl[0];
    newHsl[1] = hsl[1];
    for (int i = 0; i < numberOfColours; i++) {
      newHsl[2] = hsl[2] * i / numberOfColours; // Lum value
      palette[i] = HSLToColor(newHsl);
    }
    return palette;
  }

  //----------------------------------

  /**
   * Convert RGB components to HSL (hue-saturation-lightness).
   */
  public static float[] RGBToHSL(int r, int g, int b) {
    final float rf = r / 255f;
    final float gf = g / 255f;
    final float bf = b / 255f;

    final float max = Math.max(rf, Math.max(gf, bf));
    final float min = Math.min(rf, Math.min(gf, bf));
    final float deltaMaxMin = max - min;

    float h, s;
    float l = (max + min) / 2f;

    if (max == min) {
        // Monochromatic
        h = s = 0f;
    } else {
        if (max == rf) {
            h = ((gf - bf) / deltaMaxMin) % 6f;
        } else if (max == gf) {
            h = ((bf - rf) / deltaMaxMin) + 2f;
        } else {
            h = ((rf - gf) / deltaMaxMin) + 4f;
        }

        s = deltaMaxMin / (1f - Math.abs(2f * l - 1f));
    }

    return new float[] { (h * 60f) % 360f, s, l };
  }

  /**
   * Convert HSL (hue-saturation-lightness) components to a RGB color.
   * <ul>
   * <li>hsl[0] is Hue [0 .. 360)</li>
   * <li>hsl[1] is Saturation [0...1]</li>
   * <li>hsl[2] is Lightness [0...1]</li>
   * </ul>
   * If hsv values are out of range, they are pinned.
   *
   * @param hsl 3 element array which holds the input HSL components.
   * @return the resulting RGB color
   */
  public static int HSLToColor(float[] hsl) {
    final float h = hsl[0];
    final float s = hsl[1];
    final float l = hsl[2];

    final float c = (1f - Math.abs(2 * l - 1f)) * s;
    final float m = l - 0.5f * c;
    final float x = c * (1f - Math.abs((h / 60f % 2f) - 1f));

    final int hueSegment = (int) h / 60;

    int r = 0, g = 0, b = 0;

    switch (hueSegment) {
      case 0:
        r = Math.round(255 * (c + m));
        g = Math.round(255 * (x + m));
        b = Math.round(255 * m);
        break;
      case 1:
        r = Math.round(255 * (x + m));
        g = Math.round(255 * (c + m));
        b = Math.round(255 * m);
        break;
      case 2:
        r = Math.round(255 * m);
        g = Math.round(255 * (c + m));
        b = Math.round(255 * (x + m));
        break;
      case 3:
        r = Math.round(255 * m);
        g = Math.round(255 * (x + m));
        b = Math.round(255 * (c + m));
        break;
      case 4:
        r = Math.round(255 * (x + m));
        g = Math.round(255 * m);
        b = Math.round(255 * (c + m));
        break;
      case 5:
      case 6:
        r = Math.round(255 * (c + m));
        g = Math.round(255 * m);
        b = Math.round(255 * (x + m));
        break;
    }

    r = Math.max(0, Math.min(255, r));
    g = Math.max(0, Math.min(255, g));
    b = Math.max(0, Math.min(255, b));

    return rgb(r, g, b);
  }

  /**
   * Return the red component of a color int. This is the same as saying
   * (color >> 16) & 0xFF
   */
  public static int red(int color) {
    return (color >> 16) & 0xFF;
  }
  /**
   * Return the green component of a color int. This is the same as saying
   * (color >> 8) & 0xFF
   */
  public static int green(int color) {
    return (color >> 8) & 0xFF;
  }
  /**
   * Return the blue component of a color int. This is the same as saying
   * color & 0xFF
   */
  public static int blue(int color) {
    return color & 0xFF;
  }

  public static int rgb(int red, int green, int blue) {
    return 0xff000000 | (red << 16) | (green << 8) | blue;
  }

  public static String colorToHex(int c) {
    return String.format("#%02x%02x%02x", red(c), green(c), blue(c));  
  }
}
