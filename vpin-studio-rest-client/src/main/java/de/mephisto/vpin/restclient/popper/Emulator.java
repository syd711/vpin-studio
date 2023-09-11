package de.mephisto.vpin.restclient.popper;

public class Emulator {

  private String name;
  private int id;
  private String mediaDir;
  private boolean visible;

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

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

  public String getMediaDir() {
    return mediaDir;
  }

  public void setMediaDir(String mediaDir) {
    this.mediaDir = mediaDir;
  }

  public boolean isVisualPinball() {
    return isVisualPinball(this.name);
  }

  public static boolean isVisualPinball(String name) {
    return EmulatorType.VISUAL_PINBALL_X.equalsIgnoreCase(name)
        || EmulatorType.VISUAL_PINBALL.equalsIgnoreCase(name)
        || EmulatorType.VISUALPINBALL.equalsIgnoreCase(name)
        || EmulatorType.VISUALPINBALLX.equalsIgnoreCase(name);
  }
}
