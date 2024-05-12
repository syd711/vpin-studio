package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

public class TableUploadProcessingProgressModel extends ProgressModel<TableUploadDescriptor> {
  private final static Logger LOG = LoggerFactory.getLogger(TableUploadProcessingProgressModel.class);

  private final Iterator<TableUploadDescriptor> iterator;
  private final TableUploadDescriptor uploadDescriptor;

  public TableUploadProcessingProgressModel(String title, TableUploadDescriptor uploadDescriptor) {
    super(title);
    this.uploadDescriptor = uploadDescriptor;
    iterator = Arrays.asList(uploadDescriptor).iterator();
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
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public TableUploadDescriptor getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(TableUploadDescriptor descriptor) {
    return "Processing \"" + uploadDescriptor.getOriginalUploadedVPXFileName() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, TableUploadDescriptor descriptor) {
    try {
      TableUploadDescriptor result = Studio.client.getGameService().proccessTableUpload(uploadDescriptor);
      progressResultModel.getResults().add(result);
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Post processing failed: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}
