package de.mephisto.vpin.ui.apng.image;

import de.mephisto.vpin.ui.apng.chunks.ApngChunkDataInputStream;
import de.mephisto.vpin.ui.apng.chunks.ApngPalette;

public class ApngFrameDecoderIndexed extends ApngFrameDecoder
{
  final int maxBit;
  final int bitMask;
  final int[] shifts;

  ApngPalette palette;

  ApngFrameDecoderIndexed(ApngChunkDataInputStream stream) {
    super(stream);

		switch (bitDepth) {
    case 1:
      maxBit = 7;
      bitMask = 1;
      shifts = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
      break;

    case 2:
      maxBit = 3;
      bitMask = 3;
      shifts = new int[] { 0, 2, 4, 6 };
      break;

    case 4:
      maxBit = 1;
      bitMask = 15;
      shifts = new int[] { 0, 4 };
      break;

    default:
      maxBit = 0;
      bitMask = 0;
      shifts = null;
      break;
    }
  }

  @Override
  public void write(ApngFrame dest, byte[] src, int srcIdx, int offsX, int widthX, int stepX, int line, boolean composeAlphacomposeAlpha) {
    final long[] pixels = dest.getPixels();
    final int destOffset = line * dest.getWidth();

    if (palette == null) {
      palette = stream.getPalette();
      // palette still null ??
      if (palette == null)
      throw new RuntimeException("Missing Palette");
    }

    switch (bitDepth) {
    case 1:
    case 2:
    case 4:
      int nBit = maxBit;

      for (int nX = offsX; nX < offsX + widthX; nX += stepX) {
        final int nIdx = (src[srcIdx] >> shifts[nBit]) & bitMask;
        pixels[destOffset + nX] = palette.get(nIdx);

        if (nBit == 0) {
          srcIdx++;
          nBit = maxBit;
        }
        else {
          nBit--;
        }
      }
      break;

    case 8:
      for (int nX = offsX; nX < offsX + widthX; nX += stepX) {
        final int nIdx = src[srcIdx++] & 0xff;
        pixels[destOffset + nX] = palette.get(nIdx);
      }
      break;

    default:
      throw new RuntimeException("Wrong Bitdepth");
    }
  }

}