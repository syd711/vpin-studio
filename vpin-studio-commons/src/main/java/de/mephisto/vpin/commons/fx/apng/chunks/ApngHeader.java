package de.mephisto.vpin.commons.fx.apng.chunks;

/**
 * Map IHDR chunk
 * @see https://www.w3.org/TR/png/#11IHDR
 */
public class ApngHeader
{
  private final int width;
  private final int height;
  private final int colorType;
  private final int bitDepth;
  private final int compressionMethod;
  private final int filterMethod;
  private final int interlaceMethod;

  public ApngHeader(int width, int height, int bitDepth, int colorType, int compressionMethod, int filterMethod, int interlaceMethod) {
    this.width = width;
    this.height = height;
    this.bitDepth = bitDepth;
    this.colorType = colorType;
    this.compressionMethod = compressionMethod;
    this.filterMethod = filterMethod;
    this.interlaceMethod = interlaceMethod;
  }

  /**
   * Gets the width in pixels
   */
  public int getWidth() {
    return width;
  }

  /**
   * Gets the height in pixels
   */
  public int getHeight() {
    return height;
  }

  /**
   * Bit depth is a single-byte integer giving the number of bits per sample or per palette index (not per pixel). 
   * Valid values are 1, 2, 4, 8, and 16, although not all values are allowed for all color types.
   */
  public int getBitDepth() {
    return bitDepth;
  }

  /**
   * colorType used in the image
   */
  public int getColorType() {
    return colorType;
  }

  /**
   * Compression method is a single-byte integer that indicates the method used to compress the image data. 
   * Only compression method 0 (deflate compression with a sliding window of at most 32768 bytes) is supported
   */
  public int getCompressionMethod() {
    return compressionMethod;
  }

  /**
   * Filter method is a single-byte integer that indicates the preprocessing method applied to the image data before compression. 
   * Only filter method 0 (adaptive filtering with five basic filter types) is defined in this specification
   */
  public int getFilterMethod() {
    return filterMethod;
  }

  /**
   * Interlace method is a single-byte integer that indicates the transmission order of the image data. 
   * Two values are defined in this specification: 0 (no interlace) or 1 (Adam7 interlace)
   */
  public int getInterlaceMethod()  {
    return interlaceMethod;
  }
}
