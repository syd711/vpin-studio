package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
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

public class PupPackUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackUploadProgressModel.class);

  private final String rom;

  public PupPackUploadProgressModel(String rom, String title, File file) {
    super(file, title);
    this.rom = rom;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor result = Studio.client.getPupPackService().uploadPupPack(next, percent ->
          Platform.runLater(() -> {
            progressResultModel.setProgress(percent);
          }));
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
      EventManager.getInstance().notifyTableChange(-1, rom);
    }
    catch (Exception e) {
      LOG.error("PUP pack upload failed: " + e.getMessage(), e);
    }
  }
}
