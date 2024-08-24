package de.mephisto.vpin.ui.dropins;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DropInMonitoringThread {

  private final static Logger LOG = LoggerFactory.getLogger(DropInMonitoringThread.class);
  private final AtomicBoolean running = new AtomicBoolean(false);

  private Thread monitorThread;
  private File dropinsFolder;

  public void startMonitoring() {
    if (this.monitorThread == null) {
      startMonitor();
    }
  }

  public void stopMonitoring() {
    this.running.set(false);
  }

  private void startMonitor() {
    this.running.set(true);
    monitorThread = new Thread(() -> {
      Thread.currentThread().setName("Drop-In Monitoring Thread for \"" + dropinsFolder.getAbsolutePath() + "\"");
      LOG.info("Launched " + Thread.currentThread().getName());
      try {
        final Path path = dropinsFolder.toPath();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
          path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
          while (running.get()) {
            final WatchKey wk = watchService.take();
            for (WatchEvent<?> event : wk.pollEvents()) {
              if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                final Path filedropped = (Path) event.context();
                if (isNotTempFile(filedropped)) {
                  File f = new File(dropinsFolder, filedropped.toString());
                  waitABit(f, 3000);
                  if (f.exists()) {
                    notifyUpdates(StandardWatchEventKinds.ENTRY_CREATE);
                  }
                }
              }
              else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                notifyUpdates(StandardWatchEventKinds.ENTRY_DELETE);
              }
            }
            wk.reset();
          }
          LOG.info("Terminated Drop-In Monitoring Thread");
        }
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

  private void notifyUpdates(WatchEvent.Kind<Path> entry) {
    LOG.info("Noticed drop-in folder update.");
    DropInManager.getInstance().notifyDropInUpdates(entry);
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
