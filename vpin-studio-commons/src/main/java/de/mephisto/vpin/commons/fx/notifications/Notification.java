package de.mephisto.vpin.commons.fx.notifications;

import javafx.scene.image.Image;

import java.util.Objects;

public class Notification {

  private String windowTitle;
  private Image image;
  private String title1;
  private String title2;
  private String title3;
  private long durationSec;
  private int margin;
  private boolean desktopMode;
  private boolean showOnEmulatorExit = true;

  public int getMargin() {
    return margin;
  }

  public void setMargin(int margin) {
    this.margin = margin;
  }

  public String getWindowTitle() {
    return windowTitle;
  }

  public void setWindowTitle(String windowTitle) {
    this.windowTitle = windowTitle;
  }

  public boolean isShowOnEmulatorExit() {
    return showOnEmulatorExit;
  }

  public void setShowOnEmulatorExit(boolean showOnEmulatorExit) {
    this.showOnEmulatorExit = showOnEmulatorExit;
  }

  public boolean isDesktopMode() {
    return desktopMode;
  }

  public void setDesktopMode(boolean desktopMode) {
    this.desktopMode = desktopMode;
  }

  public long getDurationSec() {
    return durationSec;
  }

  public void setDurationSec(long durationSec) {
    this.durationSec = durationSec;
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public String getTitle1() {
    return title1;
  }

  public void setTitle1(String title1) {
    this.title1 = title1;
  }

  public String getTitle2() {
    return title2;
  }

  public void setTitle2(String title2) {
    this.title2 = title2;
  }

  public String getTitle3() {
    return title3;
  }

  public void setTitle3(String title3) {
    this.title3 = title3;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification)) return false;

    Notification that = (Notification) o;

    if (!Objects.equals(title1, that.title1)) return false;
    if (!Objects.equals(title2, that.title2)) return false;
    return Objects.equals(title3, that.title3);
  }

  @Override
  public int hashCode() {
    int result = title1 != null ? title1.hashCode() : 0;
    result = 31 * result + (title2 != null ? title2.hashCode() : 0);
    result = 31 * result + (title3 != null ? title3.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Notification \"" + title1 + "\" (" + title2 + ")";
  }
}

