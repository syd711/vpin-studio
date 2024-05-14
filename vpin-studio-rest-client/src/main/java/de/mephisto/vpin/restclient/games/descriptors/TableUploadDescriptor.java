package de.mephisto.vpin.restclient.games.descriptors;

public class TableUploadDescriptor {
  private TableUploadType uploadType;
  private String tempFilename;
  private String originalUploadFileName;
  private String originalUploadedVPXFileName;
  private String error;
  private int gameId;
  private int emulatorId;
  private boolean folderBasedImport;
  private boolean autoFill;
  private String subfolderName;

  private boolean importAltSound;
  private boolean importBackglass;
  private boolean importPupPack;
  private boolean importDMD;
  private boolean importMediaAssets;
  private boolean importRom;

  public boolean isImportAltSound() {
    return importAltSound;
  }

  public void setImportAltSound(boolean importAltSound) {
    this.importAltSound = importAltSound;
  }

  public boolean isImportBackglass() {
    return importBackglass;
  }

  public void setImportBackglass(boolean importBackglass) {
    this.importBackglass = importBackglass;
  }

  public boolean isImportPupPack() {
    return importPupPack;
  }

  public void setImportPupPack(boolean importPupPack) {
    this.importPupPack = importPupPack;
  }

  public boolean isImportDMD() {
    return importDMD;
  }

  public void setImportDMD(boolean importDMD) {
    this.importDMD = importDMD;
  }

  public boolean isImportMediaAssets() {
    return importMediaAssets;
  }

  public void setImportMediaAssets(boolean importMediaAssets) {
    this.importMediaAssets = importMediaAssets;
  }

  public boolean isImportRom() {
    return importRom;
  }

  public void setImportRom(boolean importRom) {
    this.importRom = importRom;
  }

  public String getSubfolderName() {
    return subfolderName;
  }

  public void setSubfolderName(String subfolderName) {
    this.subfolderName = subfolderName;
  }

  public boolean isAutoFill() {
    return autoFill;
  }

  public void setAutoFill(boolean autoFill) {
    this.autoFill = autoFill;
  }

  public boolean isFolderBasedImport() {
    return folderBasedImport;
  }

  public void setFolderBasedImport(boolean folderBasedImport) {
    this.folderBasedImport = folderBasedImport;
  }

  public String getOriginalUploadedVPXFileName() {
    return originalUploadedVPXFileName;
  }

  public void setOriginalUploadedVPXFileName(String originalUploadedVPXFileName) {
    this.originalUploadedVPXFileName = originalUploadedVPXFileName;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public String getOriginalUploadFileName() {
    return originalUploadFileName;
  }

  public void setOriginalUploadFileName(String originalUploadFileName) {
    this.originalUploadFileName = originalUploadFileName;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public TableUploadType getUploadType() {
    return uploadType;
  }

  public void setUploadType(TableUploadType uploadType) {
    this.uploadType = uploadType;
  }

  public String getTempFilename() {
    return tempFilename;
  }

  public void setTempFilename(String tempFilename) {
    this.tempFilename = tempFilename;
  }
}
