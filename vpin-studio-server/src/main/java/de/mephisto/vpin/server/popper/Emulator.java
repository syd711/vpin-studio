package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.commons.EmulatorTypes;

public class Emulator {
  public final static String VISUAL_PINBALL_X = EmulatorTypes.VISUAL_PINBALL_X;
  public final static String PINBALL_FX3 = EmulatorTypes.PINBALL_FX3;
  public final static String FUTURE_PINBALL = EmulatorTypes.FUTURE_PINBALL;

  private String name;
  private int id;
  private String mediaDir;

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
    return VISUAL_PINBALL_X.equals(this.name);
  }
}
