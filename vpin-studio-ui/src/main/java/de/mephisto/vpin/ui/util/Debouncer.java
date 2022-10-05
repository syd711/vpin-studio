package de.mephisto.vpin.ui.util;

import java.util.concurrent.*;

public class Debouncer {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final ConcurrentHashMap<String, Future<?>> delayedMap = new ConcurrentHashMap<>();

  /**
   *
   */
  public void debounce(final String key, final Runnable runnable, int ms) {
    if (delayedMap.containsKey(key)) {
      delayedMap.get(key).cancel(true);
    }

    final Future<?> prev = delayedMap.put(key, scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        try {
          runnable.run();
        } finally {
          delayedMap.remove(key);
        }
      }
    }, ms, TimeUnit.MILLISECONDS));
  }

  public void shutdown() {
    scheduler.shutdownNow();
  }
}