package de.mephisto.vpin.ui.util;

import javafx.stage.Screen;

public class ScreenModel {
  private int id;
  private String name;

  public ScreenModel(Screen screen, int index) {
    this.id = index;
    this.name = "Screen " + index + " (" + (int) screen.getVisualBounds().getWidth() + " x " + (int) screen.getVisualBounds().getHeight() + ")";
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ScreenModel)) return false;

    ScreenModel that = (ScreenModel) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return name;
  }
}
