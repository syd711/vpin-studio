package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

import static de.mephisto.vpin.restclient.jobs.JobType.DIRECTB2S_INSTALL;

public class DirectB2SUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final int gameId;
  private final File file;

  public DirectB2SUploadProgressModel(int gameId, String title, File file) {
    super(file, title);
    this.gameId = gameId;
    this.file = file;
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
    return file.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor result = Studio.client.getBackglassServiceClient().uploadDirectB2SFile(next, gameId, percent -> progressResultModel.setProgress(percent));
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
      LOG.error("Res upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
