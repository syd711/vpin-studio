package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class UniversalUploadUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static Optional<UploadDescriptor> upload(File selection, int gameId, UploadType uploadType, int emulatorId) {
    Optional<UploadDescriptor> result = Optional.empty();
    try {
      GameMediaUploadProgressModel model = new GameMediaUploadProgressModel("Game Media Upload", selection, gameId, uploadType, emulatorId);
      ProgressResultModel uploadResultModel = ProgressDialog.createProgressDialog(model);

      List<Object> results = uploadResultModel.getResults();
      if (!results.isEmpty()) {
        final UploadDescriptor uploadDescriptor = (UploadDescriptor) results.getFirst();
        if (!StringUtils.isEmpty(uploadDescriptor.getError())) {
          Platform.runLater(() -> {
            WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.upload_failed") + uploadDescriptor.getError());
          });
          return result;
        }

        result = Optional.of(uploadDescriptor);
      }
    }
    catch (Exception e) {
      LOG.error("Upload failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.uploading_game_media_failed"), Messages.get("dialog.please_check_the_log_file_for_details"), Messages.get("dialog.error") + e.getMessage());
    }
    return result;
  }

  public static Optional<UploadDescriptor> postProcess(ProgressModel<?> progressModel) {
    Optional<UploadDescriptor> result = Optional.empty();
    try {
      ProgressResultModel progressDialogResult = ProgressDialog.createProgressDialog(progressModel);
      if (!progressDialogResult.getResults().isEmpty()) {
        UploadDescriptor uploadedAndImportedDescriptor = (UploadDescriptor) progressDialogResult.getResults().getFirst();
        if (!StringUtils.isEmpty(uploadedAndImportedDescriptor.getError())) {
          Platform.runLater(() -> {
            WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.error") + uploadedAndImportedDescriptor.getError());
          });
          return result;
        }

        result = Optional.of(uploadedAndImportedDescriptor);
      }
    }
    catch (Exception e) {
      LOG.error("Upload post processing failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.post_processing_failed_2"), Messages.get("dialog.please_check_the_log_file_for_details"), Messages.get("dialog.error") + e.getMessage());
      });
    }
    return result;
  }
}
