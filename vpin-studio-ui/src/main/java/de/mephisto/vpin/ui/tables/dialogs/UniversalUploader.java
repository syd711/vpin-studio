package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class UniversalUploader {
  private final static Logger LOG = LoggerFactory.getLogger(UniversalUploader.class);

  public static Optional<UploadDescriptor> upload(File selection, int gameId, TableUploadType tableUploadType, GameEmulatorRepresentation emulatorRepresentation) {
    Optional<UploadDescriptor> result = Optional.empty();
    try {
      GameMediaUploadProgressModel model = new GameMediaUploadProgressModel("Game Media Upload", selection, gameId, tableUploadType, emulatorRepresentation.getId());
      ProgressResultModel uploadResultModel = ProgressDialog.createProgressDialog(model);

      List<Object> results = uploadResultModel.getResults();
      if (!results.isEmpty()) {
        final UploadDescriptor uploadDescriptor = (UploadDescriptor) results.get(0);
        if (!StringUtils.isEmpty(uploadDescriptor.getError())) {
          Platform.runLater(() -> {
            WidgetFactory.showAlert(Studio.stage, "Error", "Upload Failed: " + uploadDescriptor.getError());
          });
          return result;
        }

        result = Optional.of(uploadDescriptor);
      }
    }
    catch (Exception e) {
      LOG.error("Upload failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Uploading game media failed.", "Please check the log file for details.", "Error: " + e.getMessage());
    }
    return result;
  }

  public static Optional<UploadDescriptor> postProcess(UploadDescriptor descriptor) {
    Optional<UploadDescriptor> result = Optional.empty();
    try {
      GameMediaUploadPostProcessingProgressModel progressModel = new GameMediaUploadPostProcessingProgressModel("Importing Game Media", descriptor);
      ProgressResultModel progressDialogResult = ProgressDialog.createProgressDialog(progressModel);
      if (!progressDialogResult.getResults().isEmpty()) {
        UploadDescriptor uploadedAndImportedDescriptor = (UploadDescriptor) progressDialogResult.getResults().get(0);
        if (!StringUtils.isEmpty(uploadedAndImportedDescriptor.getError())) {
          Platform.runLater(() -> {
            WidgetFactory.showAlert(Studio.stage, "Error", "Error during import: " + uploadedAndImportedDescriptor.getError());
          });
          return result;
        }

        result = Optional.of(uploadedAndImportedDescriptor);
      }
    }
    catch (Exception e) {
      LOG.error("Upload post processing failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Post processing game media failed.", "Please check the log file for details.", "Error: " + e.getMessage());
    }
    return result;
  }
}