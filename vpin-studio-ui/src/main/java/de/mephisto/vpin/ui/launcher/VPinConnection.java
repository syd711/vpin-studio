package de.mephisto.vpin.ui.launcher;

import javafx.scene.image.Image;

public class VPinConnection {
  private String host;
  private String name;
  private Image avatar;
  private boolean discovered = false;
  private String macAddress;
  private boolean maybeAsleep = false;
  private Integer connectionId; // Nullable reference to saved configuration

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

  public boolean getDiscovered() {
    return discovered;
  }

  public void setDiscovered(boolean discovered) {
    this.discovered = discovered;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  public boolean isMaybeAsleep() {
    return maybeAsleep;
  }

  public void setMaybeAsleep(boolean maybeAsleep) {
    this.maybeAsleep = maybeAsleep;
  }

  public Integer getConnectionId() {
    return connectionId;
  }

  public void setConnectionId(Integer connectionId) {
    this.connectionId = connectionId;
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

  @Override
  public String toString() {
    return this.host;
  }
}
