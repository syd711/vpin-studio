package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

public class DMDUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(DMDUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final int emulatorId;
  private final File file;
  private final GameRepresentation game;

  public DMDUploadProgressModel(String title, File file, int emulatorId, GameRepresentation game, Runnable finalizer) {
    super(file, title, finalizer);
    this.emulatorId = emulatorId;
    this.file = file;
    this.game = game;
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
      UploadDescriptor result = Studio.client.getDmdService().uploadDMDPackage(next, game.getId(), percent ->
        progressResultModel.setProgress(percent));
      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      else {
        Platform.runLater(() -> {
          JobPoller.getInstance().setPolling();
        });
      }
      progressResultModel.addProcessed();

      if (game != null) {
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
    }
    catch (Exception e) {
      LOG.error("DMD bundle upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
