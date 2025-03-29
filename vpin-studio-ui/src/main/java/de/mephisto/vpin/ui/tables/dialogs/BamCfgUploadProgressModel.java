package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class BamCfgUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(BamCfgUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final List<File> files;
  private double percentage = 0;
  private final int gameId;

  public BamCfgUploadProgressModel(String title, List<File> files, int gameId, Runnable finalizer) {
    super(files, title, finalizer);
    this.files = files;
    this.iterator = files.iterator();
    this.gameId = gameId;
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
  public String nextToString(File file) {
    return file.getName();
  }

  @Override
  public int getMax() {
    return files.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor descriptor = Studio.client.getFpService().uploadCfg(gameId, next, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / this.files.size());
      });
      progressResultModel.addProcessed();
      percentage++;

      if (descriptor.getError() != null) {
        throw new Exception(descriptor.getError());
      }
    }
    catch (Exception e) {
      LOG.error("BAM Cfg upload failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "BAM Cfg upload failed: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
