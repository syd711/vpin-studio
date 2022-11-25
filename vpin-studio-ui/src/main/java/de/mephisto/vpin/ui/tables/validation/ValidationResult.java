package de.mephisto.vpin.ui.tables.validation;

public class ValidationResult {
  private String label;
  private String text;

  public ValidationResult(String label, String text) {
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
