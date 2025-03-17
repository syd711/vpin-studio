package de.mephisto.vpin.restclient.directb2s;

/**
 * Inherits all properties of DirectB2STableSettings + few specific ones
 * see b2sbackglassserver/b2sbackglassserver/Classes/B2SSettings.vb
 */
public class DirectB2ServerSettings extends DirectB2STableSettings {

  private String backglassServerFolder;

  private boolean pluginsOn;
  private boolean showStartupError;
  /**
   * 2 = exe
   * 1 = Standard
   */
  private int defaultStartMode = EXE_START_MODE;

  public int getDefaultStartMode() {
    return defaultStartMode;
  }

  public void setDefaultStartMode(int defaultStartMode) {
    this.defaultStartMode = defaultStartMode;
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

  public String getBackglassServerFolder() {
    return backglassServerFolder;
  }

  public void setBackglassServerFolder(String backglassServerFolder) {
    this.backglassServerFolder = backglassServerFolder;
  }
}
