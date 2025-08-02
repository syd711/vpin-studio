package de.mephisto.vpin.restclient.archiving;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class VpaArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpaArchiveUtil.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String PASSWORD = null;

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

  public static ArchivePackageInfo readPackageInfo(File file) throws Exception {
    ZipFile zipFile = createZipFile(file);
    try {
      String text = readStringFromZip(zipFile, ArchivePackageInfo.ARCHIVE_FILENAME);
      if (text != null) {
        return objectMapper.readValue(text, ArchivePackageInfo.class);
      }
    }
    finally {
      zipFile.close();
    }
    return null;
  }

  public static ArchiveMameData readMameData(ZipFile file) {
    try {
      String text = readStringFromZip(file, ArchivePackageInfo.REGISTRY_FILENAME);
      if (text != null) {
        return objectMapper.readValue(text, ArchiveMameData.class);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read Windows registry entries: {}", e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public static String readStringFromZip(ZipFile zipFile, String fileName) {
    try {
      File target = extractFile(zipFile, fileName);
      return FileUtils.readFileToString(target, "utf8");
    }
    catch (Exception e) {
      LOG.error("Failed to read {}: {}", fileName, e.getMessage(), e);
    }
    return null;
  }

  public static boolean extractFile(File archiveFile, File targetFile, String name) {
    ZipFile zipFile = createZipFile(archiveFile);
    try {
      FileHeader fileHeader = zipFile.getFileHeader(name);
      if (targetFile.exists() && targetFile.isFile() && !targetFile.delete()) {
        LOG.error("Failed to delete target extraction file {}", targetFile.getAbsolutePath());
        return false;
      }
//      File t = new File(targetFile.getParentFile(), name);
//      if (t.exists() && t.isFile() && !t.delete()) {
//        LOG.error("Failed to delete target extraction file {}", t.getAbsolutePath());
//        return false;
//      }

      zipFile.extractFile(fileHeader, targetFile.getParentFile().getAbsolutePath(), targetFile.getName());
      if (targetFile.exists()) {
        LOG.info("Written temporary vpa archive file {} / {}", targetFile.getAbsolutePath(), de.mephisto.vpin.restclient.util.FileUtils.readableFileSize(targetFile.length()));
        return true;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to extract from {}: {}", archiveFile.getAbsolutePath(), e.getMessage(), e);
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

  public static File extractFile(ZipFile zipFile, String file) {
    try {
      String tempDir = System.getProperty("java.io.tmpdir");
      File tmp = new File(tempDir, file);
      if (tmp.exists() && !tmp.delete()) {
        LOG.error("Failed to delete {}", tmp.getAbsolutePath());
      }
      FileHeader fileHeader = zipFile.getFileHeader(file);
      zipFile.extractFile(fileHeader, tmp.getParentFile().getAbsolutePath(), tmp.getName());
      tmp.deleteOnExit();
      return tmp;
    }
    catch (Exception e) {
      LOG.error("Failed to extract from {}", zipFile.getFile().getAbsolutePath());
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

  public static void main(String[] args) throws Exception {
    File file = new File("C:\\workspace\\vpin-studio-dev\\resources\\vpa\\Apache (Playmatic 1975).vpa");
    extractFile(file, new File("C:\\vPinball\\pinupsystem\\POPMedia\\Visual Pinball X\\Topper\\Apache (Playmatic 1975).png"), "Screens/Other2/Apache (Playmatic 1975).png");
  }
}
