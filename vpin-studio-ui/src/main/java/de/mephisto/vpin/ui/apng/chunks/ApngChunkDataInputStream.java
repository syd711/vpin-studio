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

  private ApngHeader header;
  private ApngPalette palette;
  private int transparentColor;
  //private int gamma;

  /** The current FrameControl that goes along side the nextFrame() */
  private ApngFrameControl fcTL;

  /** Number of animated frames contained in the image, -1 means not set as no ACTL chunk present */
  private int availableFrames = -1;
  /* how many loop to play, 0 means repeat infinitely */
  private int numLoops = -1;

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

  public ApngColorType getColorType() {
    return header.getColorType();
  }

  public int getAvailableFrames() {
    return availableFrames;
  }

  public int getAnimationLoops() {
    return numLoops;
  }

  public int getTransparentColor() {
    return transparentColor;
  }

  public ApngPalette getPalette() {
    return palette;
  }

  public ApngFrameControl getLastFrameControl() {
    return fcTL;
  }

  //--------------------------------------

  public byte[] nextDat() throws IOException {
    // comes again after end has been reached
    if (chunkType == ApngChunkType.IEND) {
      return null;
    }

    do {
      processChunk();
      readNextChunk();
    }
    while (chunkType != ApngChunkType.fcTL && chunkType != ApngChunkType.IEND);

    if (frameData.size() > 0) {
      byte[] dat = frameData.toByteArray();
      frameData.reset();
      return dat;
    }
    else if (chunkType != ApngChunkType.IEND) {
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
    if (remainingLength > 0xfffff) {
      throw new IOException("Wrong length for chunk");
    }

    this.chunkType = readInt();
    if (header == null && chunkType != ApngChunkType.IHDR) {
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
    case ApngChunkType.IHDR:
      processIHDR();
      break;

    case ApngChunkType.acTL:
      processACTL();
      break;

    case ApngChunkType.fcTL:
      processFCTL();
      break;

    case ApngChunkType.IDAT:
      processIDAT();
      break;

    case ApngChunkType.fdAT:
      processFDAT();
      break;

    case ApngChunkType.bKGD:
      processBKGD();
      break;

    case ApngChunkType.gAMA:
      processGAMA();
      break;

    case ApngChunkType.tRNS:
      processTRNS();
      break;

    case ApngChunkType.PLTE:
      processPLTE();
      break;

    case ApngChunkType.IEND:
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
    this.header = new ApngHeader(
        crcReadInt(),                       // Width
        crcReadInt(),                       // Height
        crcReadByte(),                      // Bit depth
        ApngColorType.byType(crcReadByte()), // Color type
        crcReadByte(),                      // Compression method
        crcReadByte(),                      // Filter method
        crcReadByte()                       // Interlace method
      );  
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
    byte[] TRNS = new byte[remainingLength];
    crcReadFully(TRNS);
    this.transparentColor = ApngPalette.fromTRNS(header.getColorType(), header.getBitDepth(), TRNS);
    if (this.palette != null) {
      palette.applyTransparency(TRNS);
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
    byte[] abPLTE = new byte[remainingLength];
    crcReadFully(abPLTE);
    this.palette = ApngPalette.createPalette(abPLTE);
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

    this.fcTL = new ApngFrameControl(
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
