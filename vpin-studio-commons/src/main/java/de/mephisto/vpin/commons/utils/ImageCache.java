package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.restclient.VPinStudioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
  private final static Logger LOG = LoggerFactory.getLogger(ImageCache.class);

  private final Map<String, byte[]> imageCache = new HashMap<>();

  public InputStream getCachedUrlImage(String imageUrl) {
    try {
      if (!imageCache.containsKey(imageUrl)) {
        URL url = new URL(imageUrl);
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = null;
        is = url.openStream();
        byte[] bytebuff = new byte[4096];
        int n;

        while ((n = is.read(bytebuff)) > 0) {
          bis.write(bytebuff, 0, n);
        }
        is.close();
        bis.close();

        byte[] bytes = bis.toByteArray();
        imageCache.put(imageUrl, bytes);
        LOG.info("Cached image URL " + imageUrl);
      }
    } catch (IOException e) {
      LOG.error("Failed to read image from URL: " + e.getMessage(), e);
    }

    byte[] bytes = imageCache.get(imageUrl);
    return new ByteArrayInputStream(bytes);
  }
}
