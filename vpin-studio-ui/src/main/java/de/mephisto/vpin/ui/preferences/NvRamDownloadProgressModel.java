package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.system.NVRamsInfo;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class NvRamDownloadProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamDownloadProgressModel.class);
  private Iterator<String> fileIterator;

  public NvRamDownloadProgressModel(String title) {
    super(title);
    List<String> list = Arrays.asList("Download NV RAM Files");
    this.fileIterator = list.iterator();
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public String getNext() {
    return this.fileIterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(String next) {
    return next;
  }

  @Override
  public boolean hasNext() {
    return fileIterator.hasNext();
  }

  public void processNext(ProgressResultModel progressResultModel, String item) {
    try {
      NVRamsInfo nvRamsInfo = client.getSystemService().resetNvRams();
      progressResultModel.getResults().add(nvRamsInfo);
    } catch (Exception e) {
      LOG.error("Logs download error: " + e.getMessage(), e);
    }
  }
}
