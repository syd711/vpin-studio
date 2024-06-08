package de.mephisto.vpin.ui.tables.models;

public class B2SGlowing {
  private int id;
  private String name;

  public B2SGlowing(int id, String name) {
    this.id = id;
    this.name = name;
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
    if (!(o instanceof B2SGlowing)) return false;

    B2SGlowing that = (B2SGlowing) o;
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
