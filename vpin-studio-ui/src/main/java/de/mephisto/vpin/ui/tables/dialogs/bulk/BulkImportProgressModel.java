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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BulkImportProgressModel extends ProgressModel<BulkImportItem> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<BulkImportItem> items;
  private final Iterator<BulkImportItem> iterator;
  private final Set<Integer> touchedGames = new HashSet<>();

  public BulkImportProgressModel(List<BulkImportItem> items) {
    super(Messages.get("dialog.bulk_importer"));
    this.items = items;
    this.iterator = items.iterator();
  }

  @Override
  public int getMax() {
    return items.size();
  }

  @Override
  public BulkImportItem getNext() {
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
          item.appendProperty().get(), VPinScreen.Loading, percent -> progressResultModel.setProgress(percent));
      if (!StringUtils.isEmpty(result.getError())) {
        LOG.error("Bulk import of {} failed: {}", item.getFile().getName(), result.getError());
        Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), result.getError()));
      }
      else {
        touchedGames.add(gameId);
        progressResultModel.addProcessed();
      }
    }
    catch (Exception e) {
      LOG.error("Bulk import of {} failed: {}", item.getFile().getName(), e.getMessage(), e);
    }
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
