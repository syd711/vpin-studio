package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static de.mephisto.vpin.restclient.jobs.JobType.POV_INSTALL;

public class PovUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(PovUploadProgressModel.class);

  private final int gameId;

  public PovUploadProgressModel(int gameId, String title, File file) {
    super(file, title);
    this.gameId = gameId;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor result = Studio.client.getVpxService().uploadPov(next, gameId, percent -> progressResultModel.setProgress(percent));
      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      else {
        Platform.runLater(() -> {
          EventManager.getInstance().notifyJobFinished(POV_INSTALL, gameId);
        });
      }
      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      LOG.error("POV upload failed: " + e.getMessage(), e);
    }
  }
}
