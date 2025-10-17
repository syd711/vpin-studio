package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class CfgUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(CfgUploadProgressModel.class);

  private final int emuId;
  private double percentage = 0;

  public CfgUploadProgressModel(String title, List<File> files, int emuId) {
    super(files, title);
    this.emuId = emuId;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor descriptor = Studio.client.getMameService().uploadCfg(emuId, next, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / getMax());
      });
      progressResultModel.addProcessed();
      percentage++;

      if (descriptor.getError() != null) {
        throw new Exception(descriptor.getError());
      }
    }
    catch (Exception e) {
      LOG.error("Cfg upload failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Cfg upload failed: " + e.getMessage());
      });
    }
  }
}
