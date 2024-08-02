package de.mephisto.vpin.server.score;

/**
 * A Processor that split the frame in two parts and analyse them separately
 * ex: Avatar (Stern) - avr_200
 */
public class DMDScoreProcessor2Sides extends  DMDScoreScannerTessAPI {

  private int splitPosition;

  public DMDScoreProcessor2Sides(int splitPosition) {
    this.splitPosition = splitPosition;
  }

  @Override
  public String onFrameReceived(Frame frame, int[] palette) {

    int width = frame.getWidth();
    int height = frame.getHeight();
    int H = (height + 2 * border) * scale;
    int size = 2 * radius + 1;
    size *= size;

    // Apply the transformations, crop, rescale and recolor, then blur
    byte[] left = crop(frame.getPlane(), width, height, border, 0, splitPosition-1, 0, height, scale, (byte) size);
    int leftW = (splitPosition - 1 + 2 * border) * scale;
    left = blur(left, leftW, H, radius);
    String leftTxt = extractText(frame, left, leftW, H);

    byte[] right = crop(frame.getPlane(), width, height, border, splitPosition+1, width, 0, height, scale, (byte) size);
    int rightW = (width - splitPosition - 1 + 2 * border) * scale;
    right = blur(right, rightW, H, radius);
    String rightTxt = extractText(frame, right, rightW, H);

    return leftTxt + "\n" + rightTxt;
  }
}
