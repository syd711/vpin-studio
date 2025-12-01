package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

public class VpsDBDownloadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<File> iterator;

  public VpsDBDownloadProgressModel(String title, List<File> files) {
    super(title);
    this.iterator = files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(File file) {
    return "Download VPS Database";
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
    EventManager.getInstance().notifyTablesChanged();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      Studio.client.getVpsService().update();
    }
    catch (Exception e) {
      LOG.error("VPS database download failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Download Failed", "VPS database download failed: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
