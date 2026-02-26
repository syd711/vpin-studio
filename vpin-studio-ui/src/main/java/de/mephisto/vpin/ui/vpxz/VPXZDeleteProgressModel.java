package de.mephisto.vpin.ui.vpxz;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZDescriptorRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class VPXZDeleteProgressModel extends ProgressModel<VPXZDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZDeleteProgressModel.class);

  private final Iterator<VPXZDescriptorRepresentation> iterator;
  @NonNull
  private final List<VPXZDescriptorRepresentation> vpxzFiles;

  public VPXZDeleteProgressModel(List<VPXZDescriptorRepresentation> vpxzFiles) {
    super(".vpxz Deletion");
    this.iterator = vpxzFiles.iterator();
    this.vpxzFiles = vpxzFiles;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return vpxzFiles.size();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public VPXZDescriptorRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return vpxzFiles.size() == 1;
  }

  @Override
  public String nextToString(VPXZDescriptorRepresentation f) {
    return "Deleting " + f.getFilename();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
    client.getVpxzService().invalidateVPXZCache();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, VPXZDescriptorRepresentation descriptor) {
    try {
      TableDetails tableDetails = descriptor.getTableDetails();
      List<GameRepresentation> gamesByFileName = client.getGameService().getGamesByFileName(-1, tableDetails.getGameFileName());
      boolean b = client.getVpxzService().deleteVPXZ(descriptor.getSource().getId(), descriptor.getFilename());

      for (GameRepresentation gameRepresentation : gamesByFileName) {
        EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
      }
    }
    catch (Exception e) {
      LOG.error("Error deleting .vpxz file: " + e.getMessage(), e);
    }
  }
}
