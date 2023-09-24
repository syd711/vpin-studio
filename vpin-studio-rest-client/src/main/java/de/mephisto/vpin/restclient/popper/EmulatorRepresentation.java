package de.mephisto.vpin.restclient.popper;

public class EmulatorRepresentation {

  private String name;
  private boolean visualPinball;
  private String mediaDir;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isVisualPinball() {
    return visualPinball;
  }

  public void setVisualPinball(boolean visualPinball) {
    this.visualPinball = visualPinball;
  }

  public String getMediaDir() {
    return mediaDir;
  }

  public void setMediaDir(String mediaDir) {
    this.mediaDir = mediaDir;
  }
}
