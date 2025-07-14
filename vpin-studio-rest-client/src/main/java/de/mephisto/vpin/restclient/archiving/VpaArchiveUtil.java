package de.mephisto.vpin.restclient.archiving;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VpaArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpaArchiveUtil.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static TableDetails readTableDetails(File file) throws JsonProcessingException {
    String text = readStringFromZip(file, TableDetails.ARCHIVE_FILENAME);
    if (text != null) {
      return objectMapper.readValue(text, TableDetails.class);
    }
    return null;
  }

  public static ArchivePackageInfo readPackageInfo(File file) throws Exception {
    String text = readStringFromZip(file, ArchivePackageInfo.ARCHIVE_FILENAME);
    if (text != null) {
      return objectMapper.readValue(text, ArchivePackageInfo.class);
    }
    return null;
  }

  public static RegistryData readWindowRegistryValues(File file) {
    try {
      String text = readStringFromZip(file, ArchivePackageInfo.REGISTRY_FILENAME);
      if (text != null) {
        return objectMapper.readValue(text, RegistryData.class);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read Windows registry entries: {}", e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public static String readStringFromZip(File file, String fileName) {
    try (ZipFile zipFile = new ZipFile(file)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().equals(fileName)) {
          InputStream stream = zipFile.getInputStream(entry);
          String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
          stream.close();
          return text;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read {}: {}", file.getAbsolutePath(), e.getMessage(), e);
    }
    //ignore
    return null;
  }
}
