package de.mephisto.vpin.restclient.altsound;

import java.util.List;

public class AltSound2DuckingProfile {
  private String name;
  private int id;
  private List<AltSoundDuckingProfileValue> values;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<AltSoundDuckingProfileValue> getValues() {
    return values;
  }

  public void setValues(List<AltSoundDuckingProfileValue> values) {
    this.values = values;
  }
}
