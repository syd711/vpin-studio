package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.VpaImportDescriptor;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class VpaImportProgressModel extends ProgressModel<VpaDescriptorRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(VpaImportProgressModel.class);

  private final VpaImportDescriptor descriptor;
  private final Iterator<VpaDescriptorRepresentation> iterator;
  private final List<VpaDescriptorRepresentation> vpaDescriptors;
  private double percentage = 0;

  public VpaImportProgressModel(String title, VpaImportDescriptor descriptor, List<VpaDescriptorRepresentation> vpaDescriptors) {
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
  public VpaDescriptorRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(VpaDescriptorRepresentation d) {
    return d.getManifest().getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, VpaDescriptorRepresentation next) {
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
