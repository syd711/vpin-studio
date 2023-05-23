package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TableUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final List<File> files;
  private final int gameId;
  private final TableUploadDescriptor tableUploadDescriptor;
  private double percentage = 0;

  public TableUploadProgressModel(String title, File file, int gameId, TableUploadDescriptor tableUploadDescriptor) {
    super(title);
    this.files = Collections.singletonList(file);
    this.gameId = gameId;
    this.tableUploadDescriptor = tableUploadDescriptor;
    iterator = this.files.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return true;
  }

  @Override
  public int getMax() {
    return this.files.size();
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
      Studio.client.getHighscoreCardsService().uploadTable(next,tableUploadDescriptor, gameId, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / this.files.size());
      });
      progressResultModel.addProcessed();
      percentage++;
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
