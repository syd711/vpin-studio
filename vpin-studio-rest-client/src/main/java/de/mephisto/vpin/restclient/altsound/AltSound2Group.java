package de.mephisto.vpin.restclient.altsound;

import java.util.ArrayList;
import java.util.List;

public class AltSound2Group {
  private AltSound2SampleType name;
  private List<AltSound2SampleType> ducks = new ArrayList<>();
  private List<AltSound2SampleType> pauses = new ArrayList<>();
  private List<AltSound2SampleType> stops = new ArrayList<>();
  private Integer groupVol;
  private List<AltSound2DuckingProfile> profiles = new ArrayList<>();

  public AltSound2SampleType getName() {
    return name;
  }

  public void setName(AltSound2SampleType name) {
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

  public Integer getGroupVol() {
    return groupVol;
  }

  public void setGroupVol(Integer groupVol) {
    this.groupVol = groupVol;
  }

  public List<AltSound2DuckingProfile> getProfiles() {
    return profiles;
  }

  public void setProfiles(List<AltSound2DuckingProfile> profiles) {
    this.profiles = profiles;
  }

  public void removeDuck(AltSound2SampleType sampleType) {
    if(this.getDucks().contains(sampleType)) {
      this.getDucks().remove(sampleType);
    }
  }

  public void addDuck(AltSound2SampleType sampleType) {
    if(!this.getDucks().contains(sampleType)) {
      this.ducks.add(sampleType);
    }
  }
}
