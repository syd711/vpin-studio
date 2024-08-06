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
    return splitV(frame, frame.getPlane(), frame.getWidth(), frame.getHeight(), 0, frame.getWidth(), 0, frame.getHeight());
  }

  protected String splitV(Frame frame, byte[] plane, int W, int H, int xF, int xT, int yF, int yT) {
    if (hasBorder(plane, W, H, xF, xT, yF, yT)) {
      return splitV(frame, plane, W, H, xF + 1, xT - 1, yF + 1, yT - 1);
    }

    byte blank = getBlankIndex(frame.getPalette());

    // detect images in frame and remove them
    if (removeImages(plane, W, H, xF, xT, yF, yT, blank)) {
      // for debugging purpose, dump frame post images removal, if any
      if (DEV_MODE) {
        File imgFile = new File(folder, frame.getTimeStamp() + "_.png"); 
        saveImage(plane, W, H, generatePalette(frame.getPalette()), imgFile);
      }
    }

    // else
    StringBuilder bld = new StringBuilder();

    // first detect full single color vertical lines
    byte previousColor = -1;
    int xStart = xF;
    int xStartSplit = -1;
    for (int x = xF; x < xT; x++) {
      byte b = getSingleColumnColor(plane, W, H, xF, xT, yF, yT, x);
      // detection of a full white column
      if (b == blank) {
        // detection of a split
        if (previousColor >= 0 && previousColor != blank) {
          String txt = splitH(frame, plane, W, H, xStart, xStartSplit, yF, yT);
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

    String txt = splitH(frame, plane, W, H, xStart, xT, yF, yT);
    bld.append(txt);
    return bld.toString();
  }

  protected String splitH(Frame frame, byte[] plane, int W, int H, int xF, int xT, int yF, int yT) {
    if (hasBorder(plane, W, H, xF, xT, yF, yT)) {
      return splitH(frame, plane, W, H, xF + 1, xT - 1, yF + 1, yT - 1);
    }
    // else
    StringBuilder bld = new StringBuilder();

    // first detect full single color horizontal lines
    byte idxBlank = getBlankIndex(frame.getPalette());
    int yStart = -1;
    for (int y = yF; y < yT; y++) {
      int x = getFirstColorX(plane, W, H, xF, xT, yF, yT, y, idxBlank);
      // detection of a full white row
      if (x < 0) {
        // detection of a split
        if (yStart >= 0) {
          String txt = splitByFontSize(frame, plane, W, H, xF, xT, yStart, y);
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
      String txt = splitByFontSize(frame, plane, W, H, xF, xT, yStart, yT);
      bld.append(txt);
    }

    return bld.toString();
  }


  protected String splitByFontSize(Frame frame, byte[] plane, int W, int H, int xF, int xT, int yF, int yT) {
    StringBuilder bld = new StringBuilder();

    byte blank = getBlankIndex(frame.getPalette());
    int previousFontSize = -1;
    int blockFontSize = 0;
    int spaceBetweenBlock = 0;
    int startX = -1;
    int blockX = -1;
    for (int x = xF; x < xT; x++) {
      int size = getFontSize(plane, W, H, xF, xT, yF, yT, x, blank);
      if (size < 0 && blockFontSize > 0 && spaceBetweenBlock >= 3) {
        // detection of new block
        if (previousFontSize <= 0) {
          previousFontSize = blockFontSize;
        }
        // second block has different size, 
        else if (previousFontSize != blockFontSize) {
          // new block detected
          String txt = extractRect(frame, plane, W, H, startX, blockX - spaceBetweenBlock, yF, previousFontSize + 1);
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
      String txt = extractRect(frame, plane, W, H, startX, blockX, yF, previousFontSize + 1);
      bld.append(txt);
      bld.append("\n");

      startX = blockX;
      previousFontSize = blockFontSize;
    }

    String txt = extractRect(frame, plane, W, H, startX, xT - spaceBetweenBlock, yF, previousFontSize + 1);
    bld.append(txt);

    return bld.toString();
  }


  protected String splitByFontSize_2(Frame frame, byte[] plane, int W, int H, int xF, int xT, int yF, int yT) {
    StringBuilder bld = new StringBuilder();

    byte blank = getBlankIndex(frame.getPalette());
    float previousFontSize = -1;
    int blockFontSize = 0;
    int startX = -1;
    int blockX = -1;
    int spaceBetweenBlock = 0;
    for (int x = xF; x < xT; x++) {
      int size = getFontSize(plane, W, H, xF, xT, yF, yT, x, blank);
      if (size < 0 && blockFontSize > 0) {
        // blank column after a block

        if (previousFontSize <= 0 || spaceBetweenBlock <= 2
            || Math.abs((previousFontSize - blockFontSize) / previousFontSize) < 0.1) {
          // do nothing : first block or another block with same font size
          previousFontSize = Math.max(previousFontSize, blockFontSize);
        }
        else {
          // new block detected
          String txt = extractRect(frame, plane, W, H, startX, blockX, yF, (int) previousFontSize + 1);
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

    String txt = extractRect(frame, plane, W, H, startX, xT, yF, (int) previousFontSize + 1);
    bld.append(txt);

    return bld.toString();
  }

  protected String extractRect(Frame frame, byte[] plane, int W, int H, int xF, int xT, int yF, int yT) {
    int newW = (xT - xF + 2 * BORDER) * SCALE;
    int newH = (yT - yF + 2 * BORDER) * SCALE;
    int size = 2 * RADIUS + 1;
    size *= size;
    byte blank = getBlankIndex(frame.getPalette());

    // Apply the transformations, add an empty border, rescale and recolor, then blur
    byte[] pixels = crop(plane, W, H, BORDER, xF, xT, yF, yT, SCALE, blank, (byte) size);
    pixels = blur(pixels, newW, newH, RADIUS);

    return extractText(frame, "_" + xF + "x" + yF, pixels, newW, newH);
  }

  //--------------------------------

  protected boolean hasBorder(byte[] plane, int W, int H, int xF, int xT, int yF, int yT) {
    byte b = getSingleColumnColor(plane, W, H, xF, xT, yF, yT, xF);
    return (b >= 0
      && b == getSingleColumnColor(plane, W, H, xF, xT, yF, yT, xT - 1)
      && b == getSingleRowColor(plane, W, H, xF, xT, yF, yT, yF)
      && b == getSingleRowColor(plane, W, H, xF, xT, yF, yT, yT - 1));
  }

  protected byte getSingleRowColor(byte[] plane, int W, int H, int xF, int xT, int yF, int yT, int y) {
    byte rowColor = -10;
    for (int x = xF; x < xT; x++) {
      byte color = plane[y * W + x];
      if (rowColor==-10) {
        rowColor = color;
      }
      else if (color != rowColor) {
        return -1;
      }
    } 
    return rowColor;
  }

  protected byte getSingleColumnColor(byte[] plane, int W, int H, int xF, int xT, int yF, int yT, int x) {
    byte columnColor = -1;
    for (int y = yF; y < yT; y++) {
      byte color = plane[y * W + x];
      if (columnColor == -1) {
        columnColor = color;
      }
      else if (color != columnColor) {
        return -1;
      }
    } 
    return columnColor;
  }

  protected int getFirstColorX(byte[] plane, int W, int H, int xF, int xT, int yF, int yT, int y, byte blank) {
    for (int x = xF; x < xT; x++) {
      byte color = plane[y * W + x];
      if (color != blank) {
        return x;
      }
    } 
    return -1;
  }

  protected int getFontSize(byte[] plane, int W, int H, int xF, int xT, int yF, int yT, int x, byte blank) {
    for (int y = yT - 1; y >= yF; y--) {
      byte color = plane[y * W + x];
      if (color != blank) {
        return y;
      }
    }
    return -1;
  }
}
