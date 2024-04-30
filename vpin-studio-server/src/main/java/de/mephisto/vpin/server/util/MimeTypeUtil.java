package de.mephisto.vpin.server.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MimeTypeUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MimeTypeUtil.class);

  public static String determineMimeType(File file) {
    try {
      String mimeType = Files.probeContentType(file.toPath());
      if (mimeType == null) {
        String suffix = FilenameUtils.getExtension(file.getName()).toLowerCase();
        if (suffix.endsWith("apng")) {
          mimeType = "image/apng";
        }

        if (mimeType == null && suffix.endsWith("png")) {
          mimeType = "image/png";
        }
        else if (mimeType == null && suffix.endsWith("mp4")) {
          mimeType = "video/mp4";
        }
      }
      return mimeType;
    } catch (IOException e) {
      LOG.error("Failed to determine mimetype for " + file.getAbsolutePath() + ": " + e.getMessage(), e);
      return "image/png";
    }
  }
}
