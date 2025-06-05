package de.mephisto.vpin.ui.apng.image;

import java.io.IOException;

import de.mephisto.vpin.ui.apng.chunks.ApngChunkDataInputStream;

/**
 */
public class ApngFrameDecoderGreyscale extends ApngFrameDecoder
{

  // Faster than using PngPalette
  static final int[] GREY4 =
  {
    0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
    0x88, 0x99, 0xaa, 0xbb, 0xcc, 0xdd, 0xee, 0xff
  };

  ApngFrameDecoderGreyscale(ApngChunkDataInputStream stream) {
    super(stream);
  }

  @Override
  public void write(ApngFrame dest, byte[] src, int srcIdx, int offsX, int stepX, int line, boolean composeAlpha) throws IOException {
    final long[] pixels = dest.getPixels();
    final int width = dest.getWidth();

    int  transparentColor = stream.getTransparentColor();

    if (colorType.hasAlpha())
    {
      switch (bitDepth) {
      case 4:
        for (int nX = offsX; nX < width; nX += stepX) {
          // Upper nibble = luminance index, lower nibble = alpha index
          final int nIdx = src[srcIdx++];
          final int nL = GREY4[(nIdx >> 4) & 0x0f];
          final int nA = GREY4[nIdx & 0x0f];

          pixels[line * width + nX] = nA << 24 | nL << 16 | nL << 8 | nL;
        }
        break;

      case 8:
        for (int nX = offsX; nX < width; nX += stepX) {
          final int nL = src[srcIdx++] & 0xff;
          final int nA = src[srcIdx++] & 0xff;

          pixels[line * width + nX] = nA << 24 | nL << 16 | nL << 8 | nL;
        }
        break;

      case 16:
        for (int nX = offsX; nX < width; nX += stepX) {
          final int nL = src[srcIdx++] & 0xff;
          srcIdx++;
          final int nA = src[srcIdx++] & 0xff;
          srcIdx++;

          pixels[line * width + nX] = nA << 24 | nL << 16 | nL << 8 | nL;
        }
        break;

      default:
        throw new IOException("Wrong Bitdepth");
      }
    }
    else
    {
      switch (bitDepth) {
      case 8:
        for (int nX = offsX; nX < width; nX += stepX) {
          final int nL0 = (src[srcIdx++] & 0xff);
          final boolean b = transparentColor == nL0;
          final int nL = b ? 0 : nL0;
          final int nA = b ? 0 : 0xff000000;

          pixels[line * width + nX] = nA | nL << 16 | nL << 8 | nL;
        }
        break;

      case 16:
        for (int nX = offsX; nX < width; nX += stepX) {
          final int nL0 = (src[srcIdx++] & 0xff);
          final int nL1 = (src[srcIdx++] & 0xff);
          final boolean b = transparentColor == ((nL0 << 8) | nL1);
          final int nL = b ? 0 : nL0;
          final int nA = b ? 0 : 0xff000000;

          pixels[line * width + nX] = nA | nL << 16 | nL << 8 | nL;
        }
        break;

      default:
        throw new IOException("Wrong Bitdepth");
      }
    }
  }
}