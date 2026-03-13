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

public class DirectB2SUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final int gameId;
  private final boolean append;

  public DirectB2SUploadProgressModel(int gameId, String title, File file, boolean append) {
    super(file, title);
    this.gameId = gameId;
    this.append = append;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor result = Studio.client.getBackglassServiceClient().uploadDirectB2SFile(next, gameId, append, percent -> progressResultModel.setProgress(percent));
      if (StringUtils.isNotEmpty(result.getError())) {
        progressResultModel.addError();

        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      else {
        Platform.runLater(() -> {
          EventManager.getInstance().notifyTableChange(gameId, null);
        });
        progressResultModel.addProcessed(result);
      }
    }
    catch (Exception e) {
      LOG.error(".directb2s upload failed: " + e.getMessage(), e);
    }
  }
}
