package de.mephisto.vpin.server.dmdscore;

import java.io.File;

/**
 * A Processor that split the frame in two parts and analyse them separately
 * ex: Avatar (Stern) - avr_200
 */
public class DMDScoreProcessor2Sides extends DMDScoreProcessorImageScanner {

  private int splitPosition;

  public DMDScoreProcessor2Sides(int splitPosition) {
    this.splitPosition = splitPosition;
  }

  @Override
  public void onFrameReceived(Frame frame) {

    // Resize images
    int width = frame.getWidth();
    int height = frame.getHeight();

    int scale = 3;
    int H = height * scale;
    // Blur effect
    int radius = 1;
    int size = 2 * radius + 1;
    size *= size;

    // Apply the transformations, crop, rescale and recolor, then blur
    int[] left = crop(frame.getPlane(), width, height, 0, splitPosition - 1, scale, (byte) size);
    left = blur(left, (splitPosition - 1) * scale, H, radius);
    File leftImg = saveImage(left, (splitPosition - 1) * scale, H, Integer.toString(frame.getTimeStamp()) + "_left");
    String leftTxt = extractText(leftImg);

    int[] right = crop(frame.getPlane(), width, height, splitPosition + 1, width, scale, (byte) size);
    right = blur(right, (width - splitPosition - 1) * scale, H, radius);
    File rightImage = saveImage(right, (width - splitPosition - 1) * scale, H, Integer.toString(frame.getTimeStamp()) + "_right");
    String rightTxt = extractText(rightImage);
  }
}
