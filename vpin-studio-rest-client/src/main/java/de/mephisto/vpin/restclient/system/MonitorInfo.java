package de.mephisto.vpin.restclient.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class MonitorInfo {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private boolean portraitMode;
  private boolean primary;
  private int width;
  private int height;
  private int id;
  private double x;
  private double y;
  private String name;
  private double scaling;
  private double minY;


  public double getMinY() {
    return minY;
  }

  public void setMinY(double minY) {
    this.minY = minY;
  }

  @JsonIgnore
  public String getFormattedName() {
    return String.valueOf(name).replaceAll("\\\\", "").replaceAll("\\.", "");
  }

  public double getScaledX() {
    if (scaling != 0 && x != 0) {
      return x / scaling;
    }
    return x;
  }

  public double getScaling() {
    return scaling;
  }

  public void setScaling(double scaling) {
    this.scaling = scaling;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isPortraitMode() {
    return portraitMode;
  }

  public void setPortraitMode(boolean portraitMode) {
    this.portraitMode = portraitMode;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
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

  public int getScaledWidth() {
    if (this.scaling != 0) {
      return (int) (this.getWidth() / this.scaling);
    }
    return this.getWidth();
  }

  public int getScaledHeight() {
    if (this.scaling != 0) {
      return (int) (this.getHeight() / this.scaling);
    }
    return this.getHeight();
  }

  @Override
  public String toString() {
    if (primary) {
      return "Monitor " + (id) + " (primary) [" + getWidth() + "x" + getHeight() + "]";
    }
    return "Monitor " + (id) + " [" + getWidth() + "x" + getHeight() + "]";
  }

  @JsonIgnore
  public String toDetailsString() {
    if (primary) {
      return "Monitor " + (id) + " (primary) [" + getWidth() + "x" + getHeight() + "] Scaled X: " + getScaledX() + ", Scaling: " + scaling + ", MinY: " +getMinY();
    }
    return "Monitor " + (id) + " [" + getWidth() + "x" + getHeight() + "] Scaled X: " + getScaledX() + ", Scaling: " + scaling + ", MinY: " +getMinY();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MonitorInfo)) return false;

    MonitorInfo that = (MonitorInfo) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
