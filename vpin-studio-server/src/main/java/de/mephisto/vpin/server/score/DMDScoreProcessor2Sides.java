package de.mephisto.vpin.server.score;

import java.io.File;
import java.nio.ByteBuffer;

import javafx.scene.image.PixelFormat;

/**
 * A Processor that split the frame in two parts and analyse them separately
 * ex: Avatar (Stern) - avr_200
 */
public class DMDScoreProcessor2Sides extends  DMDScoreProcessorImageScanner {

  private int splitPosition;

  public DMDScoreProcessor2Sides(int splitPosition) {
    this.splitPosition = splitPosition;
  }

  @Override
  public String onFrameReceived(Frame frame, int[] palette, int width, int height) {

    int H = (height + 2 * border) * scale;
    int size = 2 * radius + 1;
    size *= size;

    PixelFormat<ByteBuffer> format = generateBlurPalette(size);

    // Apply the transformations, crop, rescale and recolor, then blur
    byte[] left = crop(frame.getPlane(), width, height, border, 0, splitPosition-1, 0, height, scale, (byte) size);
    left = blur(left, (splitPosition - 1 + 2 * border) * scale, H, radius);
    File leftImg = saveImage(left, (splitPosition - 1 + 2 * border) * scale, H, format, Integer.toString(frame.getTimeStamp()) + "_left");
    String leftTxt = extractText(leftImg);

    byte[] right = crop(frame.getPlane(), width, height, border, splitPosition+1, width, 0, height, scale, (byte) size);
    right = blur(right, (width - splitPosition - 1 + 2 * border) * scale, H, radius);
    File rightImage = saveImage(right, (width - splitPosition - 1 + 2 * border) * scale, H, format, Integer.toString(frame.getTimeStamp()) + "_right");
    String rightTxt = extractText(rightImage);

    return leftTxt + "\n" + rightTxt;
  }
}
