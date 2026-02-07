package de.mephisto.vpin.ui.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZDescriptorRepresentation;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class VPXZDeleteProgressModel extends ProgressModel<VPXZDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZDeleteProgressModel.class);

  private final Iterator<VPXZDescriptorRepresentation> iterator;
  @org.jetbrains.annotations.NotNull
  private final List<VPXZDescriptorRepresentation> backups;

  public VPXZDeleteProgressModel(List<VPXZDescriptorRepresentation> backups) {
    super("Backup Deletion");
    this.iterator = backups.iterator();
    this.backups = backups;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return backups.size();
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
    return backups.size() == 1;
  }

  @Override
  public String nextToString(VPXZDescriptorRepresentation f) {
    return "Deleting " + f.getFilename();
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);
    client.getVPXMobileService().invalidateVPXZCache();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, VPXZDescriptorRepresentation backup) {
    try {
      TableDetails tableDetails = backup.getTableDetails();
      List<GameRepresentation> gamesByFileName = client.getGameService().getGamesByFileName(-1, tableDetails.getGameFileName());
      boolean b = client.getVPXMobileService().deleteVPXZ(backup.getSource().getId(), backup.getFilename());

      for (GameRepresentation gameRepresentation : gamesByFileName) {
        EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
      }
    }
    catch (Exception e) {
      LOG.error("Error deleting backup file: " + e.getMessage(), e);
    }
  }
}
