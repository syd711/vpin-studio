package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class BackupDeleteProgressModel extends ProgressModel<BackupDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(BackupDeleteProgressModel.class);

  private final Iterator<BackupDescriptorRepresentation> iterator;
  @org.jetbrains.annotations.NotNull
  private final List<BackupDescriptorRepresentation> backups;

  public BackupDeleteProgressModel(List<BackupDescriptorRepresentation> backups) {
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
    return 1;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public BackupDescriptorRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return backups.size() == 1;
  }

  @Override
  public String nextToString(BackupDescriptorRepresentation f) {
    return "Deleting " + f.getFilename();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, BackupDescriptorRepresentation backup) {
    try {
      List<GameRepresentation> gamesByFileName = client.getGameService().getGamesByFileName(-1, backup.getTableDetails().getGameFileName());
      boolean b = client.getArchiveService().deleteBackup(backup.getSource().getId(), backup.getFilename());

      for (GameRepresentation gameRepresentation : gamesByFileName) {
        EventManager.getInstance().notifyTableChange(gameRepresentation.getId(), null);
      }
    }
    catch (Exception e) {
      LOG.error("Error deleting backup file: " + e.getMessage(), e);
    }
  }
}
