package de.mephisto.vpin.ui.archiving.dialogs;

import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class ArchiveRestoreProgressModel extends ProgressModel<ArchiveDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveRestoreProgressModel.class);

  private final ArchiveRestoreDescriptor descriptor;
  private final Iterator<ArchiveDescriptorRepresentation> iterator;
  private final List<ArchiveDescriptorRepresentation> archiveDescritors;
  private double percentage = 0;

  public ArchiveRestoreProgressModel(String title, ArchiveRestoreDescriptor descriptor, List<ArchiveDescriptorRepresentation> archiveDescriptors) {
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
  public ArchiveDescriptorRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(ArchiveDescriptorRepresentation d) {
    return d.getTableDetails().getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, ArchiveDescriptorRepresentation next) {
    try {
      descriptor.setFilename(next.getFilename());
      Studio.client.getArchiveService().restoreTable(descriptor);

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
