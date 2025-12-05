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

public class IniUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final int gameId;

  public IniUploadProgressModel(int gameId, String title, File file) {
    super(file, title);
    this.gameId = gameId;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor result = Studio.client.getIniService().uploadIniFile(next, gameId, percent -> progressResultModel.setProgress(percent));
      progressResultModel.addProcessed();
      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      EventManager.getInstance().notifyTableChange(gameId, null);
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Ini upload failed: " + e.getMessage(), e);
    }
  }
}
