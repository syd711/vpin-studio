package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Extracts the backglass image of each given table and stores it as backglass media asset.
 */
public class BackglassBulkImportProgressModel extends ProgressModel<BackglassBulkImportItem> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<BackglassBulkImportItem> items;
  private final Iterator<BackglassBulkImportItem> iterator;
  private final BackglassBulkImportType type;

  public BackglassBulkImportProgressModel(List<BackglassBulkImportItem> items, BackglassBulkImportType type) {
    super(Messages.get(type == BackglassBulkImportType.DMD ? "backglass.bulk_import.progress_title_dmd" : "backglass.bulk_import.progress_title"));
    this.items = items;
    this.type = type;
    this.iterator = items.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return items.size();
  }

  @Override
  public BackglassBulkImportItem getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(BackglassBulkImportItem item) {
    return Messages.get(type == BackglassBulkImportType.DMD ? "backglass.bulk_import.extracting_dmd" : "backglass.bulk_import.extracting") + " " + item.getDisplayName();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, BackglassBulkImportItem item) {
    File tmp = null;
    try {
      DirectB2SData data = client.getBackglassServiceClient().getDirectB2SData(item.getGameId());
      boolean dmd = type == BackglassBulkImportType.DMD;
      if (data == null || !(dmd ? data.isDmdImageAvailable() : data.isBackgroundAvailable())) {
        progressResultModel.addSkipped();
        return;
      }

      BufferedImage image;
      try (InputStream in = dmd ? client.getBackglassServiceClient().getDirectB2sDmd(data) : client.getBackglassServiceClient().getDirectB2sPreviewBackground(data, false)) {
        image = ImageIO.read(in);
      }
      if (image == null) {
        LOG.error("Cannot read {} image of \"{}\"", type, item.getDisplayName());
        progressResultModel.addError();
        return;
      }

      VPinScreen screen = type.getScreen();
      tmp = File.createTempFile("tmp_" + screen, ".png");
      ImageIO.write(image, "png", tmp);

      JobDescriptor result = client.getGameMediaService().uploadMedia(tmp, item.getGameId(), false, screen, false, null, percent -> {});
      if (!StringUtils.isEmpty(result.getError())) {
        LOG.error("{} bulk import failed for \"{}\": {}", type, item.getDisplayName(), result.getError());
        progressResultModel.addError();
        return;
      }

      int gameId = item.getGameId();
      Platform.runLater(() -> EventManager.getInstance().notifyJobFinished(JobType.POPPER_MEDIA_INSTALL, gameId, false, true));
      progressResultModel.addProcessed();
    }
    catch (Exception e) {
      LOG.error("Backglass bulk import failed for \"{}\": {}", item.getDisplayName(), e.getMessage(), e);
      progressResultModel.addError();
    }
    finally {
      if (tmp != null && !tmp.delete()) {
        tmp.deleteOnExit();
      }
    }
  }
}
