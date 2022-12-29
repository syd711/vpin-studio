package de.mephisto.vpin.restclient.representations;

public class PlaylistRepresentation {
  private int id;
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PlaylistRepresentation that = (PlaylistRepresentation) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
