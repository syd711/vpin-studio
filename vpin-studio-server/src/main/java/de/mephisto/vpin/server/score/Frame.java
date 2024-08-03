package de.mephisto.vpin.server.score;

import java.util.Arrays;

public class Frame {

  private FrameType type;
  private String name;
  private int timeStamp;
  private byte[] plane;
  private int width;
  private int height;

  public Frame(FrameType type, String name, int timeStamp, byte[] plane, int width, int height) {
    this.type = type;
    this.name = name;
    this.timeStamp = timeStamp;
    this.plane = plane;
    this.width = width;
    this.height = height;
  }

  public FrameType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public int getTimeStamp() {
    return timeStamp;
  }
  
  public byte[] getPlane() {
    return plane;
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
