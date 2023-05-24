package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.commons.EmulatorType;

public class Emulator {
  public final static String VISUAL_PINBALL_X = EmulatorType.VISUAL_PINBALL_X;
  public final static String VISUAL_PINBALL = EmulatorType.VISUAL_PINBALL;
  public final static String PINBALL_FX3 = EmulatorType.PINBALL_FX3;
  public final static String FUTURE_PINBALL = EmulatorType.FUTURE_PINBALL;
  public final static String PC_GAMES = EmulatorType.PC_GAMES;

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
    return VISUAL_PINBALL_X.equals(this.name) || VISUAL_PINBALL.equals(this.name);
  }
}
