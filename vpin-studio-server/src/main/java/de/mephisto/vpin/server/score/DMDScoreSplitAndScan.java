package de.mephisto.vpin.server.score;

import java.io.File;
import java.util.List;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreSplitAndScan extends DMDScoreScannerTessAPI {

  public DMDScoreSplitAndScan() {
    super(true);
  }

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    splitV(frame, texts, 0, frame.getWidth(), 0, frame.getHeight());
  }

  protected void splitV(Frame frame, List<FrameText> texts, int xF, int xT, int yF, int yT) {
    if (hasBorder(frame, xF, xT, yF, yT)) {
      splitV(frame, texts, xF + 1, xT - 1, yF + 1, yT - 1);
      return;
    }

    byte blank = getBlankIndex(frame.getPalette());

    // first detect full single color vertical lines
    byte previousColor = -1;
    int xStart = xF;
    int xStartSplit = -1;
    for (int x = xF; x < xT; x++) {
      byte b = getSingleColumnColor(frame, x, yF, yT);
      // detection of a full white column
      if (b == blank) {
        // detection of a split
        if (previousColor >= 0 && previousColor != blank) {
          splitH(frame, texts, xStart, xStartSplit, yF, yT);
          xStart = x;
        }
      }
      else if (b >= 0) {
        if (previousColor == blank || previousColor == b) {
          xStartSplit = x;
        }
      } 
      previousColor = b;
    }

    splitH(frame, texts, xStart, xT, yF, yT);
  }

  protected void splitH(Frame frame, List<FrameText> texts, int xF, int xT, int yF, int yT) {
    if (hasBorder(frame, xF, xT, yF, yT)) {
      splitH(frame, texts, xF + 1, xT - 1, yF + 1, yT - 1);
      return;
    }

    byte blank = getBlankIndex(frame.getPalette());

    // detect images in frame and remove them
    if (removeImages(frame.getPlane(), frame.getWidth(), xF, xT, yF, yT, blank)) {
      // for debugging purpose, dump frame post images removal, if any
      if (DEV_MODE) {
        File imgFile = new File(folder, frame.getTimeStamp() + "_.png"); 
        saveImage(frame.getPlane(), frame.getWidth(), frame.getHeight(), generatePalette(frame.getPalette()), imgFile);
      }
    }

    // first detect full single color horizontal lines
    int yStart = -1;
    for (int y = yF; y < yT; y++) {
      int x = getFirstColorX(frame, xF, xT, yF, yT, y, blank);
      // detection of a full white row
      if (x < 0) {
        // detection of a split
        if (yStart >= 0) {
          splitByFontSize(frame, texts, xF, xT, yStart, y);
          yStart = -1;
        }
      }
      else {
        if (yStart < 0) {
          yStart = y;
        }
      }
    }

    if (yStart >= 0) {
      splitByFontSize(frame, texts, xF, xT, yStart, yT);
    }
  }

  protected void splitByFontSize(Frame frame, List<FrameText> texts, int xF, int xT, int yF, int yT) {
    byte blank = getBlankIndex(frame.getPalette());

    // set a minimal gap 
    float highestGap = 3;
    int startX = xF, xGapF = -1, xGapT = -1, endX = xT;
    // split in two parts and calculate the sumum between averge of size between a left and a right part
    for (int x = xF + 1; x < xT - 1; x++) {
      // must cut on gap
      int size = getFontSize(frame, x, yF, yT, blank);
      if (size > 0) {
        continue;
      }
      // while left part is 0 in average, this is blank space to remove
      float avgL = getAvgFontSize(frame, xF, x, yF, yT, blank);
      if (avgL == 0) {
        startX = x;
        continue;
      }
      // when avgR becomes 0, then all rest is blank space, interrupt loop 
      float avgR = getAvgFontSize(frame, x, xT, yF, yT, blank);
      if (avgR == 0) {
        endX = x;
        break;
      }
      // else avgL and avgR are > 0, first time we reach the max, capture x
      if (highestGap < Math.abs(avgL - avgR)) {
        highestGap = Math.abs(avgL - avgR);
        xGapF = x;
        xGapT = x;
      }
      // while we continue to reach the gap, if it does not evolve it is because of spaces
      else if (highestGap == Math.abs(avgL - avgR)) {
        xGapT = x;
      }
    }

    if (xGapF >= 0) {
      extractText(frame, texts, startX, xGapF, yF, getMaxFontSize(frame, startX, xGapF, yF, yT, blank) + 1);

      extractText(frame, texts, xGapT, endX, yF, getMaxFontSize(frame, xGapT, endX, yF, yT, blank) + 1);
    }
    else {
      extractText(frame, texts, startX, endX, yF, getMaxFontSize(frame, startX, endX, yF, yT, blank) + 1);
    }
  }

  private int getMaxFontSize(Frame frame, int xF, int xT, int yF, int yT, byte blank) {
    int maxSize = 0;
    for (int x = xF; x < xT; x++) {
      maxSize = Math.max(maxSize, getFontSize(frame, x, yF, yT, blank));
    }
    return maxSize;
  }

  private float getAvgFontSize(Frame frame, int xF, int xT, int yF, int yT, byte blank) {
    int totalSize = 0;
    int totalNb = 0;

    int blockSize = 0;
    int blockNb = 0;

    for (int x = xF; x < xT; x++) {
      int size = getFontSize(frame, x, yF, yT, blank);
      if (size > 0) {
        // compute block size
        blockSize = Math.max(blockSize, size);
        blockNb++;
      }
      else {
        // add the block size to the total
        totalSize += blockSize * blockNb;
        totalNb += blockNb;

        // new block will start
        blockSize = 0;
        blockNb = 0;
      }
    }
    // add also last block
    totalSize += blockSize * blockNb;
    totalNb += blockNb;

    return totalNb > 0 ? ((float) totalSize) / totalNb : 0;
  }

  protected int getFontSize(Frame frame, int x, int yF, int yT, byte blank) {
    for (int y = yT - 1; y >= yF; y--) {
      byte color = frame.getColor(x, y);
      if (color != blank) {
        return y;
      }
    }
    return -1;
  }
}
