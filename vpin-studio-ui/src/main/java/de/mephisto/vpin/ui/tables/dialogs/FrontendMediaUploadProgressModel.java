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
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class FrontendMediaUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendMediaUploadProgressModel.class);

  private final Iterator<File> iterator;
  private int gameId = -1;
  private int playlistId = -1;
  private final VPinScreen screen;
  private final boolean append;
  private final List<File> files;

  public FrontendMediaUploadProgressModel(GameRepresentation game, String title, List<File> files, VPinScreen screen, boolean append) {
    super(title);
    this.gameId = game.getId();
    this.files = files;
    this.screen = screen;
    this.append = append;
    this.iterator = files.iterator();
  }

  public FrontendMediaUploadProgressModel(PlaylistRepresentation playlist, String title, List<File> files, VPinScreen screen, boolean append) {
    super(title);
    this.playlistId = playlist.getId();
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
      if (gameId >= 0) {
        uploadGameMedia(progressResultModel, next);
      }
      else {
        uploadPlaylistMedia(progressResultModel, next);
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

  private void uploadPlaylistMedia(ProgressResultModel progressResultModel, File next) throws Exception {
    JobDescriptor result = client.getPlaylistMediaService().uploadMedia(next, playlistId, screen, append, percent -> progressResultModel.setProgress(percent));
    if (!StringUtils.isEmpty(result.getError())) {
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
      });
    }
    else if (!iterator.hasNext()) {
      Platform.runLater(() -> {
        EventManager.getInstance().notifyJobFinished(JobType.PLAYLIST_MEDIA_INSTALL, playlistId);
      });
    }
  }

  private void uploadGameMedia(ProgressResultModel progressResultModel, File next) throws Exception {
    JobDescriptor result = client.getGameMediaService().uploadMedia(next, gameId, screen, append, percent -> progressResultModel.setProgress(percent));
    if (!StringUtils.isEmpty(result.getError())) {
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", result.getError());
      });
    }
    else if (!iterator.hasNext()) {
      Platform.runLater(() -> {
        EventManager.getInstance().notifyJobFinished(JobType.POPPER_MEDIA_INSTALL, gameId);
      });
    }
  }
}
