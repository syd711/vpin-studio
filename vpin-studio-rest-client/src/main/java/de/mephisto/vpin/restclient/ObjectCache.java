package de.mephisto.vpin.restclient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectCache<T> {

  private final Map<String, T> objectById = new ConcurrentHashMap<>();

  public T get(String key) {
    if(objectById.containsKey(key)) {
      return objectById.get(key);
    }
    return null;
  }

  public void put(String key, T message) {
    objectById.put(key, message);
  }

  public void invalidate(String key) {
    objectById.remove(key);
  }

  public void invalidateAll() {
    objectById.clear();
  }

  public boolean contains(String messageId) {
    return objectById.containsKey(messageId);
  }
}
