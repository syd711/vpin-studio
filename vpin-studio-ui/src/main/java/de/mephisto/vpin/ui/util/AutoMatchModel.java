package de.mephisto.vpin.ui.util;

public class AutoMatchModel {
  private final String displayName;
  private final String id;

  public AutoMatchModel(String displayName, String id) {
    this.displayName = displayName;
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return this.displayName;
  }
}
