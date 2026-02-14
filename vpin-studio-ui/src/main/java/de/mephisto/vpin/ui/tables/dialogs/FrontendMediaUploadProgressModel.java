package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class FrontendMediaUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<File> iterator;
  private int id = -1;
  private boolean playlistMode;
  private final VPinScreen screen;
  private final boolean append;
  private final List<File> files;

  public FrontendMediaUploadProgressModel(GameRepresentation game, String title, List<File> files, VPinScreen screen, boolean append) {
    this(game.getId(), false, title, files, screen, append);
  }

  public FrontendMediaUploadProgressModel(PlaylistRepresentation playlist, String title, List<File> files, VPinScreen screen, boolean append) {
    this(playlist.getId(), true, title, files, screen, append);
  }

  public FrontendMediaUploadProgressModel(int id, boolean playlistMode, String title, List<File> files, VPinScreen screen, boolean append) {
    super(title);
    this.playlistMode = playlistMode;
    this.id = id;
    this.files = files;
    this.screen = screen;
    this.append = append;
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
  public void finalizeModel(ProgressResultModel progressResultModel) {
    FileUtils.deleteIfTempFile(this.files);
    super.finalizeModel(progressResultModel);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      JobDescriptor result = client.getGameMediaService().uploadMedia(next, id, playlistMode, screen, append, percent -> progressResultModel.setProgress(percent));
      if (!StringUtils.isEmpty(result.getError())) {
        Platform.runLater(() -> {
          WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
        });
      }
      else if (!iterator.hasNext()) {
        Platform.runLater(() -> {
          if (playlistMode) {
            EventManager.getInstance().notifyJobFinished(JobType.PLAYLIST_MEDIA_INSTALL, id, false, true);
          }
          else {
            EventManager.getInstance().notifyJobFinished(JobType.POPPER_MEDIA_INSTALL, id, false, true);
          }
        });
      }

      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      LOG.error("Media upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

}
