package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Iterator;

public class DefaultBackgroundUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<File> iterator;
  private final int gameId;
  private final File file;

  public DefaultBackgroundUploadProgressModel(int gameId, String title, File file) {
    super(title);
    this.gameId = gameId;
    this.file = file;
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
    return file.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      Studio.client.getAssetService().uploadDefaultBackgroundFile(next, gameId, percent -> progressResultModel.setProgress(percent));
      progressResultModel.addProcessed();
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
