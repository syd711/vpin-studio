package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RomUploadProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(RomUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final VPinStudioClient client;
  private final File file;

  public RomUploadProgressModel(VPinStudioClient client, String title, File file) {
    super(title);
    this.client = client;
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
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      File next = iterator.next();
      Studio.client.uploadRom(next, percent -> progressResultModel.setProgress(percent));
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
