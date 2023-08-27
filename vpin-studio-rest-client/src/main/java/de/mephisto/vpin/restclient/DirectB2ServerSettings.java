package de.mephisto.vpin.restclient;

/**
 * <ArePluginsOn>1</ArePluginsOn>
 * <DefaultStartMode>2</DefaultStartMode>
 * <DisableFuzzyMatching>0</DisableFuzzyMatching>
 * <LogPath>
 * </LogPath>
 * <IsLampsStateLogOn>0</IsLampsStateLogOn>
 * <IsSolenoidsStateLogOn>0</IsSolenoidsStateLogOn>
 * <IsGIStringsStateLogOn>0</IsGIStringsStateLogOn>
 * <IsLEDsStateLogOn>0</IsLEDsStateLogOn>
 * <IsPaintingLogOn>0</IsPaintingLogOn>
 * <IsStatisticsBackglassOn>0</IsStatisticsBackglassOn>
 * <FormToFront>0</FormToFront>
 * <ShowStartupError>0</ShowStartupError>
 * <ScreenshotPath>
 * </ScreenshotPath>
 * <ScreenshotFileType>0</ScreenshotFileType>
 * <HyperpinXMLFile>Unknown</HyperpinXMLFile>
 */
public class DirectB2ServerSettings {
  private boolean pluginsOn;
  private int defaultStartMode;
  private boolean disableFuzzyMatching;
  private boolean lampsStateLogOn;
  private boolean solenoidsStateLogOn;
  private boolean giStringsStateLogOn;
  private boolean ledsStateLogOn;
  private boolean paintingLogOn;
  private boolean statisticsBackglassOn;
  private boolean formToFront;
  private boolean showStartupError;

  public boolean isPluginsOn() {
    return pluginsOn;
  }

  public void setPluginsOn(boolean pluginsOn) {
    this.pluginsOn = pluginsOn;
  }

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

  public boolean isLampsStateLogOn() {
    return lampsStateLogOn;
  }

  public void setLampsStateLogOn(boolean lampsStateLogOn) {
    this.lampsStateLogOn = lampsStateLogOn;
  }

  public boolean isSolenoidsStateLogOn() {
    return solenoidsStateLogOn;
  }

  public void setSolenoidsStateLogOn(boolean solenoidsStateLogOn) {
    this.solenoidsStateLogOn = solenoidsStateLogOn;
  }

  public boolean isGiStringsStateLogOn() {
    return giStringsStateLogOn;
  }

  public void setGiStringsStateLogOn(boolean giStringsStateLogOn) {
    this.giStringsStateLogOn = giStringsStateLogOn;
  }

  public boolean isLedsStateLogOn() {
    return ledsStateLogOn;
  }

  public void setLedsStateLogOn(boolean ledsStateLogOn) {
    this.ledsStateLogOn = ledsStateLogOn;
  }

  public boolean isPaintingLogOn() {
    return paintingLogOn;
  }

  public void setPaintingLogOn(boolean paintingLogOn) {
    this.paintingLogOn = paintingLogOn;
  }

  public boolean isStatisticsBackglassOn() {
    return statisticsBackglassOn;
  }

  public void setStatisticsBackglassOn(boolean statisticsBackglassOn) {
    this.statisticsBackglassOn = statisticsBackglassOn;
  }

  public boolean isFormToFront() {
    return formToFront;
  }

  public void setFormToFront(boolean formToFront) {
    this.formToFront = formToFront;
  }

  public boolean isShowStartupError() {
    return showStartupError;
  }

  public void setShowStartupError(boolean showStartupError) {
    this.showStartupError = showStartupError;
  }
}
