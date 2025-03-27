package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class VPinManiaSynchronizeProgressModel extends ProgressModel<VpsTable> {
  private final static Logger LOG = LoggerFactory.getLogger(VPinManiaSynchronizeProgressModel.class);

  private final Iterator<VpsTable> iterator;
  private final List<VpsTable> vpsTableList;

  public VPinManiaSynchronizeProgressModel(List<VpsTable> vpsTableList) {
    super("VPin Mania Synchronization");
    this.iterator = vpsTableList.iterator();
    this.vpsTableList = vpsTableList;
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
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
    client.getManiaService().clearCache();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, VpsTable next) {
    try {
      ManiaTableSyncResult result = client.getManiaService().synchronize(next.getId());
      progressResultModel.getResults().add(result);
    } catch (Exception e) {
      LOG.error("Failed to synchronize the highscore for \"" + next.getDisplayName() + "\": " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
