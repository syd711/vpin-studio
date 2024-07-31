package de.mephisto.vpin.server.score;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreProcessorFrameSplitter extends  DMDScoreProcessorDelegate {

  //private final static Logger LOG = LoggerFactory.getLogger(DMDScoreProcessorFrameSplitter.class);

  public DMDScoreProcessorFrameSplitter(DMDScoreProcessor proc) {
    super(proc);
  }

  @Override
  public String onFrameReceived(Frame frame, int[] palette) {
    return splitV(frame, palette, frame.getWidth(), frame.getHeight());
  }

  private String splitV(Frame frame, int[] palette, int width, int height) {
    StringBuilder bld = new StringBuilder();

    // first detect full single color vertical lines
    byte[] plane = frame.getPlane();
    byte previousColor = -1;
    int xStart = 0;
    int xStartSplit = -1;
    for (int x = 0; x < width; x++) {
      byte b = getSingleColumnColor(plane, width, height, x);
      // detection of a full white column
      if (b == 0) {
        // detection of a split
        if (previousColor > 0) {
          byte[] planeLeft  = crop(plane, width, height, xStart, xStartSplit, 0, height);
          Frame frameLeft = new Frame(frame.getType(), frame.getName() + "_X" + xStart, frame.getTimeStamp(), planeLeft, xStartSplit - xStart, height);
          String txt = splitH(frameLeft, palette, xStartSplit - xStart, height);
          bld.append(txt);
          // separate left from right
          bld.append("\n");
          xStart = x;
        }
        previousColor = 0;
      }
      else if (b > 0 && (previousColor == 0 || previousColor == b)) {
        previousColor = b;
        xStartSplit = x;
      } else {
        previousColor = -1;
      }
    }

    byte[] planeRemaining  = crop(plane, width, height, xStart, width, 0, height);
    Frame frameRemaining = new Frame(frame.getType(), frame.getName() + "_X" + xStart, frame.getTimeStamp(), planeRemaining, width - xStart, height);
    String txt = splitH(frameRemaining, palette, width - xStart, height);
    bld.append(txt);

    return bld.toString();
  }


  private String splitH(Frame frame, int[] palette, int width, int height) {
    StringBuilder bld = new StringBuilder();

    // first detect full single color horizontal lines
    byte[] plane = frame.getPlane();
    int yStart = -1;
    for (int y = 0; y < height; y++) {
      int x = getFirstColorX(plane, width, height, y);
      // detection of a full white row
      if (x < 0) {
        // detection of a split
        if (yStart >= 0) {
          byte[] planeTop  = crop(plane, width, height, 0, width, yStart, y);
          Frame frameTop = new Frame(frame.getType(), frame.getName() + "_Y" + yStart, frame.getTimeStamp(), planeTop, width, y - yStart);
          String txt = process(frameTop, palette, width, y - yStart);
          bld.append(txt);
          yStart = -1;
        }
      }
      else if (yStart < 0) {
        yStart = y;
      }
    }
    
    if (yStart >= 0) {
      byte[] planeRemaining  = crop(plane, width, height, 0, width, yStart, height);
      Frame frameRemaining = new Frame(frame.getType(), frame.getName() + "_Y" + yStart, frame.getTimeStamp(), planeRemaining, width, height - yStart);
      String txt = process(frameRemaining, palette, width, height - yStart);
      bld.append(txt);
    }

    return bld.toString();
  }

  private String process(Frame frame, int[] palette, int width, int height) {
    return super.onFrameReceived(frame, palette);
  }

  private byte[] crop(byte[] plane, int width, int height, int xFrom, int xTo, int yFrom, int yTo) {

    int newWith = xTo - xFrom;
    int newHeight = yTo - yFrom;
    byte[] croppedPlane = new byte[newWith * newHeight];

    for (int y = yFrom; y < yTo; y++) {
      for (int x = xFrom; x < xTo; x++) {
        croppedPlane[ (y - yFrom) * newWith + (x - xFrom)] = plane[y * width + x];
      }
    }
    return croppedPlane;
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

  protected int getFirstColorX(byte[] plane, int width, int height, int y) {
    for (int x = 0; x < width; x++) {
      byte color = plane[y * width + x];
      if (color > 0) {
        return x;
      }
    } 
    return -1;
  }
}
