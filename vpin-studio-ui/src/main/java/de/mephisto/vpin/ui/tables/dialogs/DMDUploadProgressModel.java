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

public class DMDUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(DMDUploadProgressModel.class);

  private final int emulatorId;
  private final GameRepresentation game;

  public DMDUploadProgressModel(String title, File file, int emulatorId, GameRepresentation game) {
    super(file, title);
    this.emulatorId = emulatorId;
    this.game = game;
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
}
