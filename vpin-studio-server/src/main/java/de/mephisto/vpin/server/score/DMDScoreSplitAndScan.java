package de.mephisto.vpin.server.score;

import java.io.File;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreSplitAndScan extends DMDScoreScannerTessAPI {

  public DMDScoreSplitAndScan() {
    super(true);
  }

  @Override
  public String onFrameReceived(Frame frame) {
    return splitV(frame, 0, frame.getWidth(), 0, frame.getHeight());
  }

  protected String splitV(Frame frame, int xF, int xT, int yF, int yT) {
    if (hasBorder(frame, xF, xT, yF, yT)) {
      return splitV(frame, xF + 1, xT - 1, yF + 1, yT - 1);
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

    // else
    StringBuilder bld = new StringBuilder();

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
          String txt = splitH(frame, xStart, xStartSplit, yF, yT);
          bld.append(txt);
          // separate left f right
          bld.append("\n");
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

    String txt = splitH(frame, xStart, xT, yF, yT);
    bld.append(txt);
    return bld.toString();
  }

  protected String splitH(Frame frame, int xF, int xT, int yF, int yT) {
    if (hasBorder(frame, xF, xT, yF, yT)) {
      return splitH(frame, xF + 1, xT - 1, yF + 1, yT - 1);
    }
    // else
    StringBuilder bld = new StringBuilder();

    // first detect full single color horizontal lines
    byte idxBlank = getBlankIndex(frame.getPalette());
    int yStart = -1;
    for (int y = yF; y < yT; y++) {
      int x = getFirstColorX(frame, xF, xT, yF, yT, y, idxBlank);
      // detection of a full white row
      if (x < 0) {
        // detection of a split
        if (yStart >= 0) {
          String txt = splitByFontSize(frame, xF, xT, yStart, y);
          bld.append(txt);
          bld.append("\n");
          yStart = -1;
        }
      }
      else if (yStart < 0) {
        yStart = y;
      }
    }
    
    if (yStart >= 0) {
      String txt = splitByFontSize(frame, xF, xT, yStart, yT);
      bld.append(txt);
    }

    return bld.toString();
  }

  protected String splitByFontSize(Frame frame, int xF, int xT, int yF, int yT) {
    StringBuilder bld = new StringBuilder();

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
      String txt = extractText(frame, startX, xGapF, yF, getMaxFontSize(frame, startX, xGapF, yF, yT, blank) + 1);
      bld.append(txt);
      bld.append("\n");

      txt = extractText(frame, xGapT, endX, yF, getMaxFontSize(frame, xGapT, endX, yF, yT, blank) + 1);
      bld.append(txt);
    }
    else {
      String txt = extractText(frame, startX, endX, yF, getMaxFontSize(frame, startX, endX, yF, yT, blank) + 1);
      bld.append(txt);
    }
    return bld.toString();
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

  protected String splitByFontSize_3(Frame frame, int xF, int xT, int yF, int yT) {
    StringBuilder bld = new StringBuilder();

    byte blank = getBlankIndex(frame.getPalette());
    int previousFontSize = -1;
    int blockFontSize = 0;
    int spaceBetweenBlock = 0;
    int startX = -1;
    int blockX = -1;
    for (int x = xF; x < xT; x++) {
      int size = getFontSize(frame, x, yF, yT, blank);
      if (size < 0 && blockFontSize > 0 && spaceBetweenBlock >= 7) {
        // detection of new block
        if (previousFontSize <= 0) {
          previousFontSize = blockFontSize;
        }
        // second block has different size, 
        else if (previousFontSize != blockFontSize) {
          // new block detected
          String txt = extractText(frame, startX, blockX - spaceBetweenBlock, yF, previousFontSize + 1);
          bld.append(txt);
          bld.append("\n");

          startX = blockX;
          previousFontSize = blockFontSize;
        }
        
        blockFontSize = 0;
        blockX = -1;;
        spaceBetweenBlock ++;
      }
      else if (size < 0) {
        // space betwwen blocks
        spaceBetweenBlock ++;

      }
      else {
        blockFontSize = Math.max(size, blockFontSize);
        spaceBetweenBlock = 0;
        if (startX < 0) {
          startX = x;
        }
        if (blockX < 0) {
          blockX = x;
        }
      }
    }

    if (previousFontSize <= 0) {
      previousFontSize = blockFontSize;
    }
    else if (blockFontSize > 0 && previousFontSize != blockFontSize) {
      String txt = extractText(frame, startX, blockX, yF, previousFontSize + 1);
      bld.append(txt);
      bld.append("\n");

      startX = blockX;
      previousFontSize = blockFontSize;
    }

    String txt = extractText(frame, startX, xT - spaceBetweenBlock, yF, previousFontSize + 1);
    bld.append(txt);

    return bld.toString();
  }


  protected String splitByFontSize_2(Frame frame, int xF, int xT, int yF, int yT) {
    StringBuilder bld = new StringBuilder();

    byte blank = getBlankIndex(frame.getPalette());
    float previousFontSize = -1;
    int blockFontSize = 0;
    int startX = -1;
    int blockX = -1;
    int spaceBetweenBlock = 0;
    for (int x = xF; x < xT; x++) {
      int size = getFontSize(frame, x, yF, yT, blank);
      if (size < 0 && blockFontSize > 0) {
        // blank column after a block

        if (previousFontSize <= 0 || spaceBetweenBlock <= 2
            || Math.abs((previousFontSize - blockFontSize) / previousFontSize) < 0.1) {
          // do nothing : first block or another block with same font size
          previousFontSize = Math.max(previousFontSize, blockFontSize);
        }
        else {
          // new block detected
          String txt = extractText(frame, startX, blockX, yF, (int) previousFontSize + 1);
          bld.append(txt);
          bld.append("\n");

          startX = blockX;
          previousFontSize = blockFontSize;
        }

        // start detection of new block
        blockFontSize = 0;
        spaceBetweenBlock = 1;
        blockX = -1;
      }
      else if (size < 0) {
        // space betwwen blocks
        spaceBetweenBlock ++;

      }
      else {
        blockFontSize = Math.max(size, blockFontSize);
        if (startX < 0) {
          startX = x;
        }
        if (blockX < 0) {
          blockX = x;
        }
      }
    }

    String txt = extractText(frame, startX, xT, yF, (int) previousFontSize + 1);
    bld.append(txt);

    return bld.toString();
  }


  //--------------------------------

  protected boolean hasBorder(Frame frame, int xF, int xT, int yF, int yT) {
    byte b = getSingleColumnColor(frame, xF, yF, yT);
    return (b >= 0
      && b == getSingleColumnColor(frame, xT - 1, yF, yT)
      && b == getSingleRowColor(frame, xF, xT, yF)
      && b == getSingleRowColor(frame, xF, xT, yT - 1));
  }

  protected byte getSingleRowColor(Frame frame, int xF, int xT, int y) {
    byte rowColor = -10;
    for (int x = xF; x < xT; x++) {
      byte color = frame.getColor(x, y);
      if (rowColor==-10) {
        rowColor = color;
      }
      else if (color != rowColor) {
        return -1;
      }
    } 
    return rowColor;
  }

  protected byte getSingleColumnColor(Frame frame, int x, int yF, int yT) {
    byte columnColor = -1;
    for (int y = yF; y < yT; y++) {
      byte color = frame.getColor(x, y);
      if (columnColor == -1) {
        columnColor = color;
      }
      else if (color != columnColor) {
        return -1;
      }
    } 
    return columnColor;
  }

  protected int getFirstColorX(Frame frame, int xF, int xT, int yF, int yT, int y, byte blank) {
    for (int x = xF; x < xT; x++) {
      byte color = frame.getColor(x, y);
      if (color != blank) {
        return x;
      }
    } 
    return -1;
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
