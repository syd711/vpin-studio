package de.mephisto.vpin.restclient.altsound;

import java.util.List;

public class AltSound2Group {
  private String name;
  private List<AltSound2SampleType> ducks;
  private List<AltSound2SampleType> pauses;
  private List<AltSound2SampleType> stops;
  private int groupVol;
  private List<AltSound2DuckingProfile> profiles;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<AltSound2SampleType> getDucks() {
    return ducks;
  }

  public void setDucks(List<AltSound2SampleType> ducks) {
    this.ducks = ducks;
  }

  public List<AltSound2SampleType> getPauses() {
    return pauses;
  }

  public void setPauses(List<AltSound2SampleType> pauses) {
    this.pauses = pauses;
  }

  public List<AltSound2SampleType> getStops() {
    return stops;
  }

  public void setStops(List<AltSound2SampleType> stops) {
    this.stops = stops;
  }

  public int getGroupVol() {
    return groupVol;
  }

  public void setGroupVol(int groupVol) {
    this.groupVol = groupVol;
  }

  public List<AltSound2DuckingProfile> getProfiles() {
    return profiles;
  }

  public void setProfiles(List<AltSound2DuckingProfile> profiles) {
    this.profiles = profiles;
  }
}
