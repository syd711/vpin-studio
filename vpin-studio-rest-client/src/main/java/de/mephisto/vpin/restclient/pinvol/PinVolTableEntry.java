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
}
