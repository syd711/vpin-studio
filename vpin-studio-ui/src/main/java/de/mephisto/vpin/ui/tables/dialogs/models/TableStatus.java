package de.mephisto.vpin.ui.tables.dialogs.models;

public class TableStatus {
  public final int value;
  public final String label;

  public TableStatus(int value, String label) {
    this.value = value;
    this.label = label;
  }

  public int getValue() {
    return value;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TableStatus)) return false;

    TableStatus that = (TableStatus) o;

    return value == that.value;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public String toString() {
    return label;
  }
}