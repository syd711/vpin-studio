package de.mephisto.vpin.ui.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;

/**
 * A DataInputStream that parses an APNG image in chunks and give access to frames and associated information
 */
public class ApngChunkDataInputStream extends DataInputStream {

  public static final byte[] APNG_SIGNATURE = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

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

  
  private ApngHeader header;
  private ApngPalette palette;

	private boolean tRNS_present = false;
  private int[] transparentColor;

  //private int gamma;

  /** The current FrameControl that goes along side the nextFrame() */
  private ApngFrameControl frameControl;

  /** Number of animated frames contained in the image, -1 means not set as no ACTL chunk present */
  private int availableFrames = 0;
  /* how many loop to play, 0 means repeat infinitely */
  private int numLoops = 0;

  // For integrity checks
  private CRC32 crc = new CRC32();
  int idatCount = 0;
  int frameSequenceExpected = 0;

  // Deflated IDAT or fdAT chunks
  final ByteArrayOutputStream frameData = new ByteArrayOutputStream();

  /**
   * Constructor for this PngChunkInputStream.
   */
  public ApngChunkDataInputStream(InputStream is) throws IOException {
    super(is);
    // read and check signature
    readSignature();
    readNextChunk();
    // process the header
    processChunk();
    readNextChunk();
    if (header == null) {
      throw new IOException("Cannot get Header");
    }
  }

  //-------------------------------------- GETTERS ---

  public ApngHeader getHeader() {
    return header;
  }

  public boolean isInterlaced() {
    return header.getInterlaceMethod() != 0;
  }

  public int getColorType() {
    return header.getColorType();
  }

  public int getBitDepth() {
    return header.getBitDepth();
  }

  public boolean hasTransparency() {
    return tRNS_present;
  }

  public int[] getTransparentColor() {
    return transparentColor;
  }

  public int getAvailableFrames() {
    return availableFrames;
  }

  public int getAnimationLoops() {
    return numLoops;
  }

  public ApngPalette getPalette() {
    return palette;
  }

  public ApngFrameControl getLastFrameControl() {
    return frameControl;
  }

  //--------------------------------------

  public byte[] nextDat() throws IOException {
    // comes again after end has been reached
    if (chunkType == IEND) {
      return null;
    }

    do {
      processChunk();
      readNextChunk();
    }
    while (chunkType != fcTL && chunkType != IEND);

    if (frameData.size() > 0) {
      byte[] dat = frameData.toByteArray();
      frameData.reset();
      return dat;
    }
    else if (chunkType != IEND) {
      // case where first FCTL is before IDAT, then we just consumed previous chunks; then go to next FCTL
      return nextDat();
    }
    return null;
  }

  //-------------------------------------- CHUNKS ITERATION ---
  int chunkType = -1;
  int remainingLength;

  private void readNextChunk() throws IOException {
    this.remainingLength = readInt();
    if (remainingLength < 0) {
      throw new EOFException("Unexpected end of file encountered in readNextChunk()");
    }

    this.chunkType = readInt();
    if (header == null && chunkType != IHDR) {
      throw new IOException("Chunk found before Header");
    }
  }

  /**
   * Chunk wedge to distribute the input stream to the methods according the chunk type.
   *
   * @param chunkType The chunk type from input.
   * @param remainingLength The chunk length from input.
   * @throws IOException In case of an IO problem.
   * @throws DataFormatException In case of problems with the data format.
   */
  void processChunk() throws IOException {
    crc.reset();
    crcUpdateInt(chunkType);

    switch (chunkType) {
    case IHDR:
      processIHDR();
      break;

    case acTL:
      processACTL();
      break;

    case fcTL:
      processFCTL();
      break;

    case IDAT:
      processIDAT();
      break;

    case fdAT:
      processFDAT();
      break;

    case bKGD:
      processBKGD();
      break;

    case gAMA:
      processGAMA();
      break;

    case tRNS:
      processTRNS();
      break;

    case PLTE:
      processPLTE();
      break;

    case IEND:
      break;

    default:
      ignoreChunkType();
      break;
    }
    
   // Checks the calculated checksum against the checksum read from file.
    int crcCalc = (int) crc.getValue();
    int nCRCRead = readInt();
    if (crcCalc != nCRCRead) {
      throw new IOException("Checksum error, expected " + nCRCRead + " calculated " + crcCalc);
    }
  }

  /**
   * Ignores a chunk.
   */
  private void ignoreChunkType() throws IOException {
    byte[] bytes = new byte[remainingLength];
    crcReadFully(bytes);
  }

  //-------------------------------------- CHUNKS PROCESSING ---

  /**
   * Reads and Checks the signature
   */
  void readSignature() throws IOException {
    for (int n = 0; n < APNG_SIGNATURE.length; n++) {
      int nByte = read();
      if (nByte != (APNG_SIGNATURE[n] & 0xff)) {
        throw new IOException("Wrong PNG Signature");
      }
    }
  }

  /**
   * Reads the initial header chunk (IHDR).
   */
  void processIHDR() throws IOException {
    int width = crcReadInt();
    int height = crcReadInt();
    if (width <= 0) {
      throw new IOException("Bad PNG image width, must be > 0!");
    }
    if (height <= 0) {
      throw new IOException("Bad PNG image height, must be > 0!");
    }
    if (width >= (Integer.MAX_VALUE / height)) {
      throw new IOException("Bad PNG image size!");
    }

    byte bitDepth = crcReadByte();
    if (bitDepth != 1 && bitDepth != 2 && bitDepth != 4 && bitDepth != 8 && bitDepth != 16) {
      throw new IOException("Bad PNG bit depth");
    }

    byte colorType = crcReadByte();
    if (colorType > 6 || colorType == 1 || colorType == 5) {
      throw new IOException("Bad PNG color type");
    }
    byte compressionMethod = crcReadByte();
    if (compressionMethod != 0) {
        throw new IOException("Bad PNG comression!");
    }

    byte filterMethod = crcReadByte();
    if (filterMethod != 0) {
        throw new IOException("Bad PNG filter method!");
    }

    byte interlaceMethod = crcReadByte();
    if (interlaceMethod != 0 && interlaceMethod != 1) {
        throw new IOException("Unknown interlace method (not 0 or 1)!");
    }

    this.header = new ApngHeader(width, height, bitDepth, colorType,
        compressionMethod, filterMethod, interlaceMethod);  
  }

  /**
   * Reads the animation control chunk (acTL).
   */
  void processACTL() throws IOException {
    this.availableFrames = crcReadInt();    // Number of frames
    this.numLoops = crcReadInt();           // Number of loops
  }

  /**
   * Reads the transparency chunk (tRNS).
   */
  void processTRNS() throws IOException {
    switch (header.getColorType()) {
      case ApngDecoder.PNG_COLOR_PALETTE:
        if (palette == null) {
            emitWarning("tRNS chunk without prior PLTE chunk, ignoring it.");
            return;
        }
        byte[] TRNS = new byte[remainingLength];
        crcReadFully(TRNS);
        palette.applyTransparency(TRNS);
        tRNS_present = true;
        break;
      case ApngDecoder.PNG_COLOR_GRAY:
        if (remainingLength == 2) {
          transparentColor = new int[3];
          transparentColor[1] = crcReadUnsignedShort();
          tRNS_present = true;
        }
        break;
      case ApngDecoder.PNG_COLOR_RGB:
        if (remainingLength == 6) {
          transparentColor = new int[3];
          transparentColor[0] = crcReadUnsignedShort();
          transparentColor[1] = crcReadUnsignedShort();
          transparentColor[2] = crcReadUnsignedShort();
          tRNS_present = true;
        }
        break;
      default:
        emitWarning("TransparencyChunk may not present when alpha explicitly defined");
        ignoreChunkType();
    }
  }

  /**
   * Reads the background chunk (bKGD).
   */
  void processBKGD() throws IOException {
    // TODO: Implement
    ignoreChunkType();
  }

  /**
   * Reads the gamma correction chunk (gAMA).
   */
  void processGAMA() throws IOException {
    // Representing the gamma value times 100000
    /*this.gamma =*/ crcReadInt();
  }

  /**
   * Reads the palette chunk (PLTE).
   */
  void processPLTE() throws IOException {
    if (palette != null) {
      emitWarning("A PNG image may not contain more than one PLTE chunk.\n" + "The chunk wil be ignored.");
      ignoreChunkType();
      return;
    }
    switch (header.getColorType()) {
      case ApngDecoder.PNG_COLOR_PALETTE:
        byte[] abPLTE = new byte[remainingLength];
        crcReadFully(abPLTE);
        this.palette = ApngPalette.createPalette(abPLTE, header.getBitDepth());
        return;
      case ApngDecoder.PNG_COLOR_GRAY:
      case ApngDecoder.PNG_COLOR_GRAY_ALPHA:
        emitWarning("A PNG gray or gray alpha image cannot have a PLTE chunk.\n" + "The chunk wil be ignored.");
      // silently ignore palette for RGB
      default:
        ignoreChunkType();
    }
  }

	/**
   * Reads the frame control chunk (fcTL).
   */
  void processFCTL() throws IOException {
    int frameSequence = crcReadInt();
    if (frameSequence != frameSequenceExpected) {
      throw new IOException("Wrong Frame Sequence");
    }
    frameSequenceExpected++;

    this.frameControl = new ApngFrameControl(
        crcReadInt(),             // Width
        crcReadInt(),             // Height
        crcReadInt(),             // Offset x
        crcReadInt(),             // Offset y
        crcReadUnsignedShort(),   // Delay numerator
        crcReadUnsignedShort(),   // Delay denominator
        crcReadByte(),            // Dispose operation
        crcReadByte()             // Blend operation
      );
  }

  /**
   * Reads the image data chunk (IDAT).
   */
  void processIDAT() throws IOException {
    idatCount++;
    collectImageData();
  }

  /**
   * Reads the frame data chunk (fdAT).
   */
  void processFDAT() throws IOException {
    int nSequence = crcReadInt();

    if (nSequence != frameSequenceExpected) {
      throw new IOException("failure.wrong.framesequence");
    }
    frameSequenceExpected++;

    collectImageData();
  }

  // capture frame data (IDAT / FDAT)
  private byte[] buffer = new byte[32000];

  /**
   * Collects image data for the IDAT and fdAT chunk and puts it into the lingering data.
   */
  void collectImageData() throws IOException {
    while (remainingLength > 0) {
      int nRead = crcRead(buffer, 0, Math.min(remainingLength, buffer.length));
      if (nRead <= 0) {
        throw new EOFException("Unexpected end of file encountered in collectImageData");
      }
      frameData.write(buffer, 0, nRead);
    }
  }

  @Override
  public void close() throws IOException {
    super.close();
  }

  /**
   * 
   * @param warning
   */
  private void emitWarning(String warning) {
    // apngImageLoader.emitWarning(warning);
	}

  //----------------------------------- READ / CRC UTILITIES ---

    /**
   * Reads a byte from input stream with CRC update.
   */
  byte crcReadByte() throws IOException {
    byte b = readByte();
    remainingLength -= 1;
    crc.update(b);
    return b;
  }

  /**
   * Reads an unsigned short from input stream with CRC update.
   */
  int crcReadUnsignedShort() throws IOException {
    int n = readUnsignedShort();
    remainingLength -= 2;
    crcUpdateShort(n);
    return n;
  }

  /**
   * Reads an int from input stream with CRC update.
   */
  int crcReadInt() throws IOException {
    int n = readInt();
    remainingLength -= 4;
    crcUpdateInt(n);
    return n;
  }

  /**
   * Reads an array of bytes with CRC update.
   */
  int crcRead(byte bytes[]) throws IOException {
    int nRead = read(bytes);
    crc.update(bytes);
    return nRead;
  }

  /**
   * Reads an array of bytes with CRC update.
   */
   public int crcRead(byte[] bytes, int offset, int length) throws IOException {
    int nRead = read(bytes, offset, length);
    crc.update(bytes, offset, nRead);
    remainingLength -= nRead;
    return nRead;
  }

  /**
   * Reads an array of bytes with CRC update.
   */
  public void crcReadFully(byte[] bytes) throws IOException {
      crcReadFully(bytes, 0, bytes.length);
  }

  /**
   * Reads an array of bytes with CRC update.
   */
  public void crcReadFully(byte[] bytes, int offset, int length) throws IOException {
    readFully(bytes, offset, length);
    crc.update(bytes, offset, length);
  }

  private void crcUpdateInt(int n) {
    crc.update((n >>> 24) & 0xff);
    crc.update((n >> 16) & 0xff);
    crc.update((n >> 8) & 0xff);
    crc.update(n & 0xff);
  }

  private void crcUpdateShort(int n) {
    crc.update((n >> 8) & 0xff);
    crc.update(n & 0xff);
  }

}
