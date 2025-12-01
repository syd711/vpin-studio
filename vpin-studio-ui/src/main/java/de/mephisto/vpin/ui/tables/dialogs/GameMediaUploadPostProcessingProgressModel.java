package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;

public class GameMediaUploadPostProcessingProgressModel extends ProgressModel<UploadDescriptor> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Iterator<UploadDescriptor> iterator;
  private final UploadDescriptor uploadDescriptor;

  public GameMediaUploadPostProcessingProgressModel(String title, UploadDescriptor uploadDescriptor) {
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
  public UploadDescriptor getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(UploadDescriptor descriptor) {
    return "Unpacking \"" + uploadDescriptor.getOriginalUploadFileName() + "\", please wait...";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, UploadDescriptor descriptor) {
    try {
      UploadDescriptor result = Studio.client.getGameService().proccessTableUpload(uploadDescriptor);
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
