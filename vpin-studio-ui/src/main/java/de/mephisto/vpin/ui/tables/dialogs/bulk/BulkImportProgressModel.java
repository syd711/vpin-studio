package de.mephisto.vpin.ui.tables.dialogs.bulk;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BulkImportProgressModel extends ProgressModel<BulkImportItem> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<BulkImportItem> items;
  private final Iterator<BulkImportItem> iterator;
  private final Set<Integer> touchedGames = new HashSet<>();
  private final File moveTarget;
  private final boolean keepStructure;
  private int currentIndex = -1;

  /**
   * @param moveTarget    folder the successfully imported files are moved to, null to leave them in place
   * @param keepStructure true to recreate the subfolders of the source folder below the move target
   */
  public BulkImportProgressModel(List<BulkImportItem> items, File moveTarget, boolean keepStructure) {
    super(Messages.get("dialog.bulk_importer"));
    this.items = items;
    this.iterator = items.iterator();
    this.moveTarget = moveTarget;
    this.keepStructure = keepStructure;
  }

  @Override
  public int getMax() {
    return items.size();
  }

  @Override
  public BulkImportItem getNext() {
    currentIndex++;
    return iterator.next();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public String nextToString(BulkImportItem item) {
    return Messages.get("dialog.uploading") + item.getFile().getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, BulkImportItem item) {
    try {
      int gameId = item.gameProperty().get().getId();
      JobDescriptor result = Studio.client.getGameMediaService().uploadMedia(item.getFile(), gameId, false, item.screenProperty().get(),
          item.appendProperty().get(), VPinScreen.Loading, percent -> progressResultModel.setProgress((currentIndex + percent) / items.size()));
      if (!StringUtils.isEmpty(result.getError())) {
        LOG.error("Bulk import of {} failed: {}", item.getFile().getName(), result.getError());
        Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), result.getError()));
      }
      else {
        touchedGames.add(gameId);
        progressResultModel.addProcessed();
        // only files that have been imported are moved
        if (moveTarget != null) {
          moveFile(item);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Bulk import of {} failed: {}", item.getFile().getName(), e.getMessage(), e);
    }
  }

  private void moveFile(BulkImportItem item) {
    File source = item.getFile();
    File folder = moveTarget;
    if (keepStructure) {
      String parent = new File(item.getRelativePath()).getParent();
      if (parent != null) {
        folder = new File(moveTarget, parent);
      }
    }
    try {
      Files.createDirectories(folder.toPath());
      File target = uniqueTarget(folder, source.getName());
      Files.move(source.toPath(), target.toPath());
      LOG.info("Moved imported file {} to {}", source.getAbsolutePath(), target.getAbsolutePath());
    }
    catch (IOException e) {
      LOG.error("Failed to move imported file {} to {}: {}", source.getAbsolutePath(), folder.getAbsolutePath(), e.getMessage(), e);
    }
  }

  /**
   * Files with the same name are not overwritten, a counter is added to the name instead.
   */
  private static File uniqueTarget(File folder, String name) {
    File target = new File(folder, name);
    String baseName = FilenameUtils.getBaseName(name);
    String extension = FilenameUtils.getExtension(name);
    int counter = 1;
    while (target.exists()) {
      target = new File(folder, baseName + " (" + counter++ + ")" + (extension.isEmpty() ? "" : "." + extension));
    }
    return target;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    Platform.runLater(() -> {
      for (Integer gameId : touchedGames) {
        EventManager.getInstance().notifyJobFinished(JobType.POPPER_MEDIA_INSTALL, gameId, false, true);
      }
    });
    super.finalizeModel(progressResultModel);
  }
}
