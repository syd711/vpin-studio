package de.mephisto.vpin.restclient.util;

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
        mimeType = determineMimeType(suffix);
      }
      return mimeType;
    }
    catch (IOException e) {
      LOG.error("Failed to determine mimetype for " + file.getAbsolutePath() + ": " + e.getMessage(), e);
      return "image/png";
    }
  }

  public static String determineMimeType(String suffix) {
    String mimeType = null;
    if (suffix.endsWith("apng")) {
      mimeType = "image/apng";
    }
    else if (suffix.endsWith("png")) {
      mimeType = "image/png";
    }
    else if (suffix.endsWith("jpg")) {
      mimeType = "image/jpg";
    }
    else if (suffix.endsWith("mp3")) {
      mimeType = "audio.mp3";
    }
    else if (suffix.endsWith("ogg")) {
      mimeType = "audio.ogg";
    }
    else if (suffix.endsWith("mp4")) {
      mimeType = "video/mp4";
    }
    else if (suffix.endsWith("f4v")) {
      mimeType = "video/mp4";
    }
    else if (suffix.endsWith("avi")) {
      mimeType = "video/avi";
    }
    else {
      LOG.error("Failed to determine mime type for " + suffix);
    }
    return mimeType;
  }
}
