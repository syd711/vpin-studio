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
      return this.type.equals(that.type) && Arrays.equals(this.plane, that.plane);
    }
    return false;
  }
}
