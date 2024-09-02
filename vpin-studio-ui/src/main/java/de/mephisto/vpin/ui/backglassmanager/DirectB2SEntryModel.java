package de.mephisto.vpin.ui.backglassmanager;


import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static de.mephisto.vpin.ui.Studio.client;

public class DirectB2SEntryModel extends BaseLoadingModel<DirectB2S, DirectB2SEntryModel> {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SEntryModel.class);

  // not null when loaded
  DirectB2SData backglassData;

  boolean hasDmd;

  int dmdWidth;
  int dmdHeight;
  int grillHeight;
  int nbScores;

  int hideGrill;
  boolean hideB2SDMD;
  boolean hideBackglass;
  int hideDMD;

  public DirectB2SEntryModel(DirectB2S backglass) {
    super(backglass);
  }

  @Override
  public void load() {
    this.backglassData = client.getBackglassServiceClient().getDirectB2SData(bean);
    if (backglassData != null) {

      this.grillHeight = backglassData.getGrillHeight();

      if (backglassData.isDmdImageAvailable()) {
        try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(backglassData)) {
          Image image = new Image(in);
          this.hasDmd = true;
          this.dmdWidth = (int) image.getWidth();
          this.dmdHeight = (int) image.getHeight();
        }
        catch (IOException ioe) {
          LOG.error("Cannot download DMD image for game " + backglassData.getGameId(), ioe);
        }
      }
      else {
        this.hasDmd = false;
      }

      this.nbScores = backglassData.getScores();

      DirectB2STableSettings tmpTableSettings = null;
      if (backglassData.getGameId() > 0) {
        tmpTableSettings = client.getBackglassServiceClient().getTableSettings(backglassData.getGameId());
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
  public String getName() {
    return bean.getName();
  }

  public int getEmulatorId() {
    return bean.getEmulatorId();
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
    return hasDmd;
  }

  public boolean isFullDmd() {
    return BackglassManagerController.isFullDmd(dmdWidth, dmdHeight);
  }

  public int getDmdWidth() {
    return dmdWidth;
  }

  public int getDmdHeight() {
    return dmdHeight;
  }

  public int getGrillHeight() {
    return grillHeight;
  }

  public int getNbScores() {
    return nbScores;
  }
}
