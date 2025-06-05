package de.mephisto.vpin.ui.apng.image;

import de.mephisto.vpin.ui.apng.chunks.ApngChunkDataInputStream;

public class ApngFrameDecoderTrueColor extends ApngFrameDecoder
{

  ApngFrameDecoderTrueColor(ApngChunkDataInputStream stream) {
    super(stream);
  }

  @Override
  public void write(ApngFrame dest, byte[] src, int srcIdx, int offsX, int stepX, int line, boolean composeAlpha) {
    final long[] pixels = dest.getPixels();
    final int width = dest.getWidth();

    int transparentColor = stream.getTransparentColor();

    if (colorType.hasAlpha()) {
      switch (bitDepth) {
      case 8:
        for (int nX = offsX; nX < width; nX += stepX) {
          int nR = src[srcIdx++] & 0xff;
          int nG = src[srcIdx++] & 0xff;
          int nB = src[srcIdx++] & 0xff;
          int nA = src[srcIdx++] & 0xff;

          if (composeAlpha) {
            int color = (int) pixels[line * width + nX];
            int nAbg = (color >> 24) & 0xff;
            int nAf = (int) (nA + nAbg * (255 - nA) / 255.0);
            nR = composeAlpha((color >> 16) & 0xff, nR, nAbg, nA, nAf);
            nG = composeAlpha((color >>  8) & 0xff, nG, nAbg, nA, nAf);
            nB = composeAlpha((color >>  0) & 0xff, nB, nAbg, nA, nAf);
            nA = nAf;
          }

          pixels[line * width + nX] = nA << 24 | nR << 16 | nG << 8 | nB;
        }
        break;

      case 16:
        // TODO: Warning for quality loss in App
        for (int nX = offsX; nX < width; nX += stepX) {
          int nR = src[srcIdx++] & 0xff;
          srcIdx++;
          int nG = src[srcIdx++] & 0xff;
          srcIdx++;
          int nB = src[srcIdx++] & 0xff;
          srcIdx++;
          int nA = src[srcIdx++] & 0xff;
          srcIdx++;

          if (composeAlpha) {
            int color = (int) pixels[line * width + nX];
            int nAbg = (color >> 24) & 0xff;
            int nAf = (int) (nA + nAbg * (255 - nA) / 255.0);
            nR = composeAlpha((color >> 16) & 0xff, nR, nAbg, nA, nAf);
            nG = composeAlpha((color >>  8) & 0xff, nG, nAbg, nA, nAf);
            nB = composeAlpha((color >>  0) & 0xff, nB, nAbg, nA, nAf);
            nA = nAf;
          }

          pixels[line * width + nX] = nA << 24 | nR << 16 | nG << 8 | nB;
        }
        break;

      default:
        throw new RuntimeException("Wrong Bitdepth");
      }
    }
    else
    {
      switch (bitDepth) {
      case 8:
        for (int nX = offsX; nX < width; nX += stepX) {
          final int nR = src[srcIdx++] & 0xff;
          final int nG = src[srcIdx++] & 0xff;
          final int nB = src[srcIdx++] & 0xff;
          final int nRGB = nR << 16 | nG << 8 | nB;

          if (nRGB == transparentColor) {
            pixels[line * width + nX] = 0;
          }
          else {
            pixels[line * width + nX] = 0xff000000 | nR << 16 | nG << 8 | nB;
          }
        }
        break;

      case 16:
        // TODO: Warning for quality loss in App
        for (int nX = offsX; nX < width; nX += stepX) {
          final int nR = src[srcIdx++] & 0xff;
          final int nR1 = src[srcIdx++] & 0xff;
          final int nG = src[srcIdx++] & 0xff;
          final int nG1 = src[srcIdx++] & 0xff;
          final int nB = src[srcIdx++] & 0xff;
          final int nB1 = src[srcIdx++] & 0xff;
          final int nRGB16 = nR << 40 | nR1 << 32 | nG << 24 | nG1 << 16 | nB << 8 | nB1;

          if (nRGB16 == transparentColor) {
            pixels[line * width + nX] = 0;
          }
          else {
            pixels[line * width + nX] = 0xff000000 | nR << 16 | nG << 8 | nB;
          }
        }
        break;

      default:
        throw new RuntimeException("Wrong Bitdepth");
      }
    }
  }

  /**
   * @see https://pmt.sourceforge.io/specs/png-1.2-pdg.html#D.Alpha-channel-processing
   */
  private int composeAlpha(int background, int foreground, int alphaBg, int alphaFg, int alphaFinal) {
    if (alphaFg == 0) {
      // Foreground is full transparent.
      return background;
    }
    else if (alphaFg == 255) {
      // Foreground is full opaque.
      return foreground;
    }
    else {
      double colorFinal = (alphaFg * foreground + alphaBg * background * (255 - alphaFg));
      return (int) (colorFinal / alphaFinal);
    }
  }
}