package de.mephisto.vpin.ui.vpxz.dialogs;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class VPXZSyncProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZSyncProgressModel.class);

  private final Iterator<String> iterator;
  private final List<String> urls;

  public VPXZSyncProgressModel(String title) {
    super(title);
    this.urls = List.of("https://github.com/jsm174/vpx-standalone-scripts");
    this.iterator = this.urls.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public String getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(String file) {
    return "Synchronizing " + file;
  }

  @Override
  public int getMax() {
    return urls.size();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String next) {
    try {
      client.getVpxzService().getVpxStandaloneFiles(true);
    }
    catch (Exception e) {
      LOG.error("VPX standalone file sync failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
