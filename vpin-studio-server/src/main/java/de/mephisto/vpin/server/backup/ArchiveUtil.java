package de.mephisto.vpin.server.backup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.EmulatorType;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class ArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);


  public static List<ArchiveDescriptor> readArchiveDecriptors(String json, ArchiveSource archiveSource) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      List<ArchiveDescriptor> archiveDescriptors = Arrays.asList(objectMapper.readValue(json, ArchiveDescriptor[].class));
      for (ArchiveDescriptor archiveDescriptor : archiveDescriptors) {
        archiveDescriptor.setSource(archiveSource);
      }
      return archiveDescriptors;
    } catch (IOException e) {
      LOG.error("Failed to read archive list data from json\n" + json + ": " + e.getMessage(), e);
    }
    return null;
  }

  public static void exportDescriptorJson(ArchiveSourceAdapterFileSystem source) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      List<ArchiveDescriptor> descriptors = source.getArchiveDescriptors();
      String manifestString = objectMapper.writeValueAsString(descriptors);
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
