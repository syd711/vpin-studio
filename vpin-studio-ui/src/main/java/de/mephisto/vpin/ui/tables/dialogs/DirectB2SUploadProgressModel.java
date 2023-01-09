package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;

import static de.mephisto.vpin.ui.Studio.client;

public class DirectB2SUploadProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final VPinStudioClient client;
  private int gameId;
  private final File file;
  private String directB2SType;

  public DirectB2SUploadProgressModel(VPinStudioClient client, int gameId, String title, File file, String directB2SType) {
    super(title);
    this.client = client;
    this.gameId = gameId;
    this.file = file;
    this.directB2SType = directB2SType;
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
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      File next = iterator.next();
      client.uploadDirectB2SFile(next, directB2SType, gameId, percent -> progressResultModel.setProgress(percent));
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
