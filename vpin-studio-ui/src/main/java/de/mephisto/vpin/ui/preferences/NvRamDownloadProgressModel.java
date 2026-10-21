package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.system.NVRamsInfo;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class NvRamDownloadProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamDownloadProgressModel.class);

  private List<String> nvRams;
  private Iterator<String> fileIterator;
  private final NVRamsInfo nvRamsInfo = new NVRamsInfo();

  public NvRamDownloadProgressModel(String title) {
    super(title);
  }

  /**
   * The index is fetched lazily, so that it is loaded by the progress worker thread and not the FX thread.
   */
  private void loadIndex() {
    if (nvRams == null) {
      nvRams = client.getSystemService().getNvRamIndex();
      fileIterator = nvRams.iterator();
    }
  }

  @Override
  public int getMax() {
    return nvRams != null ? nvRams.size() : 1;
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
  public String nextToString(String next) {
    return next + ".nv";
  }

  @Override
  public boolean hasNext() {
    loadIndex();
    return fileIterator.hasNext();
  }

  public void processNext(ProgressResultModel progressResultModel, String item) {
    try {
      if (client.getSystemService().resetNvRam(item)) {
        nvRamsInfo.setCount(nvRamsInfo.getCount() + 1);
      }
      if (progressResultModel.getResults().isEmpty()) {
        progressResultModel.getResults().add(nvRamsInfo);
      }
    }
    catch (Exception e) {
      LOG.error("NVRam download error for {}: {}", item, e.getMessage(), e);
    }
  }
}
