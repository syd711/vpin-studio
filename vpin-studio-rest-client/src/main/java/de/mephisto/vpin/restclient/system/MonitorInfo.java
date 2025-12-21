package de.mephisto.vpin.restclient.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

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
  private double scaledX;

  @JsonIgnore
  public String getFormattedName() {
    return String.valueOf(name).replaceAll("\\\\", "").replaceAll("\\.", "");
  }

  public double getScaledX() {
    return scaledX;
  }

  @JsonIgnore
  public Screen getMatchingScreen() {
    if (isPrimary()) {
      return Screen.getPrimary();
    }

    List<Screen> screens = Screen.getScreens().stream().filter(s -> !Screen.getPrimary().equals(s)).collect(Collectors.toList());
    for (Screen s : screens) {
      double screenX = s.getBounds().getMinX();
      if (screenX == getScaledX()) {
        return s;
      }
    }

    LOG.error("No matching monitor found for " + this);

    return screens.get(0);
  }

  public void setScaledX(double scaledX) {
    this.scaledX = scaledX;
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

  @Override
  public String toString() {
    if (primary) {
      return "Monitor " + (id + 1) + " (primary) [" + getWidth() + "x" + getHeight() + "] Scaled X: " + scaledX + ", Scaling: " + scaling;
    }
    return "Monitor " + (id + 1) + " [" + getWidth() + "x" + getHeight() + "] Scaled X: " + scaledX + ", Scaling: " + scaling;
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
