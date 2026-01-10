package de.mephisto.vpin.server.dmdscore;

import java.util.Arrays;

public class Frame {

  private FrameType type;
  private int timeStamp;
  private byte[] plane;

  public Frame(FrameType type, int timeStamp, byte[] plane) {
    this.type = type;
    this.timeStamp = timeStamp;
    this.plane = plane;
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
  
  public byte[] getPlane() {
    return plane;
  }
  public void setPlane(byte[] plane) {
    this.plane = plane;
  }

  public boolean equals(Object o) {
    if (o instanceof Frame) {
      Frame that = (Frame) o;
      return this.type.equals(that.type) && Arrays.equals(this.plane, that.plane);
    }
    return false;
  }

}
