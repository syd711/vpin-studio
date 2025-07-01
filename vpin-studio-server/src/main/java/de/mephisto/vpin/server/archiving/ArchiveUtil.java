package de.mephisto.vpin.server.archiving;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class ArchiveUtil {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);

  public static ArchiveDescriptor readArchiveDescriptor(@NonNull ArchiveSource source, @NonNull File archiveFile) {
    try {
      if (archiveFile.exists()) {
        File packageInfo = new File(archiveFile.getParentFile(), FilenameUtils.getBaseName(archiveFile.getName()) + ".json");
        if (packageInfo.exists()) {
          String json = FileUtils.readFileToString(packageInfo, StandardCharsets.UTF_8);
          ObjectMapper objectMapper = new ObjectMapper();
          objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

          ArchiveDescriptor archiveDescriptor = objectMapper.readValue(json, ArchiveDescriptor.class);
          archiveDescriptor.setSource(source);
          return archiveDescriptor;
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to read archive data from " + archiveFile + ": " + e.getMessage(), e);
    }
    return null;
  }

  public static List<ArchiveDescriptor> readArchiveDescriptors(String json, ArchiveSource archiveSource) {
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

  public static void exportArchiveDescriptor(ArchiveDescriptor descriptor) {
    try {
      File targetFolder = new File(descriptor.getSource().getLocation());
      if(!targetFolder.exists()) {
        targetFolder.mkdirs();
      }

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      File descriptorFile = new File(descriptor.getSource().getLocation(), FilenameUtils.getBaseName(descriptor.getFilename()) + ".json");
      String json = objectMapper.writeValueAsString(descriptor);
      Files.write(descriptorFile.toPath(), json.getBytes());

      LOG.info("Written " + descriptorFile.getAbsolutePath());
    } catch (IOException e) {
      LOG.error("Error writing export archive descriptor for " + descriptor.getFilename() + ": " + e.getMessage(), e);
    }
  }

  public static File exportDescriptorJson(List<ArchiveDescriptor> descriptors, File target) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      String manifestString = objectMapper.writeValueAsString(descriptors);
      Files.write(target.toPath(), manifestString.getBytes());

      LOG.info("Written " + target.getAbsolutePath());
      return target;
    } catch (IOException e) {
      LOG.error("Error writing export descriptor.json: " + e.getMessage(), e);
    }
    return null;
  }

}
