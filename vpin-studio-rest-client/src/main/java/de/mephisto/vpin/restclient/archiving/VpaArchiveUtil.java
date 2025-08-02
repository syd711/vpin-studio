package de.mephisto.vpin.restclient.archiving;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VpaArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpaArchiveUtil.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static TableDetails readTableDetails(ZipFile file) throws JsonProcessingException {
    String text = readStringFromZip(file, TableDetails.ARCHIVE_FILENAME);
    if (text != null) {
      return objectMapper.readValue(text, TableDetails.class);
    }
    return null;
  }

  public static ArchivePackageInfo readPackageInfo(ZipFile file) throws Exception {
    String text = readStringFromZip(file, ArchivePackageInfo.ARCHIVE_FILENAME);
    if (text != null) {
      return objectMapper.readValue(text, ArchivePackageInfo.class);
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
    String tempDir = System.getProperty("java.io.tmpdir");
    File target = new File(tempDir, fileName);

    try {
      if (target.exists() && !target.delete()) {
        LOG.error("Failed to delete temporary archive file {}", target.getAbsolutePath());
      }
      zipFile.extractFile(fileName, tempDir);
      return FileUtils.readFileToString(target, "utf8");
    }
    catch (Exception e) {
      LOG.error("Failed to read {}: {}", fileName, e.getMessage(), e);
    }
    finally {
      if (target.exists() && !target.delete()) {
        LOG.error("Failed to delete temporary archive file {}", target.getAbsolutePath());
      }
    }
    //ignore
    return null;
  }
}
