package de.mephisto.vpin.restclient.backups;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class VpaArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpaArchiveUtil.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static String PASSWORD = null;

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static void setPassword(String password) {
    PASSWORD = password;
    LOG.info("Updated backup password.");
  }

  public static ZipFile createZipFile(File target) {
    if (PASSWORD == null) {
      return new ZipFile(target);
    }
    return new ZipFile(target, PASSWORD.toCharArray());
  }

  public static TableDetails readTableDetails(File file) throws JsonProcessingException {
    ZipFile zipFile = VpaArchiveUtil.createZipFile(file);
    try {
      String text = readStringFromZip(zipFile, TableDetails.ARCHIVE_FILENAME);
      if (text != null) {
        return objectMapper.readValue(text, TableDetails.class);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read {}: {}", TableDetails.ARCHIVE_FILENAME, e.getMessage(), e);
    }
    finally {
      try {
        zipFile.close();
      }
      catch (IOException e) {
        //ignore
      }
    }
    return null;
  }

  public static DirectB2STableSettings readB2STableSettings(File file) throws JsonProcessingException {
    ZipFile zipFile = VpaArchiveUtil.createZipFile(file);
    try {
      String text = readStringFromZip(zipFile, DirectB2STableSettings.ARCHIVE_FILENAME);
      if (text != null) {
        return objectMapper.readValue(text, DirectB2STableSettings.class);
      }
    }
    catch (Exception e) {
      LOG.info("Failed to read {}, maybe not bundled: {}", DirectB2STableSettings.ARCHIVE_FILENAME, e.getMessage(), e);
    }
    finally {
      try {
        zipFile.close();
      }
      catch (IOException e) {
        //ignore
      }
    }
    return null;
  }

  public static BackupPackageInfo readPackageInfo(File file) throws Exception {
    ZipFile zipFile = createZipFile(file);
    try {
      String text = readStringFromZip(zipFile, BackupPackageInfo.PACKAGE_INFO_JSON_FILENAME);
      if (text != null) {
        return objectMapper.readValue(text, BackupPackageInfo.class);
      }
    }
    finally {
      zipFile.close();
    }
    return null;
  }

  public static BackupMameData readMameData(ZipFile file) {
    try {
      String text = readStringFromZip(file, BackupPackageInfo.REGISTRY_FILENAME);
      if (text != null) {
        return objectMapper.readValue(text, BackupMameData.class);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read Windows registry entries: {}", e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public static String readStringFromZip(ZipFile zipFile, String fileName) throws Exception {
    File target = null;
    try {
      target = extractFile(zipFile, fileName);
      return target != null ? FileUtils.readFileToString(target, "utf8") : null;
    }
    finally {
      if (target != null && target.exists() && !target.delete()) {
        LOG.info("Failed to delete {}", fileName);
      }
    }
  }

  public static boolean extractFolder(@NonNull File archiveFile, @NonNull File targetFolder, @Nullable String archiveFolder, @NonNull List<String> suffixAllowList) {
    ZipFile zipFile = createZipFile(archiveFile);
    try {
      List<FileHeader> fileHeaders = zipFile.getFileHeaders();
      for (FileHeader fileHeader : fileHeaders) {
        if (fileHeader.isDirectory()) {
          //ignore
        }
        else {
          String entryName = fileHeader.getFileName().replaceAll("\\\\", "/");
          String suffix = FilenameUtils.getExtension(entryName);
          boolean isTargetFolder = archiveFolder == null || entryName.startsWith(archiveFolder);
          if (suffixAllowList.isEmpty() || suffixAllowList.contains(suffix.toLowerCase()) || isTargetFolder) {
            String itempath = entryName;
            if (archiveFolder != null) {
              if (!itempath.startsWith(archiveFolder)) {
                continue;
              }
              itempath = itempath.substring(archiveFolder.length());
            }
            File target = new File(targetFolder, itempath);
            // delete existing file and don't simply write in it
            // that would corrupt the file in case conten tto be comied is smaller than previous size
            if (target.isFile() && target.exists() && !target.delete()) {
              LOG.error("Failed to delete existing unrar target file {}", target.getAbsolutePath());
            }
            else {
              // folder creation
              File parent = target.getParentFile();
              if (!parent.isDirectory() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory " + parent);
              }
            }
            zipFile.extractFile(fileHeader, target.getParentFile().getAbsolutePath(), target.getName());
            LOG.info("Encrypted unzipped \"{}\": {}", target.getAbsolutePath(), itempath);
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to extract folder {} from {}: {}", archiveFolder, archiveFile.getAbsolutePath(), e.getMessage(), e);
    }
    finally {
      try {
        zipFile.close();
      }
      catch (IOException e) {
        //ignore
      }
    }
    return false;
  }

  public static boolean extractFile(File archiveFile, File targetFile, String name) {
    ZipFile zipFile = createZipFile(archiveFile);
    FileHeader fileHeader = null;
    try {
      fileHeader = zipFile.getFileHeader(name);
      if (fileHeader == null) {
        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        for (FileHeader header : fileHeaders) {
          if (header.getFileName().endsWith(name)) {
            fileHeader = header;
            break;
          }
        }
      }

      if (fileHeader == null) {
        LOG.error("No matching file {} found in {}", name, archiveFile.getAbsolutePath());
        return false;
      }

      if (targetFile.exists() && targetFile.isFile() && !targetFile.delete()) {
        LOG.error("Failed to delete target extraction file {}", targetFile.getAbsolutePath());
        return false;
      }

      zipFile.extractFile(fileHeader, targetFile.getParentFile().getAbsolutePath(), targetFile.getName());
      if (targetFile.exists()) {
        LOG.info("Written temporary vpa archive file {} / {}", targetFile.getAbsolutePath(), de.mephisto.vpin.restclient.util.FileUtils.readableFileSize(targetFile.length()));
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to extract {} from {}: {}", fileHeader, archiveFile.getAbsolutePath(), e.getMessage(), e);
    }
    finally {
      try {
        zipFile.close();
      }
      catch (IOException e) {
        //ignore
      }
    }
    return false;
  }

  public static File extractFile(ZipFile zipFile, String fileName) {
    try {
      File tmp = File.createTempFile(fileName, ".tmp");
      if (tmp.exists() && !tmp.delete()) {
        LOG.error("Failed to delete {}", tmp.getAbsolutePath());
      }
      FileHeader fileHeader = zipFile.getFileHeader(fileName);
      zipFile.extractFile(fileHeader, tmp.getParentFile().getAbsolutePath(), tmp.getName());
      tmp.deleteOnExit();
      return tmp;
    }
    catch (Exception e) {
      LOG.info("Failed to extract {} from vpa archive {}: {}", fileName, zipFile.getFile().getAbsolutePath(), e.getMessage());
    }
    return null;
  }

  public static String contains(File file, String suffix) {
    ZipFile zipFile = createZipFile(file);
    try {
      List<FileHeader> fileHeaders = zipFile.getFileHeaders();
      String contains = null;
      for (FileHeader fileHeader : fileHeaders) {
        String name = fileHeader.toString();
        if (name.equalsIgnoreCase(suffix)) {
          contains = name;
          break;
        }
      }
      return contains;
    }
    catch (Exception e) {
      LOG.error("Failed to search zip file: {}", e.getMessage(), e);
    }
    finally {
      try {
        zipFile.close();
      }
      catch (IOException e) {
        //ignore
      }
    }
    return null;
  }
}
