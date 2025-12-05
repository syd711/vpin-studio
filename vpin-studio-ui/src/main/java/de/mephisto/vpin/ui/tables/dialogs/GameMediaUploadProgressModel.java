package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class GameMediaUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<File> iterator;
  private final List<File> files;
  private final File file;
  private final int gameId;
  private final UploadType uploadType;
  private final int emuId;
  private double percentage = 0;

  public GameMediaUploadProgressModel(String title, File file, int gameId, UploadType uploadType, int emuId) {
    super(title);
    this.files = Collections.singletonList(file);
    this.file = file;
    this.gameId = gameId;
    this.emuId = emuId;
    this.uploadType = uploadType;
    iterator = this.files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return this.files.size();
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
  public void finalizeModel(ProgressResultModel progressResultModel) {
    FileUtils.deleteIfTempFile(file);
    super.finalizeModel(progressResultModel);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      UploadDescriptor uploadDescriptor = Studio.client.getGameService().uploadTable(next, uploadType, gameId, emuId, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / this.files.size());
      });
      progressResultModel.addProcessed();
      progressResultModel.getResults().add(uploadDescriptor);
      percentage++;
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Table upload failed: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
