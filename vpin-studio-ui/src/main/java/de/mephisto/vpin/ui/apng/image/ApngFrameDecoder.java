package de.mephisto.vpin.ui.apng.image;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import de.mephisto.vpin.ui.apng.chunks.ApngChunkDataInputStream;
import de.mephisto.vpin.ui.apng.chunks.ApngColorType;
import de.mephisto.vpin.ui.apng.chunks.ApngFrameControl;
import de.mephisto.vpin.ui.apng.chunks.ApngHeader;
import de.mephisto.vpin.ui.apng.chunks.ApngPalette;
import de.mephisto.vpin.ui.apng.image.ApngFrameDecoder;

public abstract class ApngFrameDecoder implements Closeable {

  protected final ApngChunkDataInputStream stream;
  // denormalized for lisibility and direct access
  protected final ApngColorType colorType;
  protected final int bitDepth;

  private int appFrameDecoded = 0;
  private ApngFrame prevImage = null;
  private ApngFrame currentImage = null;
 
  // keep the current frameControl so that the disposal to be done after rendering this frame, can be applied before next image generation
  // disposeOp specifies how the output buffer should be changed at the end of the delay (before rendering the next frame).
  private ApngFrameControl currentFrameControl;

  // at class level for avoiding creation for each frame (reuse)
  private final byte[] _buffer;
  private final byte[] _prevLine;

  /**
   */
  ApngFrameDecoder(ApngChunkDataInputStream stream) {
    this.stream = stream;
    ApngHeader header = stream.getHeader();
    this.colorType = header.getColorType();
    this.bitDepth = header.getBitDepth();

    int scanlineStride = getScanlineStride(header.getWidth(), bitDepth);
    _buffer = new byte[scanlineStride];
    _prevLine = new byte[scanlineStride];
  }

  public ApngFrame nextFrame() throws IOException {

    // read the next image / frame
    byte[] frameData = stream.nextDat();
    // is null when default image is not part of the animation:
    ApngFrameControl frameControl = stream.getLastFrameControl();

    ApngFrame next = null;
    // case of first frame
    if (currentImage == null) {
      if (frameData == null) {
        throw new IOException("The image does not contain any frame");
      }

      ApngHeader header = stream.getHeader();
      // when frame is before first FrameControl, then the frame is the default image but not part of the animation
      if (frameControl == null) {
        // memorize the default image
        currentImage = new ApngFrame(header.getWidth(), header.getHeight(), 0);
        
        // generate a dummy frameControl for decoding
        currentFrameControl = new ApngFrameControl(header.getWidth(), header.getHeight(), 0, 0, 
            0, 0, ApngFrameControl.DISPOSE_OP_BACKGROUND, ApngFrameControl.BLEND_OP_SOURCE);
        decode(currentImage, currentFrameControl, frameData, false);

        // dest is not set so that another frame is read
        next = nextFrame();
        // no more frame, just a default one, return it
        if (next == null) {
          appFrameDecoded++;
          return currentImage;
        }
        return next;
      }
      else {
        // default image is part of the animation
        next = new ApngFrame(header.getWidth(), header.getHeight(), frameControl.getDelayMillis());
        // for the first frame, the two blend modes are functionally equivalent due to the clearing of the output buffer, so ignore
        decode(next, frameControl, frameData, false);
      }
    } else if (frameData != null) {
      // dispose the current image with the currentFrameControl
      next = dispose(currentImage, currentFrameControl);
      // secondary frames
      decode(next, frameControl, frameData, frameControl.getBlendOp() == ApngFrameControl.BLEND_OP_OVER);
    }

    // shift images
    if (next != null) {
      prevImage = currentImage;
      currentImage = next;
      currentFrameControl = frameControl;
      appFrameDecoded++;
     return next;
    }
    // else end of decoding, check integrity
    int availableFrames = stream.getAvailableFrames();
      // if no ACTL chunk in the image, getNbAvailableFrames() return -1 and it is normal
    if (availableFrames != -1 && appFrameDecoded != availableFrames) {
      throw new IOException("Wrong number of frame, decoded " + appFrameDecoded + ", but available " + availableFrames);
    }
    return null;
  }

  public int getNbAvailableFrames() {
    return stream.getAvailableFrames();
  }

  public ApngColorType getColorType() {
    return colorType;
  }

  public ApngPalette getPalette() {
    return stream.getPalette();
  }

  public int getAnimationNumPlays() {
    return stream.getAnimationLoops();
  }

  public void close() throws IOException {
    stream.close();
  }

    //------------------------------------ INTERLACING ---
    // @See https://www.w3.org/TR/PNG/#8Interlace

  /*
    * X-axis offs x, step x, offs y, step y
    * Y-axis are passes 0 - 7++
    */
  static final int OFFSX = 0, STEPX = 1, OFFSY = 2, STEPY = 3;

  /**
   * ILMX = InterLace MatriX.
   */
  static final int[][] ILMX =
  {          // Pass
    { 0, 1, 0, 1 }, // 0 not interlaced
    { 0, 8, 0, 8 }, // 1
    { 4, 8, 0, 8 }, // 2
    { 0, 4, 4, 8 }, // 3
    { 2, 4, 0, 4 }, // 4
    { 0, 2, 2, 4 }, // 5
    { 1, 2, 0, 2 }, // 6
    { 0, 1, 1, 2 }, // 7
    { 0, 1, 0, 1 }  // Pseudo pad for pass 7
  };

  /**
   * Calculates the scanline stride (bytes per scanline).
   */
  private int getScanlineStride(int nWidth, int bitDepth) {
    int bitsPerPixel = bitDepth * colorType.getComponentsPerPixel();
    int bitsPerRow = bitsPerPixel * nWidth;
    return bitsPerRow / 8 + (bitsPerRow % 8 == 0 ? 0 : 8 - bitsPerRow % 8) + 1;
  }

  /**
   * Decode the given frame bytes, removes filtering and runs the interlacing passes if needed.
   * @param frameData The byte array in IDAT / FDAT
   */
  private void decode(ApngFrame dest, ApngFrameControl ctrl, byte[] frameData, boolean composeAlpha) throws IOException
  {
    int width = ctrl.getWidth();
    int fullHeight = ctrl.getHeight();

    int bitsPerPixel = bitDepth * colorType.getComponentsPerPixel();
    final int fullBytesPerLine = getScanlineStride(width, bitDepth);

    final Inflater inflater = new Inflater();
    inflater.setInput(frameData);

    // NONE or ADAM7
    int interlaceMethod = stream.getHeader().getInterlaceMethod();
    int nPass = interlaceMethod == 0 ? 0 : 1;

    do {
      final int offsX = ILMX[nPass][OFFSX];
      final int stepX = ILMX[nPass][STEPX];
      final int padX  = ILMX[nPass + 1][STEPX] - 1;
      final int offsY = ILMX[nPass][OFFSY];
      final int stepY = ILMX[nPass][STEPY];
      final int padY  = ILMX[nPass + 1][STEPY] - 1;
      // Two steps of padding. Do not "optimize".
      final int bytesPerLine = nPass == 0 ? fullBytesPerLine : (width + padX) / stepX * bitsPerPixel / 8 + 1;
      final int height = nPass == 0 ? fullHeight : (fullHeight + padY) / stepY;

      Arrays.fill(_prevLine, (byte) 0);

      for (int line = 0; line < height; line++) {
        try {
          int nInflated = inflater.inflate(_buffer, 0, bytesPerLine);
          if (nInflated == 0) {
            throw new IOException("Unexpected End Of File");
          }
        } catch (DataFormatException dfe) {
          throw new IOException("Cannot inflate frame", dfe);
        }

        revertFilter(_buffer, _prevLine, bytesPerLine);

        write(dest, _buffer, 1, ctrl.getXOffset() + offsX, stepX, ctrl.getYOffset() + line * stepY + offsY, composeAlpha);
      }

      if (nPass == 0 || nPass == 7) {
        return;
      }
      else {
        nPass++;
      }
    }
    while (true);
  }

  /**
   * Writes a line of the frame to the destination
   */
  public abstract void write(ApngFrame dest, byte[] source, int sourceIdx, int offsetX, int nStepX, int nLine, boolean composeAlpha) throws IOException;

  //------------------------------------ DISPOSE -------------

  private ApngFrame dispose(ApngFrame current, ApngFrameControl ctrl) {

    switch (ctrl.getDisposeOp()) {
      case ApngFrameControl.DISPOSE_OP_PREVIOUS: {
        if (prevImage != null) {
          // the frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame.
          ApngFrame next = new ApngFrame(current, ctrl.getDelayMillis());
          int fromIndex = ctrl.getYOffset() * current.getWidth() + ctrl.getXOffset();
          for (int line = 0; line < ctrl.getHeight(); line++) {
            System.arraycopy(prevImage.getPixels(), fromIndex, next.getPixels(), fromIndex, ctrl.getWidth());
            fromIndex += current.getWidth();
          }
          return next;
        }
        // When Disposal is to revert to previous image, but it does not exists, disposal is to clear background
        // so here we don't return nor break and use case DISPOSE_OP_BACKGROUND
      }
      case ApngFrameControl.DISPOSE_OP_BACKGROUND: {
        // Get data from the current frame but the drawing region is needed to be cleared
        ApngFrame next = new ApngFrame(current, ctrl.getDelayMillis());
        int fromIndex = ctrl.getYOffset() * current.getWidth() + ctrl.getXOffset();
        long blackTransparentColor = 0x00000000;
        for (int line = 0; line < ctrl.getHeight(); line++) {
          Arrays.fill(next.getPixels(), fromIndex, fromIndex + ctrl.getWidth(), blackTransparentColor);
          fromIndex += current.getWidth();
        }
        return next;
      }
      case ApngFrameControl.DISPOSE_OP_NONE: 
      default: {
        // no disposal is done, the contents of the output buffer are left as is.
        return new ApngFrame(current, ctrl.getDelayMillis());
      }
    }
  }

  //------------------------------------ FILTERS ---

  private static final int FILTER_NONE = 0;
  private static final int FILTER_SUB = 1;
  private static final int FILTER_UP = 2;
  private static final int FILTER_AVERAGE = 3;
  private static final int FILTER_PAETH = 4;

  private int getFilterOffset() {
    return (bitDepth * colorType.getComponentsPerPixel() + 7) / 8;
  }

  /**
   * Reverts the filtering according the first byte in the line buffer.
   */
  private void revertFilter(byte[] buffer, byte[] prevLine, int nBytesPerLine) {
    switch (buffer[0]) {
    case FILTER_NONE: revertFilterNone(buffer, prevLine, 1, nBytesPerLine); break;
    case FILTER_SUB: revertFilterSub(buffer, prevLine, 1, nBytesPerLine); break;
    case FILTER_UP: revertFilterUp(buffer, prevLine, 1, nBytesPerLine); break;
    case FILTER_AVERAGE: revertFilterAverage(buffer, prevLine, 1, nBytesPerLine); break;
    case FILTER_PAETH: revertFilterPaeth(buffer, prevLine, 1, nBytesPerLine); break;
    }
    System.arraycopy(buffer, 1, prevLine, 0, nBytesPerLine - 1);
  }

  /**
   * None filter
   */
  private void revertFilterNone(byte[] buffer, byte[] prevLine, int begin, int end) {
  }

  /**
   * sub filter
   */
  private void revertFilterSub(byte[] buffer, byte[] prevLine, int begin, int end) {
    int nAn = begin;
    for (int n = begin + getFilterOffset(); n < end; n++, nAn++) {
      final int nX = (buffer[n] & 0xff);
      final int nA = (buffer[nAn] & 0xff);

      buffer[n] = (byte) ((nX + nA) & 0xff);
    }
  }

  /**
   * Up filter
   */
  private void revertFilterUp(byte[] buffer, byte[] prevLine, int begin, int end) {
    int nBn = 0;
    for (int n = begin; n < end; n++, nBn++) {
      final int nX = (buffer[n] & 0xff),
        nB = (prevLine[nBn] & 0xff);

      buffer[n] = (byte) ((nX + nB) & 0xff);
    }
  }

  /**
   * average filter
   */
  private void revertFilterAverage(byte[] buffer, byte[] prevLine, int begin, int end) {
    int nAn = begin - getFilterOffset(), nBn = 0;
    for (int n = begin; n < end; n++, nAn++, nBn++) {
      final int nX = buffer[n],
        nA = (nAn < begin) ? 0 : (buffer[nAn] & 0xff),
        nB = (0xff & prevLine[nBn]);

      buffer[n] = (byte) (nX + ((nA + nB) / 2) & 0xff);
    }
  }

  /**
   * Calculates a Paeth prediction of the A, C, and B bytes.
   * @param nA The byte left of nX.
   * @param nB The byte above of nX.
   * @param nC The byte left of nB.
   * @return The prediction as an int.
   * @see <a href="https://www.w3.org/TR/PNG/#9Filter-type-4-Paeth">https://www.w3.org/TR/PNG/#9Filter-type-4-Paeth</a>
   */
  int calcPaethPrediction(int nA, int nB, int nC) {
    final int nP = nA + nB - nC,
        nPA = Math.abs(nP - nA),
        nPB = Math.abs(nP - nB),
        nPC = Math.abs(nP - nC);

    return ((nPA <= nPB && nPA <= nPC) ? nA : (nPB <= nPC) ? nB : nC) & 0xff;
  }

  /**
   * Paeth filter
   */
  private void revertFilterPaeth(byte[] buffer, byte[] prevLine, int begin, int end) {
    int nAn = begin - getFilterOffset();
    int nBn = 0;
    int nCn = -getFilterOffset();

    for (int n = begin; n < end; n++, nAn++, nBn++, nCn++) {
      final int nA = (nAn < begin) ? 0 : (buffer[nAn] & 0xff),
        nB = (prevLine[nBn] & 0xff),
        nC = (nAn < begin) ? 0 : (prevLine[nCn] & 0xff),
        nX = (buffer[n] & 0xff);

      buffer[n] = (byte)((nX + calcPaethPrediction(nA, nB, nC)) & 0xff);
    }
  }

  //----------------------------------------- FACTORY ---

  /**
   * Creates an ApngFrameDecoder according to color type and bit depth
   */
  public static ApngFrameDecoder getDecoderFor(InputStream input) throws IOException {

    ApngChunkDataInputStream stream = new ApngChunkDataInputStream(input);
    ApngHeader header = stream.getHeader();
    ApngColorType colorType = header.getColorType();
    int bitDepth = header.getBitDepth();

    switch (colorType) {
    case TRUECOLOR:
    case TRUECOLOR_ALPHA:
      return new ApngFrameDecoderTrueColor(stream);

    case INDEXED:
      return new ApngFrameDecoderIndexed(stream);

    case GREYSCALE:
    case GREYSCALE_ALPHA:
      switch (bitDepth)
      {
      case 1:
      case 2:
        return new ApngFrameDecoderIndexed(stream);

      case 4:
        if (colorType.hasAlpha()) {
          return new ApngFrameDecoderGreyscale(stream);
        }
        return new ApngFrameDecoderIndexed(stream);

      case 8:
      case 16:
        return new ApngFrameDecoderGreyscale(stream);

      default:
        throw new IOException("Wrong Bitdepth");
      }

    default:
      throw new IOException("Unsupported Colortype " + colorType);
    }
  }
}