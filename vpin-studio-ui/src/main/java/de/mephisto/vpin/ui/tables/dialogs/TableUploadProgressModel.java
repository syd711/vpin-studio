package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TableUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final List<File> files;
  private final boolean importToPopper;
  private final int playlistId;
  private final int replaceId;
  private double percentage = 0;

  public TableUploadProgressModel(String title, List<File> files, boolean importToPopper, int playlistId, int replaceId) {
    super(title);
    this.files = files;
    this.importToPopper = importToPopper;
    this.playlistId = playlistId;
    this.replaceId = replaceId;
    iterator = this.files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return true;
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
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      Studio.client.uploadTable(next, importToPopper, playlistId, replaceId, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / this.files.size());
      });
      progressResultModel.addProcessed();
      percentage++;
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
