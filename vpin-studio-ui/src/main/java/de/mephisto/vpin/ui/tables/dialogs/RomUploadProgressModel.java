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

public class RomUploadProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(RomUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final VPinStudioClient client;
  private final List<File> files;
  private double percentage = 0;

  public RomUploadProgressModel(VPinStudioClient client, String title, List<File> files) {
    super(title);
    this.client = client;
    this.files = files;
    this.iterator = files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return files.size();
  }

  @Override
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      File next = iterator.next();
      Studio.client.uploadRom(next, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / this.files.size());
      });
      progressResultModel.addProcessed();
      percentage++;
      return next.getName();
    } catch (Exception e) {
      LOG.error("ROM upload failed: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
