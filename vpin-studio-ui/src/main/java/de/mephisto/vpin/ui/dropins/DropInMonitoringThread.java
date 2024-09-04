package de.mephisto.vpin.ui.dropins;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DropInMonitoringThread {

  private final static Logger LOG = LoggerFactory.getLogger(DropInMonitoringThread.class);
  private final AtomicBoolean running = new AtomicBoolean(false);

  private DropInManager dropInManager;

  private Thread monitorThread;
  private File dropinsFolder;
  private WatchService watchService;


  public DropInMonitoringThread(DropInManager dropInManager) {
    this.dropInManager = dropInManager;
  }


  public void startMonitoring() {
    if (this.monitorThread == null && dropinsFolder != null) {
      startMonitor();
    }
  }

  public void stopMonitoring() {
    this.running.set(false);
    try {
      watchService.close();
    }
    catch (IOException e) {
      LOG.error("Failed to close watch service: " + e.getMessage(), e);
    }
    finally {
      monitorThread = null;
    }
  }

  private void startMonitor() {
    this.running.set(true);
    try {
      watchService = FileSystems.getDefault().newWatchService();
    }
    catch (IOException e) {
      LOG.error("Failed to create watch service: " + e.getMessage(), e);
    }
    monitorThread = new Thread(() -> {
      Thread.currentThread().setName("Drop-In Monitoring Thread for \"" + dropinsFolder.getAbsolutePath() + "\"");
      LOG.info("Launched " + Thread.currentThread().getName());
      try {
        final Path path = dropinsFolder.toPath();
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
        while (running.get()) {
          try {
            final WatchKey wk = watchService.take();
            for (WatchEvent<?> event : wk.pollEvents()) {
              if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                final Path filedropped = (Path) event.context();
                File f = new File(dropinsFolder, filedropped.toString());
                if (dropInManager.isNotTempFile(f)) {
                  waitABit(f, 3000);
                  // still exists ?
                  if (f.exists()) {
                    notifyUpdates(f);
                  }
                }
              }
              else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                notifyUpdates(null);
              }
            }
            wk.reset();
          }
          catch (ClosedWatchServiceException e) {
            LOG.info("Terminated closed watch service.");
          }
        }
        LOG.info("Terminated Drop-In Monitoring Thread");
      }
      catch (Exception e) {
        LOG.info("Drop-in monitor failed: " + e.getMessage(), e);
      }
      finally {
        LOG.info(Thread.currentThread().getName() + " terminated.");
      }
    }, "Drop-in folder monitor");
    monitorThread.start();
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

  private void notifyUpdates(@Nullable File file) {
    LOG.info("Noticed drop-in folder update.");
    dropInManager.notifyDropInUpdates(file);
  }

  public void setDropInFolder(@Nullable File dropinsFolder) {
    this.dropinsFolder = dropinsFolder;

    if (monitorThread != null) {
      stopMonitoring();
      monitorThread = null;
    }
    startMonitoring();
  }
}
