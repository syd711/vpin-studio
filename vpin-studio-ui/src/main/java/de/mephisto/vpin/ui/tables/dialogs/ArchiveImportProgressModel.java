package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.descriptors.ArchiveInstallDescriptor;
import de.mephisto.vpin.restclient.representations.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class ArchiveImportProgressModel extends ProgressModel<ArchiveDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveImportProgressModel.class);

  private final ArchiveInstallDescriptor descriptor;
  private final Iterator<ArchiveDescriptorRepresentation> iterator;
  private final List<ArchiveDescriptorRepresentation> vpaDescriptors;
  private double percentage = 0;

  public ArchiveImportProgressModel(String title, ArchiveInstallDescriptor descriptor, List<ArchiveDescriptorRepresentation> vpaDescriptors) {
    super(title);
    this.descriptor = descriptor;
    this.iterator = vpaDescriptors.iterator();
    this.vpaDescriptors = vpaDescriptors;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return vpaDescriptors.size();
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
      //TODO
//      descriptor.setUuid(next.getManifest().getUuid());
//      Studio.client.importVpa(descriptor);

      progressResultModel.addProcessed();
      percentage++;
    } catch (Exception e) {
      LOG.error("Table export failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
