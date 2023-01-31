package de.mephisto.vpin.connectors.discord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordCache<T> {

  private final Map<Long, T> messageByid = new ConcurrentHashMap<>();

  public T get(long key) {
    if(messageByid.containsKey(key)) {
      return messageByid.get(key);
    }
    return null;
  }

  public void put(long key, T message) {
    messageByid.put(key, message);
  }

  public void invalidate(long key) {
    messageByid.remove(key);
  }

  public void invalidateAll() {
    messageByid.clear();
  }

  public boolean contains(long messageId) {
    return messageByid.containsKey(messageId);
  }
}
