package de.mephisto.vpin.server.util;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class RequestUtil {
  private final static Logger LOG = LoggerFactory.getLogger(RequestUtil.class);

  public static boolean doGet(String url) {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(500);
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();
      return (200 <= responseCode && responseCode <= 399);
    } catch (IOException exception) {
      return false;
    }
  }

  public static ResponseEntity<byte[]> serializeImage(@Nullable File file) throws Exception {
    if (file != null && file.exists()) {
      try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
        return ResponseEntity.ok()
            .lastModified(file.lastModified())
            .contentType(MediaType.parseMediaType("image/" + FilenameUtils.getExtension(file.getName())))
            .contentLength(file.length())
            .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
            .body(IOUtils.toByteArray(in));
      } catch (Exception e) {
        LOG.error("Failed to serialize image " + file.getAbsolutePath() + ": " + e.getMessage(), e);
        throw e;
      }
      //ignore
    }
    else {
      if(file != null) {
        LOG.info("Image " + file.getAbsolutePath() + " not found.");
      }
    }
    return null;
  }
}
