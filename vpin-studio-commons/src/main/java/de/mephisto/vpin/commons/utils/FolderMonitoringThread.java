package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.restclient.util.FileUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FolderMonitoringThread {
  private final static Logger LOG = LoggerFactory.getLogger(FolderMonitoringThread.class);
  private final AtomicBoolean running = new AtomicBoolean(false);

  private FolderChangeListener listener;
  private final boolean modifyEvents;

  private Thread monitorThread;
  private File folder;
  private WatchService watchService;


  public FolderMonitoringThread(FolderChangeListener listener, boolean modifyEvents) {
    this.listener = listener;
    this.modifyEvents = modifyEvents;
  }


  public void startMonitoring() {
    if (this.monitorThread == null && folder != null) {
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

   final Map<WatchKey, Path> keys = new HashMap<>();

    Consumer<Path> register = p -> {
      if (!p.toFile().exists() || !p.toFile().isDirectory()) {
        throw new RuntimeException("folder " + p + " does not exist or is not a directory");
      }
      try {
        Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
            LOG.info("registering " + path + " in watcher service");
            WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            keys.put(watchKey, path);
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (IOException e) {
        LOG.error("Error registering path " + p);
      }
    };

    register.accept(folder.toPath());

    monitorThread = new Thread(() -> {
      Thread.currentThread().setName("Folder Monitoring Thread for \"" + folder + "\"");
      LOG.info("Launched " + Thread.currentThread().getName());
      try {
        while (running.get()) {
          try {
            final WatchKey wk = watchService.take();

            Path dir = keys.get(wk);

            if (dir == null) {
              LOG.info("WatchKey " + wk + " not recognized!");
              continue;
            }

            boolean notifyGlobal = false;
            for (WatchEvent<?> event : wk.pollEvents()) {
              final Path filedropped = (Path) event.context();
              File f = new File(dir.toFile(), filedropped.toString());
              if (f.isFile()) {
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
              else if (f.isDirectory()) {
                if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                  Path absPath = dir.resolve(filedropped);
                  register.accept(absPath);
                  notifyGlobal = true;
                }
              }
              
              if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                notifyGlobal = true;
              }
            }

            if (notifyGlobal) {
              notifyUpdates(null);
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
    }, "Folder Monitor (" + folder.getAbsolutePath() + ")");
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
    listener.notifyFolderChange(folder, file);
  }

  public void setFolder(@Nullable File folder) {
    this.folder = folder;

    if (monitorThread != null) {
      stopMonitoring();
      monitorThread = null;
    }
    startMonitoring();
  }
}
