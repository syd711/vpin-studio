package de.mephisto.vpin.ui.apng.chunks;

/**
 * @see <a href="https://www.w3.org/TR/PNG/">https://www.w3.org/TR/PNG</a>
 * @see <a href="https://wiki.mozilla.org/APNG_Specification">https://wiki.mozilla.org/APNG_Specification</a>
 *
 */
public class ApngChunkType {

  public static final int IHDR = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R'; // 1229472850
  public static final int PLTE = 'P' << 24 | 'L' << 16 | 'T' << 8 | 'E'; // 1347179589
  public static final int gAMA = 'g' << 24 | 'A' << 16 | 'M' << 8 | 'A'; // 1732332865
  public static final int bKGD = 'b' << 24 | 'K' << 16 | 'G' << 8 | 'D'; // 1649100612
  public static final int tRNS = 't' << 24 | 'R' << 16 | 'N' << 8 | 'S'; // 1951551059
  public static final int acTL = 'a' << 24 | 'c' << 16 | 'T' << 8 | 'L'; // 1633899596
  public static final int fcTL = 'f' << 24 | 'c' << 16 | 'T' << 8 | 'L'; // 1717785676
  public static final int IDAT = 'I' << 24 | 'D' << 16 | 'A' << 8 | 'T'; // 1229209940
  public static final int fdAT = 'f' << 24 | 'd' << 16 | 'A' << 8 | 'T'; // 1717846356
  public static final int cHRM = 'c' << 24 | 'H' << 16 | 'R' << 8 | 'M'; // 1665684045
  public static final int iCCP = 'i' << 24 | 'C' << 16 | 'C' << 8 | 'P'; // 1766015824
  public static final int sBIT = 's' << 24 | 'B' << 16 | 'I' << 8 | 'T'; // 1933723988
  public static final int sRGB = 's' << 24 | 'R' << 16 | 'G' << 8 | 'B'; // 1934772034
  public static final int hIST = 'h' << 24 | 'I' << 16 | 'S' << 8 | 'T'; // 1749635924
  public static final int pHYs = 'p' << 24 | 'H' << 16 | 'Y' << 8 | 's'; // 1883789683
  public static final int sPLT = 's' << 24 | 'P' << 16 | 'L' << 8 | 'T'; // 1934642260
  public static final int tIME = 't' << 24 | 'I' << 16 | 'M' << 8 | 'E'; // 1950960965
  public static final int iTXt = 'i' << 24 | 'T' << 16 | 'X' << 8 | 't'; // 1767135348
  public static final int tEXt = 't' << 24 | 'E' << 16 | 'X' << 8 | 't'; // 1950701684
  public static final int zTXt = 'z' << 24 | 'T' << 16 | 'X' << 8 | 't'; // 2052348020
  public static final int IEND = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D'; // 1229278788

}
