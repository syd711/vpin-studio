package de.mephisto.vpin.server.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.restclient.TableManifest;
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

public class VpaUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  public static void exportDescriptorJson(VpaSourceAdapterFileSystem source) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

      List<VpaDescriptor> descriptors = source.getVpaDescriptors();
      List<TableManifest> manifests = descriptors.stream().map(d -> d.getManifest()).collect(Collectors.toList());
      String manifestString = objectMapper.writeValueAsString(manifests);
      File descriptorFile = new File(source.getFolder(), "descriptor.json");
      Files.write(descriptorFile.toPath(), manifestString.getBytes());

      LOG.info("Written " + descriptorFile.getAbsolutePath());
    } catch (IOException e) {
      LOG.error("Error writing export descriptor.json: " + e.getMessage(), e);
    }
  }

  public static List<TableManifest> readManifests(String json) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      return Arrays.asList(objectMapper.readValue(json, TableManifest[].class));
    } catch (IOException e) {
      LOG.error("Failed to read manifest data from json\n" + json + ": " + e.getMessage(), e);
    }
    return null;
  }

  public static TableManifest readManifest(File file) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

      ZipFile zipFile = new ZipFile(file);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.getName().equals("manifest.json")) {
          InputStream stream = zipFile.getInputStream(entry);
          String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
          stream.close();
          zipFile.close();

          return objectMapper.readValue(text, TableManifest.class);
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to read manifest information from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
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
