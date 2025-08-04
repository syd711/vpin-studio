package de.mephisto.vpin.restclient.games.descriptors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.assets.AssetType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadDescriptor {
  private final static Logger LOG = LoggerFactory.getLogger(UploadDescriptor.class);

  private UploadType uploadType;
  private String tempFilename;
  private String originalUploadedFileName;
  private String error;
  private int gameId;
  private int emulatorId;
  private boolean folderBasedImport;
  private boolean autoFill;
  private String subfolderName;
  private MultipartFile file;
  private String rom;
  private boolean async;
  private boolean acceptAllAudioAsMusic;
  private String patchVersion;

  private boolean backupRestoreMode = false;

  private List<String> excludedFiles = new ArrayList<>();
  private List<String> excludedFolders = new ArrayList<>();

  private final List<File> tempFiles = new ArrayList<>();

  public boolean isBackupRestoreMode() {
    return backupRestoreMode;
  }

  public void setBackupRestoreMode(boolean backupRestoreMode) {
    this.backupRestoreMode = backupRestoreMode;
  }

  public String getPatchVersion() {
    return patchVersion;
  }

  public void setPatchVersion(String patchVersion) {
    this.patchVersion = patchVersion;
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public boolean isAcceptAllAudioAsMusic() {
    return acceptAllAudioAsMusic;
  }

  public void setAcceptAllAudioAsMusic(boolean acceptAllAudioAsMusic) {
    this.acceptAllAudioAsMusic = acceptAllAudioAsMusic;
  }

  @JsonIgnore
  public List<File> getTempFiles() {
    return tempFiles;
  }

  public void upload() throws Exception {
    String name = FilenameUtils.getBaseName(getOriginalUploadFileName());
    //temp filenames must be larger 3 chars
    if (name.length() < 3) {
      name = name + "_temp";
    }
    String suffix = FilenameUtils.getExtension(getOriginalUploadFileName());
    File uploadTempFile = File.createTempFile(name, "." + suffix);

    setTempFilename(uploadTempFile.getAbsolutePath());

    try {
      if (uploadTempFile.exists() && !uploadTempFile.delete()) {
        throw new UnsupportedOperationException("Failed to delete existing target file " + uploadTempFile.getAbsolutePath());
      }

      BufferedInputStream in = new BufferedInputStream(file.getInputStream());
      FileOutputStream fileOutputStream = new FileOutputStream(uploadTempFile);
      IOUtils.copy(in, fileOutputStream);
      in.close();
      fileOutputStream.close();
      LOG.info("Written uploaded file: " + uploadTempFile.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Failed to store asset: " + e.getMessage(), e);
      throw e;
    }
  }

  public void finalizeUpload() {
    File tempFile = new File(getTempFilename());
    if (!isBackupRestoreMode() && tempFile.exists() && !isAsync()) {
      if (tempFile.delete()) {
        LOG.info("Finalized upload, deleted \"" + tempFile.getAbsolutePath() + "\"");
      }
      else {
        LOG.error("Finalizing upload failed, could not delete \"" + tempFile.getAbsolutePath() + "\"");
      }
    }

    for (File temp : getTempFiles()) {
      if (temp.exists()) {
        if (temp.delete()) {
          LOG.info("Finalized upload, deleted \"" + temp.getAbsolutePath() + "\"");
        }
        else {
          LOG.error("Finalizing upload failed, could not delete \"" + temp.getAbsolutePath() + "\"");
        }
      }
    }
  }

  public boolean isFileAsset(AssetType assetType) {
    String suffix = FilenameUtils.getExtension(originalUploadedFileName);
    return suffix.equalsIgnoreCase(assetType.name().toLowerCase());
  }

  public List<String> getExcludedFiles() {
    return excludedFiles;
  }

  public void setExcludedFiles(List<String> excludedFiles) {
    this.excludedFiles = excludedFiles;
  }

  public List<String> getExcludedFolders() {
    return excludedFolders;
  }

  public void setExcludedFolders(List<String> excludedFolders) {
    this.excludedFolders = excludedFolders;
  }

  @JsonIgnore
  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
    this.originalUploadedFileName = file.getOriginalFilename();
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
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
    return originalUploadedFileName;
  }

  public void setOriginalUploadFileName(String originalUploadedFileName) {
    this.originalUploadedFileName = originalUploadedFileName;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public UploadType getUploadType() {
    return uploadType;
  }

  public void setUploadType(UploadType uploadType) {
    this.uploadType = uploadType;
  }

  public String getTempFilename() {
    return tempFilename;
  }

  public void setTempFilename(String tempFilename) {
    this.tempFilename = tempFilename;
  }
}
