package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

public class DMDUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(DMDUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final TablesSidebarController tablesSidebarController;
  private final int gameId;
  private final File file;
  private final String uploadType;

  public DMDUploadProgressModel(TablesSidebarController tablesSidebarController, int gameId, String title, File file, String uploadType) {
    super(title);
    this.tablesSidebarController = tablesSidebarController;
    this.gameId = gameId;
    this.file = file;
    this.uploadType = uploadType;
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
    return "Uploading " + file.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      JobExecutionResult result = Studio.client.getDmdService().uploadDMDPackage(next, uploadType, gameId, percent ->
          Platform.runLater(() -> {
            progressResultModel.setProgress(percent);
          }));
      if(!StringUtils.isEmpty(result.getError())) {
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
    } catch (Exception e) {
      LOG.error("DMD bundle upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
