package de.mephisto.vpin.ui.apng.chunks;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import de.mephisto.vpin.ui.apng.image.ApngFrame;

/**
 * Copied from PNGImageLoader
 */
public class ApngDecoder {

  // color model
  public static final int PNG_COLOR_GRAY = 0;
  public static final int PNG_COLOR_RGB = 2;
  public static final int PNG_COLOR_PALETTE = 3;
  public static final int PNG_COLOR_GRAY_ALPHA = 4;
  public static final int PNG_COLOR_RGB_ALPHA = 6;
  // channels per pixel
  static final int[] numBandsPerColorType = {1, -1, 3, 1, 2, -1, 4};
  // filters
  static final int PNG_FILTER_NONE = 0;
  static final int PNG_FILTER_SUB = 1;
  static final int PNG_FILTER_UP = 2;
  static final int PNG_FILTER_AVERAGE = 3;
  static final int PNG_FILTER_PAETH = 4;

  // data stream
  //private int width, height;
  private int bitDepth, colorType;
  private boolean isInterlaced;
  // transparency information
  private boolean tRNS_GRAY_RGB = false;
  private int trnsR, trnsG, trnsB;

  public ApngDecoder(ApngHeader header, int[] transparentColor) {
    this.bitDepth = header.getBitDepth();
    this.colorType = header.getColorType();
    this.isInterlaced = header.getInterlaceMethod() != 0;

    if (transparentColor != null) {
      this.tRNS_GRAY_RGB = true;
      this.trnsR = transparentColor[0];
      this.trnsG = transparentColor[1];
      this.trnsB = transparentColor[2];
    }
  }

  public void decode(ApngFrame dest, ApngFrameControl ctrl, byte[] frameData, boolean composeAlpha) throws IOException {
    Inflater inf = new Inflater();
    InputStream data = new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(frameData), inf));
    try {
      if (isInterlaced) {
        for (int mip = 0; mip != 7; ++mip) {
          if (ctrl.getWidth() > starting_x[mip] && ctrl.getHeight() > starting_y[mip]) {
            loadMip(dest.getBytes(), dest.getWidth(), data, ctrl.getXOffset(), ctrl.getYOffset(), ctrl.getWidth(), ctrl.getHeight(), mip, composeAlpha);
          }
        }
      } else {
          loadMip(dest.getBytes(), dest.getWidth(), data, ctrl.getXOffset(), ctrl.getYOffset(), ctrl.getWidth(), ctrl.getHeight(), 7, composeAlpha);
      }
    } finally {
      if (inf != null) {
        inf.end();
      }
    }
  }

  //------------------------------------ DISPOSE -------------

  public ApngFrame dispose(ApngFrame current, ApngFrame prevImage, ApngFrameControl ctrl, int delayMillis) {

    switch (ctrl.getDisposeOp()) {
      case ApngFrameControl.DISPOSE_OP_PREVIOUS: {
        if (prevImage != null) {
          // the frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame.
          ApngFrame next = new ApngFrame(current, delayMillis);
          int bpp = current.getBpp();
          int fromIndex = (ctrl.getYOffset() * current.getWidth() + ctrl.getXOffset()) * bpp;
          for (int line = 0; line < ctrl.getHeight(); line++) {
            System.arraycopy(prevImage.getBytes(), fromIndex, next.getBytes(), fromIndex, ctrl.getWidth() * bpp);
            fromIndex += current.getWidth() * bpp;
          }
          return next;
        }
        // When Disposal is to revert to previous image, but it does not exists, disposal is to clear background
        // so here we don't return nor break and use case DISPOSE_OP_BACKGROUND
      }
      case ApngFrameControl.DISPOSE_OP_BACKGROUND: {
        // Get data from the current frame but the drawing region is needed to be cleared
        ApngFrame next = new ApngFrame(current, delayMillis);
        int bpp = current.getBpp();
        int fromIndex = (ctrl.getYOffset() * current.getWidth() + ctrl.getXOffset()) * bpp;
        for (int line = 0; line < ctrl.getHeight(); line++) {
          Arrays.fill(next.getBytes(), fromIndex, fromIndex + ctrl.getWidth() * bpp, (byte) 0x00);
          fromIndex += current.getWidth() * bpp;
        }
        return next;
      }
      case ApngFrameControl.DISPOSE_OP_NONE: 
      default: {
        // no disposal is done, the contents of the output buffer are left as is.
        return new ApngFrame(current, delayMillis);
      }
    }
  }

  //------------------------------------ FILTERS ---

  private void doFilter(byte line[], byte pline[], int fType, int bpp) {
    switch (fType) {
    case PNG_FILTER_SUB: doSubFilter(line, bpp); break;
    case PNG_FILTER_UP: doUpFilter(line, pline); break;
    case PNG_FILTER_AVERAGE: doAvrgFilter(line, pline, bpp); break;
    case PNG_FILTER_PAETH: doPaethFilter(line, pline, bpp); break;
    }
  }

  private void doSubFilter(byte line[], int bpp) {
    for (int i = bpp, l = line.length; i != l; ++i) {
      line[i] = (byte) (line[i] + line[i - bpp]);
    }
  }

  private void doUpFilter(byte line[], byte pline[]) {
    for (int i = 0, l = line.length; i != l; ++i) {
      line[i] = (byte) (line[i] + pline[i]);
    }
  }

  private void doAvrgFilter(byte line[], byte pline[], int bpp) {
    for (int i = 0; i != bpp; ++i) {
      line[i] = (byte) (line[i] + (pline[i] & 0xFF) / 2);
    }
    for (int i = bpp, l = line.length; i != l; ++i) {
      line[i] = (byte) (line[i] + (((line[i - bpp] & 0xFF) + (pline[i] & 0xFF))) / 2);
    }
  }

  private static int paethPrediction(int a, int b, int c) {
    int pa = Math.abs(b - c);           // p-a
    int pb = Math.abs(a - c);           // p-b
    int pc = Math.abs(b - c + a - c);   // p-c
    return (pa <= pb && pa <= pc) ? a : (pb <= pc) ? b : c;
  }

  private void doPaethFilter(byte line[], byte pline[], int bpp) {
    int l = line.length;
    for (int i = 0; i != bpp; ++i) {
      line[i] = (byte) (line[i] + pline[i]);
    }
    for (int i = bpp; i != l; ++i) {
      line[i] = (byte) (line[i] + paethPrediction(line[i - bpp] & 0xFF, pline[i] & 0xFF, pline[i - bpp] & 0xFF));
    }
  }

  //--------------------------------------------------

  protected void copyTrns_gray(byte line[], byte image[], int pos, int step, int bd, boolean composeAlpha) {
    int l = line.length;
    for (int i = 0, oPos = pos; i < l; oPos += step * 2, i += bd) {
      byte g = line[i];
      byte a;
      if (bd == 2) {
	      int gray16 = (short) ((line[i] & 0xFF) * 256 + (line[i + 1] & 0xFF));
	      a = (gray16 == trnsG) ? 0 : (byte) 255;
      } else {
      	a = (g == (byte) trnsG) ? 0 : (byte) 255;
      }
      if (composeAlpha) {
        g = a == 0 ? image[oPos + 0] : g;
        a = a == 0 ? image[oPos + 1] : a;
      }
      image[oPos + 0] = g;
      image[oPos + 1] = a;
    }
  }

  protected void copyTrns_rgb(byte line[], byte image[], int pos, int step, int bd, boolean composeAlpha) {
    int l = line.length;
    for (int i = 0, oPos = pos; i < l; oPos += step * 4, i += 3 * bd) {
      byte r = line[i], g = line[i + bd], b = line[i + 2 * bd];
      byte a;
      if (bd == 2) {
	      int r16 = (short) ((line[i + 0] & 0xFF) * 256 + (line[i + 1] & 0xFF));
	      int g16 = (short) ((line[i + 2] & 0xFF) * 256 + (line[i + 3] & 0xFF));
	      int b16 = (short) ((line[i + 4] & 0xFF) * 256 + (line[i + 5] & 0xFF));
	      a = (r16 == trnsR && g16 == trnsG && b16 == trnsB) ? 0 : (byte) 255;
      } else {
  		  a = (r == (byte) trnsR && g == (byte) trnsG && b == (byte) trnsB) ? 0 : (byte) 255;
	    }
      if (composeAlpha) {
        r = a == 0 ? image[oPos + 0] : r;
        g = a == 0 ? image[oPos + 1] : g;
        b = a == 0 ? image[oPos + 2] : b;
        a = a == 0 ? image[oPos + 3] : a;
      }
      image[oPos + 0] = r;
      image[oPos + 1] = g;
      image[oPos + 2] = b;
      image[oPos + 3] = a;
    }
  }

  protected void copy_plain_graya(byte line[], byte image[], int pos, int step, int bd, boolean composeAlpha) {
    int l = line.length;
    for (int i = 0, oPos = pos; i != l; oPos += step * 2, i += 2 * bd) {
      byte g = line[i], a = line[i + bd];
      if (composeAlpha) {
        byte ai = image[oPos + 1];
        byte af = (byte) (a + ai * (255 - a) / 255.0);
        g = composeAlpha(image[oPos + 0], g, ai, a, af);
        a = af;
      }
      image[oPos + 0] = g;
      image[oPos + 1] = a;
    }
  }

  protected void copy_plain_rgba(byte line[], byte image[], int pos, int step, int bd, boolean composeAlpha) {
    int l = line.length;
    for (int i = 0, oPos = pos; i < l; oPos += step * 4, i += 4 * bd) {
      byte r = line[i], g = line[i + bd], b = line[i + 2 * bd], a = line[i + 3 * bd];
      if (composeAlpha) {
        byte ai = image[oPos + 3];
        byte af = (byte) (a + ai * (255 - a) / 255.0);
        r = composeAlpha(image[oPos + 0], r, ai, a, af);
        g = composeAlpha(image[oPos + 1], g, ai, a, af);
        b = composeAlpha(image[oPos + 2], b, ai, a, af);
        a = af;
      }
      image[oPos + 0] = r;
      image[oPos + 1] = g;
      image[oPos + 2] = b;
      image[oPos + 3] = a;
    }
  }

  private void copy_plain(byte line[], byte image[], int pos, int step, int bpp, int bd) {
    int l = (line.length / bd / bpp) * bpp, stepBpp = step * bpp;
    for (int i = 0, oPos = pos; i != l; oPos += stepBpp, i += bpp) {
      for (int b = 0; b != bpp; ++b) {
        image[oPos + b] = line[(i + b) * bd];
      }
    }
  }

  protected void copy(byte line[], byte image[], int pos, int step, int bpp, int bd, boolean composeAlpha) {
    if (!tRNS_GRAY_RGB) {
      if (bd == 1 & step == 1 && !composeAlpha) {
        System.arraycopy(line, 0, image, pos, line.length);
      } else if (bpp == 1 || bpp == 3) {
        copy_plain(line, image, pos, step, bpp, bd);
      } else if (bpp == 2) {
        copy_plain_graya(line, image, pos, step, bd, composeAlpha);
      } else if (bpp == 4) {
        copy_plain_rgba(line, image, pos, step, bd, composeAlpha);
      }
    } else if (colorType == PNG_COLOR_GRAY) {
      copyTrns_gray(line, image, pos, step, bd, composeAlpha); // resultBpp==2
    } else if (colorType == PNG_COLOR_RGB) {
      copyTrns_rgb(line, image, pos, step, bd, composeAlpha); // resultBpp==4
    }
  }

  protected void upsampleTo8Palette(byte line[], byte image[], int pos, int w, int step) {
    int samplesInByte = 8 / bitDepth;
    int maxV = (1 << bitDepth) - 1;
    for (int i = 0, k = 0; i < w; k++, i += samplesInByte) {
      int p = (w - i < samplesInByte) ? w - i : samplesInByte;
      int in = line[k] >> (samplesInByte - p) * bitDepth;
      for (int pp = p - 1; pp >= 0; --pp) {
        image[pos + (i + pp) * step] = (byte) (in & maxV);
        in >>= bitDepth;
      }
    }
  }

  protected void upsampleTo8Gray(byte line[], byte image[], int pos, int w, int step) {
    int samplesInByte = 8 / bitDepth;
    int maxV = (1 << bitDepth) - 1, hmaxV = maxV / 2;
    for (int i = 0, k = 0; i < w; k++, i += samplesInByte) {
      int p = (w - i < samplesInByte) ? w - i : samplesInByte;
      int in = line[k] >> (samplesInByte - p) * bitDepth;
      for (int pp = p - 1; pp >= 0; --pp) {
        image[pos + (i + pp) * step] = (byte) (((in & maxV) * 255 + hmaxV) / maxV);
        in >>= bitDepth;
      }
    }
  }

  protected void upsampleTo8GrayTrns(byte line[], byte image[], int pos, int w, int step, boolean composeAlpha) {
    int samplesInByte = 8 / bitDepth;
    int maxV = (1 << bitDepth) - 1, hmaxV = maxV / 2;
    for (int i = 0, k = 0; i < w; k++, i += samplesInByte) {
      int p = (w - i < samplesInByte) ? w - i : samplesInByte;
      int in = line[k] >> (samplesInByte - p) * bitDepth;
      for (int pp = p - 1; pp >= 0; --pp) {
        int idx = pos + (i + pp) * step * 2;
        int value = in & maxV;
        byte g = (byte) ((value * 255 + hmaxV) / maxV);
        byte a = value == trnsG ? 0 : (byte) 255;
        if (composeAlpha) {
          g = a == 0 ? image[idx] : g;
        }
        image[idx] = g;
        image[idx + 1] = a;
        in >>= bitDepth;
      }
    }
  }

  protected void upsampleTo8(byte line[], byte image[], int pos, int w, int step, int bpp, boolean composeAlpha) {
    if (colorType == PNG_COLOR_PALETTE) { // as is decoder
      upsampleTo8Palette(line, image, pos, w, step);
    } else if (bpp == 1) {
      upsampleTo8Gray(line, image, pos, w, step);
    } else if (tRNS_GRAY_RGB && bpp == 2) {
      upsampleTo8GrayTrns(line, image, pos, w, step, composeAlpha);
    }
  }

  /**
   * @see https://pmt.sourceforge.io/specs/png-1.2-pdg.html#D.Alpha-channel-processing
   */
  private byte composeAlpha(byte background, byte foreground, byte alphaBg, byte alphaFg, byte alphaFinal) {
    if (alphaFg == 0) {
      // Foreground is full transparent.
      return background;
    }
    else if (alphaFg == (byte) 255) {
      // Foreground is full opaque.
      return foreground;
    }
    else {
      double colorFinal = (alphaFg * foreground + alphaBg * background * ((byte) 255 - alphaFg));
      return (byte) (colorFinal / alphaFinal);
    }
  }

  //--------------------------------- INTERLACING ---

  private static final int starting_y[] = { 0, 0, 4, 0, 2, 0, 1, 0 };
  private static final int starting_x[] = { 0, 4, 0, 2, 0, 1, 0, 0 };
  private static final int increment_y[] = { 8, 8, 8, 4, 4, 2, 2, 1 };
  private static final int increment_x[] = { 8, 8, 4, 4, 2, 2, 1, 1 };

  private static int mipSize(int size, int mip, int start[], int increment[]) {
    return (size - start[mip] + increment[mip] - 1) / increment[mip];
  }

  private static int mipPos(int pos, int mip, int start[], int increment[]) {
    return start[mip] + pos * increment[mip];
  }

  protected void loadMip(byte image[], int imgWidth, InputStream data, int offsetX, int offsetY, int width, int height, int mip, boolean composeAlpha) throws IOException {

    int mipWidth = mipSize(width, mip, starting_x, increment_x);
    int mipHeight = mipSize(height, mip, starting_y, increment_y);

    int scanLineSize = (mipWidth * bitDepth * numBandsPerColorType[colorType] + 7) / 8;
    byte scanLine0[] = new byte[scanLineSize];
    byte scanLine1[] = new byte[scanLineSize];

    // numBands might be more than numBandsPerColorType[colorType] to support tRNS
    int resultBpp = bpp(), srcBpp = numBandsPerColorType[colorType] * bytesPerColor();

    for (int y = 0; y != mipHeight; ++y) {
      int filterByte = data.read();
      if (filterByte == -1) {
        throw new EOFException();
      }

      if (data.read(scanLine0) != scanLineSize) {
        throw new EOFException();
      }

      doFilter(scanLine0, scanLine1, filterByte, srcBpp);

      int pos = ((y + mipPos(offsetY, mip, starting_y, increment_y)) * imgWidth + offsetX + starting_x[mip]) * resultBpp;
      int step = increment_x[mip];

      if (bitDepth == 16) {
        copy(scanLine0, image, pos, step, resultBpp, 2, composeAlpha);
      } else if (bitDepth < 8) {
        upsampleTo8(scanLine0, image, pos, mipWidth, step, resultBpp, composeAlpha);
      } else {
        copy(scanLine0, image, pos, step, resultBpp, 1, composeAlpha);
      }

      byte scanLineSwp[] = scanLine0;
      scanLine0 = scanLine1;
      scanLine1 = scanLineSwp;
    }
  }

  // we won`t decode palette on fly, we will do it later
  // it is possible that we might want original paletteized image
  // ImageFrame does not support 16 bit color depth,
  // numBandsPerColorType == bytesPerColorType
  // but we will convert RGB->RGBA and L->LA on order to support tRNS
  public int bpp() {
    return numBandsPerColorType[colorType] + (tRNS_GRAY_RGB ? 1 : 0);
  }

  private int bytesPerColor() {
    return bitDepth == 16 ? 2 : 1;
  }
}
