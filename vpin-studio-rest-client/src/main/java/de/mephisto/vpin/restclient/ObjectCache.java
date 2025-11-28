package de.mephisto.vpin.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectCache<T> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Map<String, T> objectById = new ConcurrentHashMap<>();

  public T get(String key) {
    if (objectById.containsKey(key)) {
      return objectById.get(key);
    }
    return null;
  }

  public void put(String key, T message) {
    objectById.put(key, message);
  }

  public void invalidate(String key) {
    objectById.remove(key);
    LOG.info("Invalidated " + key);
  }

  public void invalidateAll() {
    objectById.clear();
  }

  public void invalidateMatching(String prefix) {
    ArrayList<String> keys = new ArrayList<>(objectById.keySet());
    for (String key : keys) {
      if (key.contains(prefix)) {
        invalidate(key);
      }
    }
  }

  public boolean contains(String messageId) {
    return objectById.containsKey(messageId);
  }
}
