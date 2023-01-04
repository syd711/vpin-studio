package de.mephisto.vpin.server.highscores;

import java.io.File;
import java.nio.file.*;

public class HighscoreWatcher {

  private final File userFolder;
  private final File nvRamFolder;

  public HighscoreWatcher(File userFolder, File nvRamFolder) {
    this.userFolder = userFolder;
    this.nvRamFolder = nvRamFolder;
  }

  public void watch() {
    new Thread(() -> {
      try {
        final Path path = userFolder.toPath();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
          final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
          while (true) {
            final WatchKey wk = watchService.take();
            for (WatchEvent<?> event : wk.pollEvents()) {
              final Path changed = (Path) event.context();
              System.out.println(changed + ": " + event.kind());
              if (changed.endsWith("VPReg.stg")) {
                System.out.println("VPReg.stg has changed");
              }
            }

            boolean valid = watchKey.reset();
            if (!valid) {
              break;
            }
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

  }
}
