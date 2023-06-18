package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
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

import static de.mephisto.vpin.restclient.jobs.JobType.ALTCOLOR_INSTALL;
import static de.mephisto.vpin.restclient.jobs.JobType.ALTSOUND_INSTALL;

public class AltColorUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final TablesSidebarController tablesSidebarController;
  private final int gameId;
  private final File file;
  private final String altColorType;

  public AltColorUploadProgressModel(TablesSidebarController tablesSidebarController, int gameId, String title, File file, String altColorType) {
    super(title);
    this.tablesSidebarController = tablesSidebarController;
    this.gameId = gameId;
    this.file = file;
    this.altColorType = altColorType;
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
      JobExecutionResult result = Studio.client.getAltColorService().uploadAltColor(next, altColorType, gameId, percent -> progressResultModel.setProgress(percent));
      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      else {
        Platform.runLater(() -> {
          EventManager.getInstance().notifyJobFinished(ALTCOLOR_INSTALL);
        });
      }
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Alt color upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
