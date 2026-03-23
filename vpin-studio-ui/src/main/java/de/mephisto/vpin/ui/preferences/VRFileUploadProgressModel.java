package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class VRFileUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final int emulatorId;

  public VRFileUploadProgressModel(String title, File file, int emulatorId) {
    super(file, title);
    this.emulatorId = emulatorId;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      Studio.client.getVRService().uploadFile(next, emulatorId, percent -> {
        Platform.runLater(() -> {
              progressResultModel.setProgress(percent);
            }
        );
      });
    }
    catch (Exception e) {
      LOG.error("VR upload failed: {}", e.getMessage(), e);
    }
  }
}
