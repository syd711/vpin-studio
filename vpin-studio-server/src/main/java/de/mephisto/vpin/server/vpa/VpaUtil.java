package de.mephisto.vpin.server.vpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.EmulatorTypes;
import de.mephisto.vpin.restclient.VpaManifest;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class VpaUtil {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  public static VpaManifest readManifest(File file) {
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

          return objectMapper.readValue(text, VpaManifest.class);
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
      return EmulatorTypes.VISUAL_PINBALL_X;
    }

    if (extension.equals("fp")) {
      return EmulatorTypes.FUTURE_PINBALL;
    }

    return EmulatorTypes.VISUAL_PINBALL_X;
  }
}
