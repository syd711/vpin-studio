package de.mephisto.vpin.ui.apng.chunks;

import java.io.IOException;
import java.util.Arrays;

/**
 * Holds a PLTE Palette
 */
public class ApngPalette
{
  public static final int MAX_SIZE = 256;

  // Palette data : r,g,b,[a]  -  alpha optional
  private byte palette[][];

  /**
   * @param palette An array of ARGB values.
   */
  public ApngPalette(byte palette[][]) {
    this.palette = palette;
  }

  /**
   * Gets an ARGB value by index.
   */
  public int get(int nIdx) {
    final byte nA = (byte) 0xff;
    final byte nR = palette[0][nIdx];
    final byte nG = palette[1][nIdx];
    final byte nB = palette[2][nIdx];
    return nA << 24 | nR << 16 | nG << 8 | nB;
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
    return palette;
  }

  /**
   * Creates a PgnPalette from a given RGB byte array.
   *
   * @param PLTE An RGB byte array.
   * @return A {@link ApngPalette} object.
   */
  public static ApngPalette createPalette(final byte[] PLTE, int bitDepth) throws IOException {
    int len = PLTE.length;
    if (len % 3 != 0) {
      throw new IOException("Malformed palette");
    }

    int numEntries = len / 3;
    int paletteEntries = 1 << bitDepth;
    if (numEntries > paletteEntries) {
        //emitWarning("PLTE chunk contains too many entries for bit depth, ignoring extras.");
        numEntries = paletteEntries;
    }

    byte palette[][] = new byte[3][paletteEntries];
    for (int i = 0, idx = 0; i != numEntries; ++i) {
        for (int k = 0; k != 3; ++k) {
            palette[k][i] = PLTE[idx++];
        }
    }
    return new ApngPalette(palette);
  }

  //------------------------------------------------------

  /**
   * Apply a transparency chunk to the palette
   * @see <a href="https://www.w3.org/TR/PNG/#11PLTE">https://www.w3.org/TR/PNG/#11PLTE</a>
   * @see <a href="https://www.w3.org/TR/PNG/#11tRNS">https://www.w3.org/TR/PNG/#11tRNS</a>
   */
  void applyTransparency(byte[] TRNS) {
    int length = TRNS.length;

    byte newPal[][] = new byte[4][];
    System.arraycopy(palette, 0, newPal, 0, 3);

    int paletteLength = palette[0].length;
    newPal[3] = new byte[paletteLength];
    System.arraycopy(TRNS, 0, newPal[3], 0, Math.min(length, paletteLength));

    if (length < paletteLength) {
      Arrays.fill(newPal[3], length, paletteLength, (byte) 0xFF);
    }
    palette = newPal;
  }
}
