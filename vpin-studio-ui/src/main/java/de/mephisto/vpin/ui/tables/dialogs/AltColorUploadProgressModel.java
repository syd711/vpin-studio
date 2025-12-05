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
import java.lang.invoke.MethodHandles;

import static de.mephisto.vpin.restclient.jobs.JobType.ALTCOLOR_INSTALL;

public class AltColorUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final int gameId;

  public AltColorUploadProgressModel(int gameId, String title, File file, String altColorType) {
    super(file, title);
    this.gameId = gameId;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor result = Studio.client.getAltColorService().uploadAltColor(next, gameId, percent -> progressResultModel.setProgress(percent));
      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      else {
        Platform.runLater(() -> {
          EventManager.getInstance().notifyJobFinished(ALTCOLOR_INSTALL, gameId, false, true);
        });
      }
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Alt color upload failed: " + e.getMessage(), e);
    }
  }
}
