package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static de.mephisto.vpin.ui.Studio.client;

public class DirectB2SModel extends BaseLoadingModel<DirectB2S, DirectB2SModel> {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SModel.class);

  // not null when loaded
  private DirectB2SData backglassData;

  private int hideGrill;
  private boolean hideB2SDMD;
  private boolean hideBackglass;
  private int hideDMD;

  private String resPath;
  private String framePath;


  public DirectB2SModel(DirectB2S backglass) {
    super(backglass);
  }

  @Override
  public void load() {
    setDirectB2SData(client.getBackglassServiceClient().getDirectB2SData(bean));
  }
  /**
   * Simulate a load and initialize fully
   */
  public void load(DirectB2SData b2sdata) {
    setDirectB2SData(b2sdata);
    setLoaded();
  }
  private void setDirectB2SData(DirectB2SData b2sdata) {
    this.backglassData = b2sdata;
    if (backglassData != null) {
      DirectB2sScreenRes screenres = client.getBackglassServiceClient().getScreenRes(b2sdata.toDirectB2S(), true);
      if (screenres != null) {
        this.resPath = screenres.getScreenresFilePath();
        this.framePath = screenres.getBackgroundFilePath();
      }

      if (backglassData.getGameId() > 0) {
        DirectB2STableSettings tmpTableSettings = client.getBackglassServiceClient().getTableSettings(backglassData.getGameId());
        if (tmpTableSettings != null) {
          this.hideGrill = tmpTableSettings.getHideGrill();
          this.hideB2SDMD = tmpTableSettings.isHideB2SDMD();
          this.hideBackglass = tmpTableSettings.isHideB2SBackglass();
          this.hideDMD = tmpTableSettings.getHideDMD();
        }
      }
    }
  }

  public DirectB2S getBacklass() {
    return getBean();
  }

  @Override
  public boolean sameBean(DirectB2S other) {
    return bean.getEmulatorId() == other.getEmulatorId() && StringUtils.equals(bean.getFileName(), other.getFileName());
  }

  @Override
  public String getName() {
    return bean.getName();
  }

  public int getEmulatorId() {
    return bean.getEmulatorId();
  }

  public int getGameId() {
    return backglassData != null ? backglassData.getGameId() : -1;
  }


  public String getFileName() {
    return bean.getFileName();
  }

  public boolean isVpxAvailable() {
    return bean.isVpxAvailable();
  }

  public int getHideGrill() {
    return hideGrill;
  }

  public boolean isHideB2SDMD() {
    return hideB2SDMD;
  }

  public boolean isHideBackglass() {
    return hideBackglass;
  }

  public int getHideDMD() {
    return hideDMD;
  }

  public boolean hasDmd() {
    return backglassData != null ? backglassData.isDmdImageAvailable() : false;
  }

  public boolean isFullDmd() {
    return backglassData != null ? backglassData.isFullDmd() : false;
  }

  public int getDmdWidth() {
    return backglassData != null ? backglassData.getDmdWidth() : 0;
  }

  public int getDmdHeight() {
    return backglassData != null ? backglassData.getDmdHeight() : 0;
  }

  public int getGrillHeight() {
    return backglassData != null ? backglassData.getGrillHeight() : 0;
  }

  public int getNbScores() {
    return backglassData != null ? backglassData.getScores() : 0;
  }

  public String getResPath() {
    return resPath;
  }

  public String getFramePath() {
    return framePath;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    DirectB2SModel that = (DirectB2SModel) object;
    return Objects.equals(backglassData, that.backglassData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), backglassData);
  }
}
