package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.restclient.directb2s.DirectB2SAndVersions;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static de.mephisto.vpin.ui.Studio.client;

public class DirectB2SModel extends BaseLoadingModel<DirectB2SAndVersions, DirectB2SModel> {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SModel.class);

  // not null when loaded
  private DirectB2SData backglassData;

  private int hideGrill;
  private boolean hideB2SDMD;
  private boolean hideBackglass;
  private int hideDMD;

  private String resPath;
  private String framePath;


  public DirectB2SModel(DirectB2SAndVersions backglass) {
    super(backglass);
  }

  @Override
  public void load() {
    this.backglassData = client.getBackglassServiceClient().getDirectB2SData(bean.getEmulatorId(), bean.getVersion(0));
    if (backglassData != null) {
      DirectB2sScreenRes screenres = client.getBackglassServiceClient().getScreenRes(backglassData.getEmulatorId(), backglassData.getFilename(), true);
      if (screenres != null) {
        this.resPath = screenres.getScreenresFilePath();
        this.framePath = screenres.getBackgroundFilePath();
      }
    }
    if (getGameId() > 0) {
      DirectB2STableSettings tmpTableSettings = client.getBackglassServiceClient().getTableSettings(getGameId());
      if (tmpTableSettings != null) {
        this.hideGrill = tmpTableSettings.getHideGrill();
        this.hideB2SDMD = tmpTableSettings.isHideB2SDMD();
        this.hideBackglass = tmpTableSettings.isHideB2SBackglass();
        this.hideDMD = tmpTableSettings.getHideDMD();
      }
    }
  }

  public DirectB2SData getBackglassData() {
    return backglassData;
  }

  public DirectB2SAndVersions getBacklass() {
    return getBean();
  }

  @Override
  public boolean sameBean(DirectB2SAndVersions other) {
    return bean.getEmulatorId() == other.getEmulatorId() && StringUtils.equals(bean.getFileName(), other.getFileName());
  }

  public boolean sameBean(int emulatorId, String fileName) {
    return bean.getEmulatorId() == emulatorId && StringUtils.equals(bean.getFileName(), fileName);
  }

  @Override
  public String getName() {
    return bean.getName();
  }

  public int getEmulatorId() {
    return bean.getEmulatorId();
  }

  public int getGameId() {
    return bean.getGameId();
  }

  public String getFileName() {
    return bean.getFileName();
  }

  public boolean isGameAvailable() {
    return bean.isGameAvailable();
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
    return backglassData != null ? backglassData.getNbScores() : 0;
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
