package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.mephisto.vpin.ui.Studio.client;

public class FileMonitoringService {
  private final static Logger LOG = LoggerFactory.getLogger(FileMonitoringService.class);
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean paused = new AtomicBoolean(false);

  private Thread monitorThread;
  private File monitoringFolder;

  private final Debouncer debouncer = new Debouncer();
  private final Map<String, MonitoredTextFile> monitoredTextFiles = new HashMap<>();

  private static FileMonitoringService INSTANCE = null;

  public static FileMonitoringService getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new FileMonitoringService();
      INSTANCE.startMonitoring();
    }
    return INSTANCE;
  }

  public void monitor(MonitoredTextFile textFile) {
    monitoredTextFiles.put(textFile.getFileId(), textFile);
  }

  private void startMonitoring() {
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
        File monitoringFolder = getMonitoringFolder();
        Thread.currentThread().setName("VPin File Change Monitor Thread (" + monitoringFolder.getAbsolutePath() + ")");
        LOG.info("Launched \"VPin File Change Monitor Thread (" + monitoringFolder.getAbsolutePath() + ")\"");

        if (!monitoringFolder.exists()) {
          LOG.error("Failed to create File Monitoring folder");
        }

        final Path path = monitoringFolder.toPath();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
          path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
          while (running.get()) {
            final WatchKey wk = watchService.take();
            for (WatchEvent<?> event : wk.pollEvents()) {
              if (paused.get()) {
                continue;
              }

              if (!event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                continue;
              }

              final Path changed = (Path) event.context();
              if (changed.toFile().getName().endsWith(".vbs") || changed.toFile().getName().endsWith(".txt")) {
                debouncer.debounce("modified", () -> {
                  try {
                    String name = changed.toFile().getName();
                    String id = null;
                    if (name.contains("[")) {
                      id = name.substring(name.lastIndexOf("[") + 1, name.lastIndexOf("]"));
                    }
                    File changedFile = new File(monitoringFolder, changed.toFile().getName());

                    LOG.info("File Monitor monitor: " + changedFile.getAbsolutePath() + " has changed (" + event.kind() + ")");

                    MonitoredTextFile monitoredTextFile = new MonitoredTextFile(getFileType(name));
                    monitoredTextFile.setFileId(id);
                    if (monitoredTextFiles.containsKey(id)) {
                      monitoredTextFile.setPath(monitoredTextFiles.get(id).getPath());
                    }

                    monitoredTextFile.setContent(org.apache.commons.io.FileUtils.readFileToString(changedFile, StandardCharsets.UTF_8));
                    client.getTextEditorService().save(monitoredTextFile);
                    LOG.info("Imported file " + changed.toFile().getAbsolutePath());

                    try {
                      int i = Integer.parseInt(id);
                      EventManager.getInstance().notifyTableChange(i, null);
                    }
                    catch (NumberFormatException e) {
                      //ignore
                    }
                  }
                  catch (Exception e) {
                    LOG.error("Failed to save monitored file: " + e.getMessage(), e);
                    Platform.runLater(() -> {
                      WidgetFactory.showAlert(Studio.stage, "Error", e.getMessage());
                    });
                  }
                }, 300);
              }
            }
            // reset the key
            boolean valid = wk.reset();
            if (!valid) {
              LOG.info("Key has been unregistered");
            }
          }
          LOG.info("Terminated Monitoring Thread");
        }
      }
      catch (Exception e) {
        LOG.info("File monitor failed: " + e.getMessage(), e);
      }
      finally {
        LOG.info(Thread.currentThread().getName() + " terminated.");
      }
    });
    monitorThread.start();
  }

  private VPinFile getFileType(String name) {
    if (name.endsWith(".vbs")) {
      return VPinFile.VBScript;
    }

    return VPinFile.LOCAL_GAME_FILE;
  }

  public File getMonitoringFolder() {
    if (monitoringFolder == null) {
      File basePath = Updater.getWriteableBaseFolder();
      File txtTemp = new File(basePath, "./resources/text-temp/");
      if (!txtTemp.exists()) {
        txtTemp.mkdirs();
      }
      monitoringFolder = txtTemp;
    }

    return monitoringFolder;
  }
}
