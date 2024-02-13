package de.mephisto.vpin.restclient.directb2s;

/**
 *
 */
public class DirectB2ServerSettings {
  private boolean pluginsOn;
  private boolean showStartupError;
  private boolean disableFuzzyMatching;

  public boolean isDisableFuzzyMatching() {
    return disableFuzzyMatching;
  }

  public void setDisableFuzzyMatching(boolean disableFuzzyMatching) {
    this.disableFuzzyMatching = disableFuzzyMatching;
  }

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
