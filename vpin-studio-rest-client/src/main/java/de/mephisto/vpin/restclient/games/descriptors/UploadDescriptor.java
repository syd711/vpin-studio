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

  private TableUploadType uploadType;
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

  private List<AssetType> assetsToImport = new ArrayList<>();

  private List<File> tempFiles = new ArrayList<>();

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  @JsonIgnore
  public List<File> getTempFiles() {
    return tempFiles;
  }

  public void upload() throws Exception {
    String name = FilenameUtils.getBaseName(getOriginalUploadFileName());
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
    if (tempFile.exists()) {
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

  @JsonIgnore
  public boolean isImporting(AssetType assetType) {
    return assetsToImport.contains(assetType);
  }

  public boolean isFileAsset(AssetType assetType) {
    String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
    return suffix.equalsIgnoreCase(assetType.name().toLowerCase());
  }

  public List<AssetType> getAssetsToImport() {
    return assetsToImport;
  }

  public void setAssetsToImport(List<AssetType> assetsToImport) {
    this.assetsToImport = assetsToImport;
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

  public String getOriginalUploadedFileName() {
    return originalUploadedFileName;
  }

  public void setOriginalUploadedFileName(String originalUploadedFileName) {
    this.originalUploadedFileName = originalUploadedFileName;
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
