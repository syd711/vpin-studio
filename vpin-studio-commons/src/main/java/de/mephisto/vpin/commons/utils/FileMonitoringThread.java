package de.mephisto.vpin.commons.utils;

import com.sun.nio.file.ExtendedWatchEventModifier;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.OSUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FileMonitoringThread {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final AtomicBoolean running = new AtomicBoolean(false);

  private final FileChangeListener listener;
  private final boolean modifyEvents;

  private Thread monitorThread;
  private final File file;
  private WatchService watchService;
  private Consumer<Path> register;

  public FileMonitoringThread(FileChangeListener listener, File file, boolean modifyEvents) {
    this.listener = listener;
    this.modifyEvents = modifyEvents;
    this.file = file;
  }


  public void startMonitoring() {
    if (this.monitorThread == null && file != null) {
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

    final Path path = file.getParentFile().toPath();
    final Map<WatchKey, Path> keys = new HashMap<>();

    try {
      WatchKey register = path.register(watchService, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE,
              StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY},
          ExtendedWatchEventModifier.FILE_TREE);
      keys.put(register, path);
    }
    catch (IOException e) {
      LOG.error("Error registering path " + path);
    }

    monitorThread = new Thread(() -> {
      Thread.currentThread().setName("File Monitoring Thread for \"" + file.getAbsolutePath() + "\"");
      try {
        while (running.get()) {
          try {
            final WatchKey wk = watchService.take();

            Path dir = keys.get(wk);
            if (dir == null) {
              LOG.info("WatchKey " + wk + " not recognized!");
              continue;
            }

            for (WatchEvent<?> event : wk.pollEvents()) {
              final Path filedropped = (Path) event.context();
              File f = new File(dir.toFile(), filedropped.toString());
              if (f.isFile() && f.equals(file)) {
                if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                  if (FileUtils.isTempFile(f)) {
                    waitABit(f, 3000);
                  }
                  // still exists ?
                  if (f.exists()) {
                    notifyUpdates(f);
                  }
                }
                else if (modifyEvents && event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                  notifyUpdates(f);
                }
              }

              if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                notifyUpdates(null);
              }
            }

            wk.reset();
          }
          catch (ClosedWatchServiceException e) {
            LOG.info("Terminated closed watch service.");
          }
        }
        LOG.info("Terminated: " + Thread.currentThread().getName());
      }
      catch (Exception e) {
        LOG.info("Folder monitor failed: " + e.getMessage(), e);
      }
      finally {
        LOG.info(Thread.currentThread().getName() + " terminated.");
      }
    }, "File Monitor (" + file.getAbsolutePath() + ")");
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
    if (file != null && file.equals(this.file)) {
      listener.notifyFileChange(file);
    }
  }
}
