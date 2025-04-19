package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class VPinManiaTablesSynchronizeProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(VPinManiaTablesSynchronizeProgressModel.class);

  private Iterator<String> iterator;
  private List<String> list;

  public VPinManiaTablesSynchronizeProgressModel() {
    super("VPin Mania Synchronization");
    this.list = Arrays.asList("");
    this.iterator = this.list.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(String t) {
    return "Synchronizing Table List";
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
    client.getManiaService().clearCache();
    client.getPreferenceService().notifyPreferenceChange(PreferenceNames.MANIA_SETTINGS, null);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String next) {
    try {
      client.getManiaService().synchronizeTables();
    }
    catch (Exception e) {
      LOG.error("Failed to synchronize the tables: {}", e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
