package de.mephisto.vpin.server.doftester;

import java.util.Objects;

public class DOFEventCode {
  private final String type;
  private final int number;

  public DOFEventCode(String type, int number) {
    this.type = type;
    this.number = number;
  }

  public String getType() {
    return type;
  }

  public int getNumber() {
    return number;
  }

  @Override
  public String toString() {
    return type + number;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DOFEventCode)) return false;
    DOFEventCode that = (DOFEventCode) o;
    return number == that.number && type.equalsIgnoreCase(that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type.toUpperCase(), number);
  }
}
