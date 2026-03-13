package de.mephisto.vpin.restclient.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;

public class MimeTypeUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static String determineMimeType(File file) {
    try {
      String mimeType = Files.probeContentType(file.toPath());
      if (mimeType == null) {
        String suffix = FilenameUtils.getExtension(file.getName());
        mimeType = determineMimeType(suffix);
      }
      return mimeType;
    }
    catch (Exception e) {
      LOG.error("Failed to determine mimetype for " + file.getAbsolutePath() + ": " + e.getMessage(), e);
      return "image/png";
    }
  }

  public static String determineMimeType(String suffix) {
    String mimeType = null;
    if (suffix != null) {
      suffix = suffix.toLowerCase();

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
        mimeType = "audio/mpeg";
      }
      else if (suffix.endsWith("ogg")) {
        mimeType = "audio/ogg";
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
      else if (suffix.endsWith("directb2s")) {
        mimeType = "text/xml";
      }
      else {
        LOG.error("Failed to determine mime type for " + suffix);
      }
    }
    return mimeType;
  }

  /** 
   * Return audio, video or image depending on extension
   */
  public static String determineBaseType(String suffix) {
    String mimeType = determineMimeType(suffix);
    return mimeTypeToBaseType(mimeType);
  }

  /** 
   * Return audio, video or image depending on mimeType
   */
  public static String mimeTypeToBaseType(String mimeType) {
    return mimeType != null ? mimeType.substring(0, 5): null;
  }

  public static boolean isImage(String mimeType) {
    return "image".equalsIgnoreCase(mimeTypeToBaseType(mimeType));
  }
  public static boolean isAudio(String mimeType) {
    return "audio".equalsIgnoreCase(mimeTypeToBaseType(mimeType));
  }
  public static boolean isVideo(String mimeType) {
    return "video".equalsIgnoreCase(mimeTypeToBaseType(mimeType));
  }

}
