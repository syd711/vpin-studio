package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TableImportProgressModel extends ProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(TableImportProgressModel.class);

  private final ImportDescriptor descriptor;
  private final Iterator<File> iterator;
  private final List<File> vpaFiles;
  private double percentage = 0;

  public TableImportProgressModel(String title, ImportDescriptor descriptor, List<File> vpaFiles) {
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
  public String processNext(ProgressResultModel progressResultModel) {
    try {
      File next = iterator.next();
      String vpaFileName = Studio.client.uploadVpa(next, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / this.vpaFiles.size());
      });

      descriptor.setVpaFileName(vpaFileName);
      Studio.client.importVpa(descriptor);

      progressResultModel.addProcessed();
      percentage++;
      return next.getName();
    } catch (Exception e) {
      LOG.error("Table export failed: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
