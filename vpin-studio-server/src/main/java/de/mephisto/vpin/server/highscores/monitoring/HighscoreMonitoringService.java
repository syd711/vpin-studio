package de.mephisto.vpin.server.highscores.monitoring;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.HighscoreService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class HighscoreMonitoringService {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreMonitoringService.class);
  private final AtomicBoolean running = new AtomicBoolean(false);

  @Autowired
  private HighscoreService highscoreService;

  private Thread monitorThread;

  public void startMonitoring(@NonNull Game game) {
    if(monitorThread != null) {
      stopMonitoring();
    }

    File highscoreFile = game.getHighscoreFile();
    if (highscoreFile == null) {
      LOG.info("Cancelled highscore monitoring, no highscore type set.");
      return;
    }
    if (!highscoreFile.exists()) {
      LOG.info("Cancelled highscore monitoring, highscore file \"" + highscoreFile.getAbsolutePath() + "\" does not exist.");
      return;
    }

    startMonitor(highscoreFile, game);
  }

  public void stopMonitoring() {
    if (monitorThread != null) {
      this.running.set(false);
    }
  }

  private void startMonitor(@NonNull File highscoreFile, @NonNull Game game) {
    this.running.set(true);
    monitorThread = new Thread(() -> {
      try {
        Thread.currentThread().setName("Highscore Monitor Thread (" + highscoreFile.getName() + ")");
        LOG.info("Launched \"Highscore Monitor Thread (" + highscoreFile.getName() + ")\"");

        final Path path = highscoreFile.getParentFile().toPath();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
          final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
          while (running.get()) {
            final WatchKey wk = watchService.take();
            for (WatchEvent<?> event : wk.pollEvents()) {
              //we only register "ENTRY_MODIFY" so the context is always a Path.
              final Path changed = (Path) event.context();
              if (changed.endsWith(highscoreFile.getName())) {
                LOG.info("Highscore monitor: " + highscoreFile.getAbsolutePath() + " has changed (" + event.kind() + ")");
              }
            }
            // reset the key
            boolean valid = wk.reset();
            if (!valid) {
              LOG.info("Key has been unregistered");
            }
          }
        }
      } catch (Exception e) {
        LOG.info("Highscore monitor failed: " + e.getMessage(), e);
      } finally {
        LOG.info(Thread.currentThread().getName() + " terminated.");
      }
    });
    monitorThread.start();
  }
}
