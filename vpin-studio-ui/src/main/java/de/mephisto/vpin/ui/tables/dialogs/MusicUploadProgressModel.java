package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;

public class MusicUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final int emulatorId;
  private final int gameId;

  public MusicUploadProgressModel(String title, File file, int emulatorId, int gameId) {
    super(file, title);
    this.emulatorId = emulatorId;
    this.gameId = gameId;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      Studio.client.getVpxService().uploadMusic(next, emulatorId, gameId, percent -> {
        Platform.runLater(() -> {
              progressResultModel.setProgress(percent);
            }
        );
      });
    }
    catch (Exception e) {
      LOG.error("Music upload failed: " + e.getMessage(), e);
    }
  }
}
