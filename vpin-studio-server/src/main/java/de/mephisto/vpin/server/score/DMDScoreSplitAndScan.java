package de.mephisto.vpin.server.score;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreSplitAndScan extends DMDScoreScannerTessAPI {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreSplitAndScan.class);

  public DMDScoreSplitAndScan() {
    super(true);
  }

  @Override
  public String onFrameReceived(Frame frame, int[] palette) {
    return splitV(frame, frame.getPlane(), frame.getWidth(), frame.getHeight());
  }

  protected String splitV(Frame frame, byte[] plane, int width, int height) {
    StringBuilder bld = new StringBuilder();

    // first detect full single color vertical lines
    byte previousColor = -10;
    int xStart = 0;
    int xStartSplit = -1;
    for (int x = 0; x < width; x++) {
      byte b = getSingleColumnColor(plane, width, height, x);
      // detection of a full white column
      if (b == 0) {
        // initial empty columns, ignore
        if (previousColor == -10) {
          xStart = x;
        }
        // detection of a split
        else if (previousColor > 0) {
          String txt = splitH(frame, plane, width, height, xStart, xStartSplit);
          bld.append(txt);
          // separate left from right
          bld.append("\n");
          xStart = x;
          previousColor = 0;
        }
        // intermediate blank line, do nothing
        else {
          previousColor = 0;
        }
      }
      else if (b > 0 && (previousColor == 0 || previousColor == b)) {
        previousColor = b;
        xStartSplit = x;
      } else {
        previousColor = -1;
      }
    }

    String txt = splitH(frame, plane, width, height, xStart, width);
    bld.append(txt);
    return bld.toString();
  }


  protected String splitH(Frame frame, byte[] plane, int width, int height, int xFrom, int xTo) {
    StringBuilder bld = new StringBuilder();

    // first detect full single color horizontal lines
    int yStart = -1;
    for (int y = 0; y < height; y++) {
      int x = getFirstColorX(plane, width, height, xFrom, xTo, y);
      // detection of a full white row
      if (x < 0) {
        // detection of a split
        if (yStart >= 0) {
          String txt = extractRect(frame, plane, width, height, xFrom, xTo, yStart, y);
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
      String txt = extractRect(frame, plane, width, height, xFrom, xTo, yStart, height);
      bld.append(txt);
    }

    return bld.toString();
  }

  protected String extractRect(Frame frame, byte[] plane, int width, int height, int _xFrom, int _xTo, int _yFrom, int _yTo) {

    int W = (_xTo - _xFrom + 2 * border) * scale;
    int H = (_yTo - _yFrom + 2 * border) * scale;
    int size = 2 * radius + 1;
    size *= size;

    // Apply the transformations, add an empty border, rescale and recolor, then blur
    byte[] pixels = crop(plane, width, height, border, _xFrom, _xTo, _yFrom, _yTo, scale, (byte) size);
    pixels = blur(pixels, W, H, radius);

    return extractText(frame, pixels, W, H );
  }


  protected byte getSingleColumnColor(byte[] plane, int width, int height, int x) {
    byte columnColor = -10;
    for (int y = 0; y < height; y++) {
      byte color = plane[y * width + x];
      if (columnColor==-10) {
        columnColor = color;
      }
      else if (color != columnColor) {
        return -1;
      }
    } 
    return columnColor;
  }

  protected int getFirstColorX(byte[] plane, int width, int height, int xFrom, int xTo, int y) {
    for (int x = xFrom; x < xTo; x++) {
      byte color = plane[y * width + x];
      if (color > 0) {
        return x;
      }
    } 
    return -1;
  }

 
}
