package de.mephisto.vpin.ui.apng.chunks;

/**
 * Holds a PLTE Palette
 */
public class ApngPalette
{
  public static final int MAX_SIZE = 256;

  int[] palette;

  /**
   * @param palette An array of ARGB values.
   */
  public ApngPalette(int[] palette) {
    this.palette = palette;
  }

  /**
   * Gets an ARGB value by index.
   */
  public int get(int nIdx) {
    return palette[nIdx];
  }

  /**
   * Gets the number of palette entries.
   */
  public int length() {
    return palette.length;
  }

  /**
   * Get the palette as a byte array
   */
  public byte[][] asBytes() {
    byte[][] abRGB = new byte[3][palette.length];
    int n = 0;
    for (int nARGB : palette) {
      abRGB[0][n] = (byte)((nARGB >> 16) & 0xff);
      abRGB[1][n] = (byte)((nARGB >> 8) & 0xff);
      abRGB[2][n] = (byte)(nARGB & 0xff);
      n++;
    }
    return abRGB;
  }

  /**
   * Creates a PgnPalette from a given RGB byte array.
   *
   * @param PLTE An RGB byte array.
   * @return A {@link ApngPalette} object.
   */
  public static ApngPalette createPalette(final byte[] PLTE) {
    int len = PLTE.length;
    if (len % 3 != 0) {
      throw new RuntimeException("Malformed palette");
    }

    final int nA = 0xff000000;
    final int nbColors = len / 3;
    final int[] palette = new int[nbColors];

    for (int destIdx = 0, srcIdx = 0; destIdx < nbColors; destIdx++) {
      final int nR = PLTE[srcIdx++] & 0xff;
      final int nG = PLTE[srcIdx++] & 0xff;
      final int nB = PLTE[srcIdx++] & 0xff;
      palette[destIdx] = nA | nR << 16 | nG << 8 | nB;
    }

    return new ApngPalette(palette);
  }

  /**
   * Apply a transparency chunk to the palette
   * @see <a href="https://www.w3.org/TR/PNG/#11PLTE">https://www.w3.org/TR/PNG/#11PLTE</a>
   * @see <a href="https://www.w3.org/TR/PNG/#11tRNS">https://www.w3.org/TR/PNG/#11tRNS</a>
   */
  void applyTransparency(byte[] TRNS) {
    int len = TRNS.length;

    if (len > length()) {
      throw new RuntimeException("Wrong Transparency Length " + length() + " < " + len);
    }

    // In case the length of tRNS is shorter than palette length, the palette is
    // already initialized fully opaque.
    for (int n = 0; n < len; n++) {
      palette[n] = (TRNS[n] & 0xff) << 24 | get(n) & 0x00ffffff;
    }
  }

  //------------------------------------------------------

  /**
   * Decode the TRNS chunk and returns the transparent color
   */
  static int fromTRNS(ApngColorType colorType, int bitDepth, byte[] tRNS) {
    switch (colorType) {
    case GREYSCALE:
      if (tRNS.length != 2) {
        throw new RuntimeException("Wrong Transparency Length, should be 2 but is " + tRNS.length);
      }

      switch (bitDepth) {
      case 8: return (tRNS[0] & 0xff);
      case 16: return (tRNS[0] & 0xff) << 8 | (tRNS[1] & 0xff);
      default: break;
      }
      break;

    case TRUECOLOR:
      if (tRNS.length != 6) {
         throw new RuntimeException("Wrong Transparency Length, should be 6 but is " + tRNS.length);
      }

      switch (bitDepth) {
      case 8: return (tRNS[0] & 0xff) << 16 | (tRNS[2] & 0xff) << 8 | (tRNS[4] & 0xff);
      case 16: return (tRNS[0] & 0xff) << 40 | (tRNS[1] & 0xff) << 32 | (tRNS[2] & 0xff) << 24
                      | (tRNS[3] & 0xff) << 16 | (tRNS[4] & 0xff) << 8 | (tRNS[5] & 0xff);
      default: break;
      }
      break;

    case INDEXED:
      break;

    case GREYSCALE_ALPHA:
    case TRUECOLOR_ALPHA:
        throw new RuntimeException("Wrong Transparency " + colorType);
    default:
      break;
    }
    return -1;
  }
}
