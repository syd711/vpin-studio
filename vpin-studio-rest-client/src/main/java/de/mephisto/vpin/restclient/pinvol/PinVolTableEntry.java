package de.mephisto.vpin.restclient.pinvol;

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

  public String toSettingsString() {
    StringBuilder builder = new StringBuilder(getName());
    builder.append("\t");
    builder.append(getPrimaryVolume());
    builder.append("\t");
    builder.append(getSecondaryVolume());
    builder.append("\t");
    builder.append(getSsfBassVolume());
    builder.append("\t");
    builder.append(getSsfRearVolume());
    builder.append("\t");
    builder.append(getSsfFrontVolume());
    builder.append("\n");
    return builder.toString();
  }
}
