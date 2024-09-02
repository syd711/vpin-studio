package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.altsound.AltSoundServiceClient;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Future;

import static de.mephisto.vpin.restclient.jobs.JobType.ALTSOUND_INSTALL;

public class AltSoundUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final int gameId;
  private final File file;
  private final int emulatorId;
  private final String rom;

  private Future<UploadDescriptor> currentUploadFuture;

  public AltSoundUploadProgressModel(int gameId, String title, File file, int emulatorId, String rom) {
    super(file, title);
    this.gameId = gameId;
    this.file = file;
    this.emulatorId = emulatorId;
    this.rom = rom;
    this.iterator = Collections.singletonList(this.file).iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(File file) {
    return "Uploading " + file.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      // UploadDescriptor result = Studio.client.getAltSoundService().uploadAltSound(next, emulatorId, percent -> progressResultModel.setProgress(percent));
      currentUploadFuture = Studio.client.getAltSoundService().uploadAltSoundFuture(next, emulatorId, percent -> progressResultModel.setProgress(percent));
      UploadDescriptor result = currentUploadFuture.get();

      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }

      Platform.runLater(() -> {
        EventManager.getInstance().notifyTableChange(gameId, rom);
      });
      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      if (!currentUploadFuture.isCancelled()) {
        LOG.error("Alt sound upload failed: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
  @Override
  public void cancel() {
    if (currentUploadFuture != null && !currentUploadFuture.isDone()) {
      currentUploadFuture.cancel(true);

      LOG.warn("Upload cancelled");
    }
  }
}
