package de.mephisto.vpin.restclient.dmd;

import java.util.ArrayList;
import java.util.List;

public class DMDInfo {

  private int gameId;
  private String gameRom;

  /** Aspect ratio from dmddevice.ini */ 
  private boolean forceAspectRatio;
  /** Selected aspect ratio */ 
  private DMDAspectRatio aspectRatio;
  /** whether save uses registry or ini */ 
  private boolean useRegistry;

  /** Whether dmd position is a per table one (true) or a global settings (false) */
  private boolean locallySaved;

  /** Whether external DMD is used for that table or VpinMame */
  private DMDType dmdType;

  /** When disabled, whether use virtualdmd enabled = false or turn external dmd off in VpinMame settings */
  private boolean disableInVpinMame;
  private boolean disableViaIni;

  private List<DMDInfoZone> zones = new ArrayList<>();

  //private boolean imageCentered;

  /** whether external DMD is supported */ 
  private boolean supportExtDmd;
  /** Whether alphanumeric DMD i ssupported or not */
  private boolean supportAlphaNumericDmd;
  /** when dmd is positioned on grill, dmd size is 0x0, then option to position on dmd should be turned off */ 
  private boolean supportFullDmd;


  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getGameRom() {
		return gameRom;
	}

	public void setGameRom(String gameRom) {
		this.gameRom = gameRom;
	}

	public boolean isForceAspectRatio() {
    return forceAspectRatio;
  }

  public void setForceAspectRatio(boolean forceAspectRatio) {
    this.forceAspectRatio = forceAspectRatio;
  }

  public DMDAspectRatio getAspectRatio() {
    return aspectRatio;
  }

  public void setAspectRatio(DMDAspectRatio aspectRatio) {
    this.aspectRatio = aspectRatio;
  }

  public boolean isUseRegistry() {
		return useRegistry;
	}

	public void setUseRegistry(boolean useRegistry) {
		this.useRegistry = useRegistry;
	}

  public boolean isLocallySaved() {
		return locallySaved;
	}

	public void setLocallySaved(boolean locallySaved) {
		this.locallySaved = locallySaved;
	}

  public DMDType getDMDType() {
    return dmdType;
  }

  public void setDMDType(DMDType dmdType) {
    this.dmdType = dmdType;
  }

  
/*
	public boolean isImageCentered() {
		return imageCentered;
	}

	public void setImageCentered(boolean imageCentered) {
		this.imageCentered = imageCentered;
	}
*/

  public List<DMDInfoZone> getZones() {
    return zones;
  }

  public void setZones(List<DMDInfoZone> zones) {
    this.zones = zones;
  }

  public boolean isDisableInVpinMame() {
    return disableInVpinMame;
  }

  public void setDisableInVpinMame(boolean disableInVpinMame) {
    this.disableInVpinMame = disableInVpinMame;
  }

  public boolean isDisableViaIni() {
    return disableViaIni;
  }

  public void setDisableViaIni(boolean disableViaIni) {
    this.disableViaIni = disableViaIni;
  }

  //-----------------------
  /*
  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    DMDInfo that = (DMDInfo) object;
    return gameId == that.gameId && x == that.x && y == that.y && width == that.width && height == that.height && Objects.equals(onScreen, that.onScreen);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, x, y, width, height);
  }
  */

  public boolean isUseExternalDmd() {
    return dmdType.equals(DMDType.VirtualDMD) || dmdType.equals(DMDType.AlphaNumericDMD);
  }

  public boolean isDisabled() {
    return dmdType == null || dmdType.equals(DMDType.NoDMD);
  }

  public boolean isSupportExtDmd() {
    return supportExtDmd;
  }

  public void setSupportExtDmd(boolean supportExtDmd) {
    this.supportExtDmd = supportExtDmd;
  }

  public boolean isSupportAlphaNumericDmd() {
    return supportAlphaNumericDmd;
  }

  public void setSupportAlphaNumericDmd(boolean supportAlphaNumericDmd) {
    this.supportAlphaNumericDmd = supportAlphaNumericDmd;
  }

  public boolean isSupportFullDmd() {
    return supportFullDmd;
  }

  public void setSupportFullDmd(boolean supportFullDmd) {
    this.supportFullDmd = supportFullDmd;
  }

  //-------------------------------

}
