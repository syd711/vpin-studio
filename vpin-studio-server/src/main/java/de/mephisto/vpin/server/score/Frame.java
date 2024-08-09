package de.mephisto.vpin.server.score;

import java.util.Arrays;

public class Frame {

  private FrameType type;
  private int timeStamp;
  private int displayTime;
  private byte[] plane;
  private int[] palette;
  private int width;
  private int height;

  public Frame(FrameType type, int timeStamp, byte[] plane, int[] palette, int width, int height) {
    this.type = type;
    this.timeStamp = timeStamp;
    this.plane = plane;
    this.palette = palette;
    this.width = width;
    this.height = height;
  }
  
  public FrameType getType() {
    return type;
  }

  public int getTimeStamp() {
    return timeStamp;
  }

  public int getDisplayTime() {
    return displayTime;
  }
  
  public byte[] getPlane() {
    return plane;
  }

  public int[] getPalette() {return palette;
  }

  public int getWidth() {
    return width;
  }
  public int getHeight() {
    return height;
  }

  public boolean equals(Object o) {
    if (o instanceof Frame) {
      Frame that = (Frame) o;
      return this.type.equals(that.type) && samePlane(this.plane, that.plane);
    }
    return false;
  }

  //--------------------------------------------------------

  /**
   * @param timeStamp Hit when next Frame arrives so that we can calculate display time 
   */
  public void setTimeStampClose(int timeStampClose) {
    this.displayTime = timeStampClose - timeStamp;
  }

  public byte getColor(int x, int y) {
    return plane[y * width + x];
  }
  public void setColor(int x, int y, byte c) {
    plane[y * width + x] = c;
  }

  //--------------------------------------------------------
    
  private boolean samePlane(byte[] plane1, byte[] plane2) {
    return Arrays.equals(plane1, plane2);
  }

  private boolean _samePlane(byte[] plane1, byte[] plane2) {
    if (plane1.length != plane2.length) {
      return false;
    }
    // compare skiping blinking
    int delta = 0;
    for (int idx = 0; idx < plane1.length; idx++) {
      if (plane1[idx] != plane2[idx]) {
        // first delta, capture it
        if (delta == 0) {
          delta = plane1[idx] - plane2[idx];
        }
        // compare, if same delta, image blinking, else different images
        else if (delta != (plane1[idx] - plane2[idx])) {
          return false;
        }
      }
    }
    return true;
  }
}
