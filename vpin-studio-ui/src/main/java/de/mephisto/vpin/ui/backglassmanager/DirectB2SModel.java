package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SDetail;
import de.mephisto.vpin.restclient.validation.BackglassValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static de.mephisto.vpin.ui.Studio.client;

public class DirectB2SModel extends BaseLoadingModel<DirectB2S, DirectB2SModel> {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SModel.class);

  // not null when loaded
  private DirectB2SDetail backglassDetail;

  public DirectB2SModel(DirectB2S backglass) {
    super(backglass);
  }

  @Override
  public void load() {
    this.backglassDetail = client.getBackglassServiceClient().getDirectB2SDetail(bean.getEmulatorId(), bean.getVersion(0), bean.getGameId());
  }

  public DirectB2S getBacklass() {
    return getBean();
  }

  @Override
  public boolean sameBean(DirectB2S other) {
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

  public int getHideGrill() {
    return backglassDetail != null ? backglassDetail.getHideGrill() : 0;
  }

  public boolean isHideB2SDMD() {
    return backglassDetail != null ? backglassDetail.isHideB2SDMD() : false;
  }

  public boolean isHideBackglass() {
    return backglassDetail != null ? backglassDetail.isHideBackglass() : false;
  }

  public int getHideDMD() {
    return backglassDetail != null ? backglassDetail.getHideDMD() : 0;
  }

  public boolean hasDmd() {
    return backglassDetail != null ? backglassDetail.isDmdImageAvailable() : false;
  }

  public boolean isFullDmd() {
    return backglassDetail != null ? backglassDetail.isFullDmd() : false;
  }

  public int getDmdWidth() {
    return backglassDetail != null ? backglassDetail.getDmdWidth() : 0;
  }

  public int getDmdHeight() {
    return backglassDetail != null ? backglassDetail.getDmdHeight() : 0;
  }

  public int getGrillHeight() {
    return backglassDetail != null ? backglassDetail.getGrillHeight() : 0;
  }

  public int getNbScores() {
    return backglassDetail != null ? backglassDetail.getNbScores() : 0;
  }

  public String getResPath() {
    return backglassDetail != null ? backglassDetail.getResPath() : null;
  }

  public String getFramePath() {
    return backglassDetail != null ? backglassDetail.getFramePath() : null;
  }

  //---------------------------------------

  public int getValidationCode() {
    ValidationState validationState = null;
    if (backglassDetail != null && !backglassDetail.getValidations().isEmpty()) {
      validationState = backglassDetail.getValidations().get(0);
    }
    return validationState != null ? validationState.getCode() : -1;
  }

  public boolean _isGameAvailable() {
    return backglassDetail == null ? false : 
      !ValidationState.contains(backglassDetail.getValidations(), BackglassValidationCode.CODE_NO_GAME);
  }

  //---------------------------------------

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    DirectB2SModel that = (DirectB2SModel) object;
    return Objects.equals(backglassDetail, that.backglassDetail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), backglassDetail);
  }
}
