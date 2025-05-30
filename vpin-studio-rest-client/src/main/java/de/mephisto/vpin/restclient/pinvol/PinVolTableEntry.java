package de.mephisto.vpin.restclient.pinvol;

import java.util.Objects;

public class PinVolTableEntry {
  private String name;
  private int primaryVolume;
  private int secondaryVolume;
  private int ssfBassVolume;
  private int ssfFrontVolume;
  private int ssfRearVolume;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getPrimaryVolume() {
    return primaryVolume;
  }

  public void setPrimaryVolume(int primaryVolume) {
    this.primaryVolume = primaryVolume;
  }

  public int getSecondaryVolume() {
    return secondaryVolume;
  }

  public void setSecondaryVolume(int secondaryVolume) {
    this.secondaryVolume = secondaryVolume;
  }

  public int getSsfBassVolume() {
    return ssfBassVolume;
  }

  public void setSsfBassVolume(int ssfBassVolume) {
    this.ssfBassVolume = ssfBassVolume;
  }

  public int getSsfFrontVolume() {
    return ssfFrontVolume;
  }

  public void setSsfFrontVolume(int ssfFrontVolume) {
    this.ssfFrontVolume = ssfFrontVolume;
  }

  public int getSsfRearVolume() {
    return ssfRearVolume;
  }

  public void setSsfRearVolume(int ssfRearVolume) {
    this.ssfRearVolume = ssfRearVolume;
  }

  public void applyValues(PinVolTableEntry tableVolume) {
    setSsfBassVolume(tableVolume.getSsfBassVolume());
    setSsfRearVolume(tableVolume.getSsfRearVolume());
    setSsfFrontVolume(tableVolume.getSsfFrontVolume());
    setPrimaryVolume(tableVolume.getPrimaryVolume());
    setSecondaryVolume(tableVolume.getSecondaryVolume());
  }

  public String toSettingsString(int ssfDbLimit) {
    StringBuilder builder = new StringBuilder(getName());
    builder.append("\t");
    builder.append(getPrimaryVolume());
    builder.append("\t");
    builder.append(getSecondaryVolume());
    builder.append("\t");
    builder.append(formatGainValue(getSsfBassVolume(), ssfDbLimit));
    builder.append("\t");
    builder.append(formatGainValue(getSsfRearVolume(), ssfDbLimit));
    builder.append("\t");
    builder.append(formatGainValue(getSsfFrontVolume(), ssfDbLimit));
    builder.append("\n");
    return builder.toString();
  }

  public static int formatGainValue(int i, int ssfDbLimit) {
    try {
      if (i < -ssfDbLimit) {
        i = -ssfDbLimit;
      }
      else if (i > ssfDbLimit) {
        i = ssfDbLimit;
      }
      return i;
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    PinVolTableEntry entry = (PinVolTableEntry) object;
    return Objects.equals(name, entry.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public String toString() {
    return "PinVol Entry \"" + name + "\"";
  }
}
