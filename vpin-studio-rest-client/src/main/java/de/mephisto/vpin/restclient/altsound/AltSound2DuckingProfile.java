package de.mephisto.vpin.restclient.altsound;

import java.util.List;

public class AltSound2DuckingProfile {
  private int id;
  private List<AltSoundDuckingProfileValue> values;
  private AltSound2SampleType type;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public AltSound2SampleType getType() {
    return type;
  }

  public void setType(AltSound2SampleType type) {
    this.type = type;
  }

  public List<AltSoundDuckingProfileValue> getValues() {
    return values;
  }

  public void setValues(List<AltSoundDuckingProfileValue> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return type.name().toUpperCase() + " Profile " + id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AltSound2DuckingProfile)) return false;

    AltSound2DuckingProfile that = (AltSound2DuckingProfile) o;

    if (id != that.id) return false;
    return type == that.type;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + type.hashCode();
    return result;
  }
}
