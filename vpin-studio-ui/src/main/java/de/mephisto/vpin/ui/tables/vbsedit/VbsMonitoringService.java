package de.mephisto.vpin.ui.tables.vbsedit;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.mephisto.vpin.ui.Studio.client;

public class VbsMonitoringService {
  private final static Logger LOG = LoggerFactory.getLogger(VbsMonitoringService.class);
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean paused = new AtomicBoolean(false);

  private Thread monitorThread;
  private File vbsFolder;

  private Debouncer debouncer = new Debouncer();

  public void startMonitoring(@NonNull File vbsFolder) {
    this.vbsFolder = vbsFolder;
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
        Thread.currentThread().setName("VBS Change Monitor Thread (" + vbsFolder.getAbsolutePath() + ")");
        LOG.info("Launched \"VBS Change Monitor Thread (" + vbsFolder.getAbsolutePath() + ")\"");

        if (!vbsFolder.exists() && !vbsFolder.mkdirs()) {
          LOG.error("Failed to create VBS folder");
        }

        final Path path = vbsFolder.toPath();
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
          final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
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
              if (changed.toFile().getName().endsWith(".vbs")) {
                debouncer.debounce("modified", () -> {
                  try {
                    String name = changed.toFile().getName();
                    int id = -1;
                    if (name.contains("[")) {
                      String idNumber = name.substring(name.lastIndexOf("[") + 1, name.lastIndexOf("]"));
                      id = Integer.parseInt(idNumber);
                    }
                    File changedFile = new File(vbsFolder, changed.toFile().getName());

                    LOG.info("VBS monitor: " + changedFile.getAbsolutePath() + " has changed (" + event.kind() + ")");
                    TextFile textFile = new TextFile(VPinFile.VBScript);
                    textFile.setFileId(id);
                    textFile.setContent(org.apache.commons.io.FileUtils.readFileToString(changedFile, StandardCharsets.UTF_8));
                    client.getTextEditorService().save(textFile);
                    LOG.info("Imported vbs file " + changed.toFile().getAbsolutePath());

                    EventManager.getInstance().notifyTableChange(id, null);
                  } catch (Exception e) {
                    LOG.error("Failed to save vbs file: " + e.getMessage(), e);
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
      } catch (Exception e) {
        LOG.info("Highscore monitor failed: " + e.getMessage(), e);
      } finally {
        LOG.info(Thread.currentThread().getName() + " terminated.");
      }
    });
    monitorThread.start();
  }
}
