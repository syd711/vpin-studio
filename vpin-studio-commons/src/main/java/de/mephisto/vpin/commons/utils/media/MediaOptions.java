package de.mephisto.vpin.commons.utils.media;

public class MediaOptions {
  private boolean autoRotate = true;
  private boolean muted = true;

  public boolean isAutoRotate() {
    return autoRotate;
  }

  public void setAutoRotate(boolean autoRotate) {
    this.autoRotate = autoRotate;
  }

  public boolean isMuted() {
    return muted;
  }

  public void setMuted(boolean muted) {
    this.muted = muted;
  }
}
