package de.mephisto.vpin.server.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.restclient.ArchivePackageInfo;
import de.mephisto.vpin.restclient.TableDetails;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);

  public static void exportDescriptorJson(ArchiveSourceAdapterFileSystem source) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

      List<ArchiveDescriptor> descriptors = source.getArchiveDescriptors();
      List<ArchivePackageInfo> packageInfos = descriptors.stream().map(d -> d.getPackageInfo()).collect(Collectors.toList());
      String manifestString = objectMapper.writeValueAsString(packageInfos);
      File descriptorFile = new File(source.getFolder(), "descriptor.json");
      Files.write(descriptorFile.toPath(), manifestString.getBytes());

      LOG.info("Written " + descriptorFile.getAbsolutePath());
    } catch (IOException e) {
      LOG.error("Error writing export descriptor.json: " + e.getMessage(), e);
    }
  }

  public static String getEmulatorType(File gameFile) {
    String extension = FilenameUtils.getExtension(gameFile.getName());
    if (extension.equals("vpx")) {
      return EmulatorType.VISUAL_PINBALL_X;
    }

    if (extension.equals("fp")) {
      return EmulatorType.FUTURE_PINBALL;
    }

    return EmulatorType.VISUAL_PINBALL_X;
  }
}
