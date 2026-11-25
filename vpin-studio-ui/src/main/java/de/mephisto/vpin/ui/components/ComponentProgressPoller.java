package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.components.ComponentProgressRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.restclient.components.ComponentProgressRepresentation.*;
import static de.mephisto.vpin.ui.Studio.client;

/**
 * Polls the server for the progress of a component action while the blocking REST call runs and shows it in the
 * progress dialog.
 */
public class ComponentProgressPoller implements AutoCloseable {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentProgressPoller.class);

  private static final long INTERVAL_MS = 300;

  private final ComponentType type;
  private final ProgressResultModel progressResultModel;
  private final ScheduledExecutorService executor;
  private double lastProgress = 0;

  public ComponentProgressPoller(ComponentType type, ProgressResultModel progressResultModel) {
    this.type = type;
    this.progressResultModel = progressResultModel;
    this.progressResultModel.setProgress(0);
    this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "Component Progress Poller");
      thread.setDaemon(true);
      return thread;
    });
    this.executor.scheduleWithFixedDelay(this::poll, INTERVAL_MS, INTERVAL_MS, TimeUnit.MILLISECONDS);
  }

  private void poll() {
    try {
      ComponentProgressRepresentation progress = client.getComponentService().getProgress(type);
      if (progress == null || !progress.isActive() || progress.getPhase() == null) {
        return;
      }

      // the bar never goes backwards, e.g. when a poll overlaps the end of the action
      lastProgress = Math.max(lastProgress, progress.getProgress());
      progressResultModel.setProgress(lastProgress);
      progressResultModel.setStatusMessage(toStatusMessage(progress));
    }
    catch (Exception e) {
      LOG.warn("Failed to read progress of {}: {}", type, e.getMessage());
    }
  }

  private static String toStatusMessage(ComponentProgressRepresentation progress) {
    long percent = progress.getTotal() > 0 ? Math.min(100, progress.getDone() * 100 / progress.getTotal()) : 0;
    return switch (progress.getPhase()) {
      case PHASE_FETCH -> Messages.get("component.progress.fetch");
      case PHASE_DOWNLOAD -> progress.getTotal() > 0
          ? Messages.get("component.progress.download", FileUtils.byteCountToDisplaySize(progress.getDone()), FileUtils.byteCountToDisplaySize(progress.getTotal()))
          : Messages.get("component.progress.download_unknown", FileUtils.byteCountToDisplaySize(progress.getDone()));
      case PHASE_COMPARE -> Messages.get("component.progress.compare", percent);
      case PHASE_SIMULATE -> Messages.get("component.progress.simulate", percent);
      case PHASE_EXTRACT -> Messages.get("component.progress.extract", percent);
      default -> "";
    };
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }
}
