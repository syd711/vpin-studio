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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VPinConnection that = (VPinConnection) o;

    return host.equals(that.host);
  }

  @Override
  public int hashCode() {
    return host.hashCode();
  }
}
