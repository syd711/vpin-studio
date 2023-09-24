package de.mephisto.vpin.restclient.directb2s;

/**
 *
 */
public class DirectB2ServerSettings {
  private boolean pluginsOn;
  private boolean showStartupError;

  public boolean isPluginsOn() {
    return pluginsOn;
  }

  public void setPluginsOn(boolean pluginsOn) {
    this.pluginsOn = pluginsOn;
  }

  public boolean isShowStartupError() {
    return showStartupError;
  }

  public void setShowStartupError(boolean showStartupError) {
    this.showStartupError = showStartupError;
  }
}
