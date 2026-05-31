package de.mephisto.vpin.server.dmdscore;

import java.util.Arrays;

public class Frame {

  private FrameType type;
  private int timeStamp;
  private int[] plane;
  private int width;
  private int height;
  
  public Frame(FrameType type, int timeStamp, int[] plane, int width, int height) {
    this.type = type;
    this.timeStamp = timeStamp;
    this.plane = plane;
    this.width = width;
    this.height = height;
  }

  public FrameType getType() {
    return type;
  }
  public void setType(FrameType type) {
    this.type = type;
  }
  
  public int getTimeStamp() {
    return timeStamp;
  }
  public void setTimeStamp(int timeStamp) {
    this.timeStamp = timeStamp;
  }
  
  public int[] getPlane() {
    return plane;
  }
  public void _setPlane(int[] plane) {
    this.plane = plane;
  }

  public int getWidth() {
    return width;
  }
  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }
  public void setHeight(int height) {
    this.height = height;
  }

  public boolean equals(Object o) {
    if (o instanceof Frame) {
      Frame that = (Frame) o;
      return this.type.equals(that.type) && Arrays.equals(this.plane, that.plane);
    }
    return false;
  }
}
