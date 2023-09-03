package de.mephisto.vpin.ui.util;

public class LocalizedValidation {
  private final String label;
  private final String text;

  public LocalizedValidation(String label, String text) {
    this.label = label;
    this.text = text;
  }

  public String getLabel() {
    return label;
  }

  public String getText() {
    return text;
  }
}
