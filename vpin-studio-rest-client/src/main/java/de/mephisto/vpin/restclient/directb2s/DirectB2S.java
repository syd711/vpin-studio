package de.mephisto.vpin.restclient.directb2s;

import java.util.Objects;

public class DirectB2S {
  private String name;
  private int emulatorId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DirectB2S)) return false;

    DirectB2S directB2S = (DirectB2S) o;

    if (emulatorId != directB2S.emulatorId) return false;
    return Objects.equals(name, directB2S.name);
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + emulatorId;
    return result;
  }
}
