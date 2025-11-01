package de.mephisto.vpin.ui.backups.dialogs;

import de.mephisto.vpin.restclient.games.descriptors.BackupRestoreDescriptor;
import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class BackupRestoreProgressModel extends ProgressModel<BackupDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(BackupRestoreProgressModel.class);

  private final BackupRestoreDescriptor descriptor;
  private final Iterator<BackupDescriptorRepresentation> iterator;
  private final List<BackupDescriptorRepresentation> archiveDescritors;
  private double percentage = 0;

  public BackupRestoreProgressModel(String title, BackupRestoreDescriptor descriptor, List<BackupDescriptorRepresentation> archiveDescriptors) {
    super(title);
    this.descriptor = descriptor;
    this.iterator = archiveDescriptors.iterator();
    this.archiveDescritors = archiveDescriptors;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return archiveDescritors.size();
  }

  @Override
  public BackupDescriptorRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(BackupDescriptorRepresentation d) {
    return d.getTableDetails().getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, BackupDescriptorRepresentation next) {
    try {
      descriptor.setFilename(next.getFilename());
      Studio.client.getBackupService().restoreTable(descriptor);

      progressResultModel.addProcessed();
      percentage++;
    } catch (Exception e) {
      LOG.error("Table installation failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
