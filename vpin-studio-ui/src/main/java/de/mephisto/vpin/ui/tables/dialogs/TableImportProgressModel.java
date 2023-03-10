package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.VpaImportDescriptor;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TableImportProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportProgressModel.class);

  private final VpaImportDescriptor descriptor;
  private final Iterator<File> iterator;
  private final List<File> vpaFiles;
  private double percentage = 0;

  public TableImportProgressModel(String title, VpaImportDescriptor descriptor, List<File> vpaFiles) {
    super(title);
    this.descriptor = descriptor;
    this.iterator = vpaFiles.iterator();
    this.vpaFiles = vpaFiles;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return vpaFiles.size();
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(File file) {
    return file.getName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
//      String uuid = Studio.client.uploadVpa(next, percent -> {
//        double total = percentage + percent;
//        progressResultModel.setProgress(total / this.vpaFiles.size());
//      });
//
//      descriptor.setUuid(uuid);
//      Studio.client.importVpa(descriptor);
//
//      progressResultModel.addProcessed();
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
