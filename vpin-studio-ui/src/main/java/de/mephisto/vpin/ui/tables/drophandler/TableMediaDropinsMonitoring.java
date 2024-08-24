package de.mephisto.vpin.ui.tables.drophandler;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import javafx.application.Platform;

public class TableMediaDropinsMonitoring {

  private final static Logger LOG = LoggerFactory.getLogger(TableMediaDropinsMonitoring.class);
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean paused = new AtomicBoolean(false);

  private TablesController tablesController;

  private Thread monitorThread;
  private File dropinsFolder;
  private boolean deleteFileAfterImport = false;

  public void startMonitoring(File dropinsFolder, TablesController tablesController) {
    this.dropinsFolder = dropinsFolder;
    this.tablesController = tablesController;

    if (monitorThread == null) {
      startMonitor();
    }
  }

  public void setPaused(boolean b) {
    paused.set(b);
  }

  public void stopMonitoring() {
    if (monitorThread != null) {
      this.running.set(false);
    }
  }

  private void startMonitor() {
    this.running.set(true);
    monitorThread = new Thread(() -> {
      try {
        LOG.info("Launched \"Dropins folder monitoring (" + dropinsFolder.getAbsolutePath() + ")\"");

        if (!dropinsFolder.exists() && !dropinsFolder.mkdirs()) {
          LOG.error("Failed to create dropins folder");
        }

        final Path path = dropinsFolder.toPath();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
          path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
          while (running.get()) {
            final WatchKey wk = watchService.take();
            for (WatchEvent<?> event : wk.pollEvents()) {
              if (paused.get()) {
                continue;
              }
              if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                final Path filedropped = (Path) event.context();
                if (isNotTempFile(filedropped)) {
                  File f = new File(dropinsFolder, filedropped.toString());
                  waitABit(f, 5000);
                  if (f.exists()) {
                    dispatchDroppedFile(f);
                    //f.delete();
                  }
                }
              }
            }
            wk.reset();
          }
          LOG.info("Terminated Monitoring Thread");
        }
      }
      catch (Exception e) {
        LOG.info("Highscore monitor failed: " + e.getMessage(), e);
      }
      finally {
        LOG.info(Thread.currentThread().getName() + " terminated.");
      }
    }, "Dropins folder monitor");
    monitorThread.start();
  }

  private boolean isNotTempFile(Path filedropped) {
    String filename = filedropped.toString();
    return !StringUtils.endsWithIgnoreCase(filename, "tmp")
        && !StringUtils.endsWithIgnoreCase(filename, "crdownload");
  }

  /**
   * taken from https://stackoverflow.com/questions/3369383/java-watching-a-directory-to-move-large-files
   */
  private void waitABit(File file, long delayMillis) throws Exception {
    LOG.info("File detected: '{}'", file.getAbsolutePath());
    boolean locked = true;
    while (locked) {
      // creating RAF will throw FileNotFoundException is used
      try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
        //FileChannel channel = raf.getChannel();
        //channel.lock(0, Long.MAX_VALUE, true);

        // just to make sure everything was copied, goes to the last byte
        raf.seek(file.length());

        locked = false;
      }
      catch (Exception e) {
        locked = file.exists();
        if (locked) {
          LOG.info("File locked: '{}'", file.getAbsolutePath());
          Thread.sleep(delayMillis); // waits some time
        }
        else {
          LOG.info("File was deleted while copying: '{}'", file.getAbsolutePath());
        }
      }
    }
  }

  private void dispatchDroppedFile(File file) {
    LOG.info("File ready for import: '{}", file.getAbsolutePath());
    Platform.runLater(() -> {
      // go back on javafx Thread for installation
      GameRepresentation selection = tablesController.getTableOverviewController().getSelection();
      UploadAnalysisDispatcher.dispatch(file, selection);
      if (deleteFileAfterImport) {
        file.delete();
      }
      LOG.info("File imported: '{}", file.getAbsolutePath());
    });
  }
}
