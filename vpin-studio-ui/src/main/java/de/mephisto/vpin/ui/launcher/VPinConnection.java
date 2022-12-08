package de.mephisto.vpin.ui.launcher;

import javafx.scene.image.Image;

public class VPinConnection {
  private String host;
  private String name;
  private Image avatar;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Image getAvatar() {
    return avatar;
  }

  public void setAvatar(Image avatar) {
    this.avatar = avatar;
  }
}
