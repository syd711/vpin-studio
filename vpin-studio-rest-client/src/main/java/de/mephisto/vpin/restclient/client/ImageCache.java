package de.mephisto.vpin.restclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Map<String, byte[]> imageCache = new ConcurrentHashMap<>();

  public ImageCache(VPinStudioClient client) {
    super(client);
  }

  public synchronized InputStream getCachedUrlImage(String imageUrl) {
    try {
      if (!imageCache.containsKey(imageUrl)) {
        LOG.info("Loading cached image " + imageUrl);
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
        LOG.info("Cached image URL " + imageUrl + ", cache size: " + imageCache.size());
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to read image from URL: " + e.getMessage());
      return null;
    }

    byte[] bytes = imageCache.get(imageUrl);
    return new ByteArrayInputStream(bytes);
  }


  public void clearCache() {
    int size = this.imageCache.size();
    this.imageCache.clear();
    getRestClient().clearCache();
    LOG.info("Cleared " + size + " resources from cache.");
  }

  public void clearWheelCache() {
    List<String> keys = new ArrayList<>(imageCache.keySet());
    for (String key : keys) {
      if (key.contains("/Wheel")) {
        imageCache.remove(key);
      }
    }
  }

  public boolean containsKey(String name) {
    return imageCache.containsKey(name);
  }

  public void put(String name, byte[] bytes) {
    this.imageCache.put(name, bytes);
  }

  public byte[] get(String name) {
    return imageCache.get(name);
  }

  public void clear(String url) {
    this.imageCache.remove(url);

    for (String key : this.imageCache.keySet()) {
      if (key.contains(url)) {
        this.imageCache.remove(key);
      }
    }
  }
}
