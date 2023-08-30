package de.mephisto.vpin.restclient.altsound;

public class AltSoundDuckingProfileValue {
  private AltSound2SampleType sampleType;
  private int volume;

  public AltSound2SampleType getSampleType() {
    return sampleType;
  }

  public void setSampleType(AltSound2SampleType sampleType) {
    this.sampleType = sampleType;
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }
}
