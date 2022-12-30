package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TableUploadProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final VPinStudioClient client;
  private final List<File> files;
  private final boolean importToPopper;
  private final int playlistId;

  public TableUploadProgressModel(VPinStudioClient client, String title, List<File> files, boolean importToPopper, int playlistId) {
    super(title);
    this.client = client;
    this.files = files;
    this.importToPopper = importToPopper;
    this.playlistId = playlistId;
    iterator = this.files.iterator();
  }

  @Override
  public int getMax() {
    return this.files.size();
  }

  @Override
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      File next = iterator.next();
      Studio.client.uploadTable(next, importToPopper, playlistId);
      progressResultModel.addProcessed();
      return next.getName();
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
