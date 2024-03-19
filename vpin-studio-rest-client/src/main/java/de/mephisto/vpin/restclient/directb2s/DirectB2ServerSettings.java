package de.mephisto.vpin.restclient.directb2s;

/**
 *
 */
public class DirectB2ServerSettings {
  public final static int STANDARD_START_MODE = 1;
  public final static int EXE_START_MODE = 2;

  private boolean pluginsOn;
  private boolean showStartupError;
  private boolean disableFuzzyMatching;

  /**
   * 2 = exe
   * 1 = Standard
   */
  private int defaultStartMode = DirectB2ServerSettings.EXE_START_MODE;

  public int getDefaultStartMode() {
    return defaultStartMode;
  }

  public void setDefaultStartMode(int defaultStartMode) {
    this.defaultStartMode = defaultStartMode;
  }

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
