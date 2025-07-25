package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class VPinManiaScoreSynchronizeProgressModel extends ProgressModel<VpsTable> {
  private final static Logger LOG = LoggerFactory.getLogger(VPinManiaScoreSynchronizeProgressModel.class);

  private Iterator<VpsTable> iterator;
  private List<VpsTable> vpsTableList = new ArrayList<>();

  public VPinManiaScoreSynchronizeProgressModel() {
    super("VPin Mania Synchronization");
    this.vpsTableList = Studio.client.getGameService().getInstalledVpsTables();
    this.iterator = vpsTableList.iterator();
  }

  public VPinManiaScoreSynchronizeProgressModel(List<VpsTable> vpsTableList) {
    super("VPin Mania Synchronization");
    this.vpsTableList = vpsTableList;
    this.iterator = vpsTableList.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public VpsTable getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(VpsTable t) {
    return "Synchronizing \"" + t.getDisplayName() + "\"";
  }

  @Override
  public int getMax() {
    return vpsTableList.size();
  }

  @Override
  public boolean isIndeterminate() {
    return vpsTableList.size() == 1;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
    client.getManiaService().clearCache();
    client.getPreferenceService().notifyPreferenceChange(PreferenceNames.MANIA_SETTINGS, null);
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, VpsTable next) {
    try {
      ManiaTableSyncResult result = client.getManiaService().synchronizeHighscore(next.getId());
      if (result != null) {
        progressResultModel.getResults().add(result);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to synchronize the highscore for \"" + next.getDisplayName() + "\": " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
