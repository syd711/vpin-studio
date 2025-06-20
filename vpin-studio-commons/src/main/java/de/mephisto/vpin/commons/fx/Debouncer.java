package de.mephisto.vpin.commons.fx;

import java.util.concurrent.*;

import javafx.application.Platform;

public class Debouncer {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final ConcurrentHashMap<String, Future<?>> delayedMap = new ConcurrentHashMap<>();

  /**
   *
   */
  public void debounce(final String key, final Runnable runnable, int ms) {
    debounce(key, runnable, ms, false);
  }

   public void debounce(final String key, final Runnable runnable, int ms, boolean runLater) {
    if (delayedMap.containsKey(key)) {
      delayedMap.get(key).cancel(true);
    }

    final Future<?> prev = delayedMap.put(key, scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        try {
          if (runLater) {
            Platform.runLater(runnable);
          }
          else {
            runnable.run();
          }
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