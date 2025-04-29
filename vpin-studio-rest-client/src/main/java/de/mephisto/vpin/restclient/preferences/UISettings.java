package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;

import java.util.ArrayList;
import java.util.List;

public class UISettings extends JsonSettings {
  private boolean showPlayerScores = true;

  private boolean hideVersions;
  private boolean hideVPSUpdates;

  private boolean vpsAltSound = true;
  private boolean vpsAltColor = true;
  private boolean vpsBackglass = true;
  private boolean vpsPOV = true;
  private boolean vpsPUPPack = true;
  private boolean vpsRom = true;
  private boolean vpsSound = true;
  private boolean vpsToppper = true;
  private boolean vpsTutorial = true;
  private boolean vpsWheel = true;

  private boolean hideComponentWarning;
  private boolean hideVPXStartInfo;
  private boolean hideFrontendLaunchQuestion;

  private boolean hideDismissConfirmations;
  private boolean hideUpdateInfo;
  private boolean hideRatingSyncInfo;

  private boolean autoApplyVpsData = true;

  private String localFavsColor = "#ffcc00";
  private String globalFavsColor = "#cc6600";
  private String justAddedColor = "#FFFFFF";
  private String mostPlayedColor = "#FFFFFF";

  private String defaultUploadMode = UploadType.uploadAndImport.name();

  //open after upload
  private boolean autoEditTableData = true;

  private boolean propperAuthorField = true;
  private boolean propperModField = true;
  private boolean propperVersionField = true;
  private boolean propperVRField = true;

  //sections
  private boolean sectionAltColor = true;
  private boolean sectionAltSound = true;
  private boolean sectionBackglass = true;
  private boolean sectionDMD = true;
  private boolean sectionHighscore = true;
  private boolean sectionAssets = true;
  private boolean sectionPov = false; //lets remove POV be default
  private boolean sectionIni = true;
  private boolean sectionPupPack = true;
  private boolean sectionPlaylists = true;
  private boolean sectionTableData = true;
  private boolean sectionScriptDetails = true;
  private boolean sectionVps = true;
  private boolean sectionVPinMAME = true;

  private boolean tableSidebarVisible = true;
  private boolean competitionsSidebarVisible = true;
  private boolean tournamentsSidebarVisible = true;

  //columns
  private boolean columnAltColor = true;
  private boolean columnAltSound = true;
  private boolean columnBackglass = true;
  private boolean columnRating = true;
  private boolean columnDateAdded = true;
  private boolean columnDateModified = false;
  private boolean columnLauncher = false;
  private boolean columnHighscore = true;
  private boolean columnEmulator = false;
  private boolean columnIni = true;
  private boolean columnRes = true;
  private boolean columnPlaylists = true;
  private boolean columnPov = false;
  private boolean columnPinVol = false;
  private boolean columnPupPack = true;
  private boolean columnRom = true;
  private boolean columnVersion = true;
  private boolean columnVpsStatus = true;
  private boolean columnComment = false;
  private boolean columnPatchVersion = false;

  public boolean isTournamentsSidebarVisible() {
    return tournamentsSidebarVisible;
  }

  public void setTournamentsSidebarVisible(boolean tournamentsSidebarVisible) {
    this.tournamentsSidebarVisible = tournamentsSidebarVisible;
  }

  private AutoFillSettings autoFillSettings = new AutoFillSettings();

  public boolean isColumnRating() {
    return columnRating;
  }

  public void setColumnRating(boolean columnRating) {
    this.columnRating = columnRating;
  }

  public String getDefaultUploadMode() {
    return defaultUploadMode;
  }

  public void setDefaultUploadMode(String defaultUploadMode) {
    this.defaultUploadMode = defaultUploadMode;
  }

  public String getLocalFavsColor() {
    return localFavsColor;
  }

  public void setLocalFavsColor(String localFavsColor) {
    this.localFavsColor = localFavsColor;
  }

  public String getJustAddedColor() {
    return justAddedColor;
  }

  public void setJustAddedColor(String justAddedColor) {
    this.justAddedColor = justAddedColor;
  }

  public String getMostPlayedColor() {
    return mostPlayedColor;
  }

  public void setMostPlayedColor(String mostPlayedColor) {
    this.mostPlayedColor = mostPlayedColor;
  }

  public String getGlobalFavsColor() {
    return globalFavsColor;
  }

  public void setGlobalFavsColor(String globalFavsColor) {
    this.globalFavsColor = globalFavsColor;
  }

  public boolean isHideFrontendLaunchQuestion() {
    return hideFrontendLaunchQuestion;
  }

  public void setHideFrontendLaunchQuestion(boolean hideFrontendLaunchQuestion) {
    this.hideFrontendLaunchQuestion = hideFrontendLaunchQuestion;
  }

  public boolean isColumnComment() {
    return columnComment;
  }

  public void setColumnComment(boolean columnComment) {
    this.columnComment = columnComment;
  }

  public boolean isShowPlayerScores() {
    return showPlayerScores;
  }

  public void setShowPlayerScores(boolean showPlayerScores) {
    this.showPlayerScores = showPlayerScores;
  }

  public AutoFillSettings getAutoFillSettings() {
    return autoFillSettings;
  }

  public void setAutoFillSettings(AutoFillSettings autoFillSettings) {
    this.autoFillSettings = autoFillSettings;
  }

  public boolean isSectionIni() {
    return sectionIni;
  }

  public void setSectionIni(boolean sectionIni) {
    this.sectionIni = sectionIni;
  }

  public boolean isColumnRes() {
    return columnRes;
  }

  public void setColumnRes(boolean columnRes) {
    this.columnRes = columnRes;
  }

  public boolean isColumnPatchVersion() {
    return columnPatchVersion;
  }

  public void setColumnPatchVersion(boolean columnPatchVersion) {
    this.columnPatchVersion = columnPatchVersion;
  }

  public boolean isTablesSidebarVisible() {
    return tableSidebarVisible;
  }

  public void setTablesSidebarVisible(boolean sidebarVisible) {
    this.tableSidebarVisible = sidebarVisible;
  }

  public boolean isCompetitionsSidebarVisible() {
    return competitionsSidebarVisible;
  }

  public void setCompetitionsSidebarVisible(boolean competitionsSidebarVisible) {
    this.competitionsSidebarVisible = competitionsSidebarVisible;
  }

  public boolean isSectionScriptDetails() {
    return sectionScriptDetails;
  }

  public void setSectionScriptDetails(boolean sectionScriptDetails) {
    this.sectionScriptDetails = sectionScriptDetails;
  }

  public boolean isSectionPlaylists() {
    return sectionPlaylists;
  }

  public void setSectionPlaylists(boolean sectionPlaylists) {
    this.sectionPlaylists = sectionPlaylists;
  }

  public boolean isSectionVPinMAME() {
    return sectionVPinMAME;
  }

  public void setSectionVPinMAME(boolean sectionVPinMAME) {
    this.sectionVPinMAME = sectionVPinMAME;
  }

  public boolean isSectionAltColor() {
    return sectionAltColor;
  }

  public void setSectionAltColor(boolean sectionAltColor) {
    this.sectionAltColor = sectionAltColor;
  }

  public boolean isSectionAltSound() {
    return sectionAltSound;
  }

  public void setSectionAltSound(boolean sectionAltSound) {
    this.sectionAltSound = sectionAltSound;
  }

  public boolean isSectionBackglass() {
    return sectionBackglass;
  }

  public void setSectionBackglass(boolean sectionBackglass) {
    this.sectionBackglass = sectionBackglass;
  }

  public boolean isSectionDMD() {
    return sectionDMD;
  }

  public void setSectionDMD(boolean sectionDMD) {
    this.sectionDMD = sectionDMD;
  }

  public boolean isSectionHighscore() {
    return sectionHighscore;
  }

  public void setSectionHighscore(boolean sectionHighscore) {
    this.sectionHighscore = sectionHighscore;
  }

  public boolean isSectionAssets() {
    return sectionAssets;
  }

  public void setSectionAssets(boolean sectionAssets) {
    this.sectionAssets = sectionAssets;
  }

  public boolean isSectionPov() {
    return sectionPov;
  }

  public void setSectionPov(boolean sectionPov) {
    this.sectionPov = sectionPov;
  }

  public boolean isSectionPupPack() {
    return sectionPupPack;
  }

  public void setSectionPupPack(boolean sectionPupPack) {
    this.sectionPupPack = sectionPupPack;
  }

  public boolean isSectionTableData() {
    return sectionTableData;
  }

  public void setSectionTableData(boolean sectionTableData) {
    this.sectionTableData = sectionTableData;
  }

  public boolean isSectionVps() {
    return sectionVps;
  }

  public void setSectionVps(boolean sectionVps) {
    this.sectionVps = sectionVps;
  }

  public boolean isColumnAltColor() {
    return columnAltColor;
  }

  public void setColumnAltColor(boolean columnAltColor) {
    this.columnAltColor = columnAltColor;
  }

  public boolean isColumnAltSound() {
    return columnAltSound;
  }

  public void setColumnAltSound(boolean columnAltSound) {
    this.columnAltSound = columnAltSound;
  }

  public boolean isColumnBackglass() {
    return columnBackglass;
  }

  public void setColumnBackglass(boolean columnBackglass) {
    this.columnBackglass = columnBackglass;
  }

  public boolean isColumnDateAdded() {
    return columnDateAdded;
  }

  public void setColumnDateAdded(boolean columnDateAdded) {
    this.columnDateAdded = columnDateAdded;
  }

  public boolean isColumnLauncher() {
    return columnLauncher;
  }

  public void setColumnLauncher(boolean columnLauncher) {
    this.columnLauncher = columnLauncher;
  }

  public boolean isColumnDateModified() {
    return columnDateModified;
  }

  public void setColumnDateModified(boolean columnDateModified) {
    this.columnDateModified = columnDateModified;
  }

  public boolean isColumnHighscore() {
    return columnHighscore;
  }

  public void setColumnHighscore(boolean columnHighscore) {
    this.columnHighscore = columnHighscore;
  }

  public boolean isColumnEmulator() {
    return columnEmulator;
  }

  public void setColumnEmulator(boolean columnEmulator) {
    this.columnEmulator = columnEmulator;
  }

  public boolean isColumnIni() {
    return columnIni;
  }

  public void setColumnIni(boolean columnIni) {
    this.columnIni = columnIni;
  }

  public boolean isColumnPlaylists() {
    return columnPlaylists;
  }

  public boolean isColumnPinVol() {
    return columnPinVol;
  }

  public void setColumnPinVol(boolean columnPinVol) {
    this.columnPinVol = columnPinVol;
  }

  public void setColumnPlaylists(boolean columnPlaylists) {
    this.columnPlaylists = columnPlaylists;
  }

  public boolean isColumnPov() {
    return columnPov;
  }

  public void setColumnPov(boolean columnPov) {
    this.columnPov = columnPov;
  }

  public boolean isColumnPupPack() {
    return columnPupPack;
  }

  public void setColumnPupPack(boolean columnPupPack) {
    this.columnPupPack = columnPupPack;
  }

  public boolean isColumnRom() {
    return columnRom;
  }

  public void setColumnRom(boolean columnRom) {
    this.columnRom = columnRom;
  }

  public boolean isColumnVersion() {
    return columnVersion;
  }

  public void setColumnVersion(boolean columnVersion) {
    this.columnVersion = columnVersion;
  }

  public boolean isColumnVpsStatus() {
    return columnVpsStatus;
  }

  public void setColumnVpsStatus(boolean columnVpsStatus) {
    this.columnVpsStatus = columnVpsStatus;
  }

  private List<Integer> ignoredEmulatorIds = new ArrayList<>();

  public List<Integer> getIgnoredEmulatorIds() {
    return ignoredEmulatorIds;
  }

  public void setIgnoredEmulatorIds(List<Integer> ignoredEmulatorIds) {
    this.ignoredEmulatorIds = ignoredEmulatorIds;
  }

  private String winNetworkShare;

  public String getWinNetworkShare() {
    return winNetworkShare;
  }

  public void setWinNetworkShare(String winNetworkShare) {
    this.winNetworkShare = winNetworkShare;
  }

  public boolean isAutoApplyVpsData() {
    return autoApplyVpsData;
  }

  public void setAutoApplyVpsData(boolean autoApplyVpsData) {
    this.autoApplyVpsData = autoApplyVpsData;
  }

  public boolean isPropperAuthorField() {
    return propperAuthorField;
  }

  public void setPropperAuthorField(boolean propperAuthorField) {
    this.propperAuthorField = propperAuthorField;
  }

  public boolean isPropperModField() {
    return propperModField;
  }

  public void setPropperModField(boolean propperModField) {
    this.propperModField = propperModField;
  }

  public boolean isPropperVersionField() {
    return propperVersionField;
  }

  public void setPropperVersionField(boolean propperVersionField) {
    this.propperVersionField = propperVersionField;
  }

  public boolean isPropperVRField() {
    return propperVRField;
  }

  public void setPropperVRField(boolean propperVRField) {
    this.propperVRField = propperVRField;
  }

  public boolean isAutoEditTableData() {
    return autoEditTableData;
  }

  public void setAutoEditTableData(boolean autoEditTableData) {
    this.autoEditTableData = autoEditTableData;
  }

  public boolean isVpsWheel() {
    return vpsWheel;
  }

  public void setVpsWheel(boolean vpsWheel) {
    this.vpsWheel = vpsWheel;
  }

  public boolean isVpsAltSound() {
    return vpsAltSound;
  }

  public void setVpsAltSound(boolean vpsAltSound) {
    this.vpsAltSound = vpsAltSound;
  }

  public boolean isVpsAltColor() {
    return vpsAltColor;
  }

  public void setVpsAltColor(boolean vpsAltColor) {
    this.vpsAltColor = vpsAltColor;
  }

  public boolean isVpsBackglass() {
    return vpsBackglass;
  }

  public void setVpsBackglass(boolean vpsBackglass) {
    this.vpsBackglass = vpsBackglass;
  }

  public boolean isVpsPOV() {
    return vpsPOV;
  }

  public void setVpsPOV(boolean vpsPOV) {
    this.vpsPOV = vpsPOV;
  }

  public boolean isVpsPUPPack() {
    return vpsPUPPack;
  }

  public void setVpsPUPPack(boolean vpsPUPPack) {
    this.vpsPUPPack = vpsPUPPack;
  }

  public boolean isVpsRom() {
    return vpsRom;
  }

  public void setVpsRom(boolean vpsRom) {
    this.vpsRom = vpsRom;
  }

  public boolean isVpsSound() {
    return vpsSound;
  }

  public void setVpsSound(boolean vpsSound) {
    this.vpsSound = vpsSound;
  }

  public boolean isVpsToppper() {
    return vpsToppper;
  }

  public void setVpsToppper(boolean vpsToppper) {
    this.vpsToppper = vpsToppper;
  }

  public boolean isVpsTutorial() {
    return vpsTutorial;
  }

  public void setVpsTutorial(boolean vpsTutorial) {
    this.vpsTutorial = vpsTutorial;
  }

  public boolean isHideVersions() {
    return hideVersions;
  }

  public void setHideVersions(boolean hideVersions) {
    this.hideVersions = hideVersions;
  }

  public boolean isHideVPSUpdates() {
    return hideVPSUpdates;
  }

  public void setHideVPSUpdates(boolean hideVPSUpdates) {
    this.hideVPSUpdates = hideVPSUpdates;
  }

  public boolean isHideComponentWarning() {
    return hideComponentWarning;
  }

  public void setHideComponentWarning(boolean hideComponentWarning) {
    this.hideComponentWarning = hideComponentWarning;
  }

  public boolean isHideDismissConfirmations() {
    return hideDismissConfirmations;
  }

  public void setHideDismissConfirmations(boolean hideDismissConfirmations) {
    this.hideDismissConfirmations = hideDismissConfirmations;
  }

  public boolean isHideVPXStartInfo() {
    return hideVPXStartInfo;
  }

  public void setHideVPXStartInfo(boolean hideVPXStartInfo) {
    this.hideVPXStartInfo = hideVPXStartInfo;
  }

  public boolean isHideUpdateInfo() {
    return hideUpdateInfo;
  }

  public void setHideUpdateInfo(boolean hideUpdateInfo) {
    this.hideUpdateInfo = hideUpdateInfo;
  }

  public boolean isHideRatingSyncInfo() {
    return hideRatingSyncInfo;
  }

  public void setHideRatingSyncInfo(boolean hideRatingSyncInfo) {
    this.hideRatingSyncInfo = hideRatingSyncInfo;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.UI_SETTINGS;
  }
}
