package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.popper.PopperScreen;
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
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.restclient.jobs.JobType.POPPER_MEDIA_INSTALL;

public class TableMediaUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(TableMediaUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final TablesSidebarController tablesSidebarController;
  private final int gameId;
  private final String mediaType;
  private final PopperScreen screen;
  private final List<File> files;

  public TableMediaUploadProgressModel(TablesSidebarController tablesSidebarController, int gameId, String title, List<File> files, String mediaType, PopperScreen screen) {
    super(title);
    this.tablesSidebarController = tablesSidebarController;
    this.gameId = gameId;
    this.files = files;
    this.mediaType = mediaType;
    this.screen = screen;
    this.iterator = files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return files.size();
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
      JobExecutionResult result = Studio.client.getPinUPPopperService().uploadMedia(next, mediaType, gameId, screen, percent -> progressResultModel.setProgress(percent));
      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      else if (!iterator.hasNext()) {
        Platform.runLater(() -> {
          EventManager.getInstance().notifyJobFinished(POPPER_MEDIA_INSTALL);
        });
      }
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Popper media upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
