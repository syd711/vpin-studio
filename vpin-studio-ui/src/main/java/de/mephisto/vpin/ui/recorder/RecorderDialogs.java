package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.ui.recorder.dialogs.FFMpegOptionsDialogController;
import de.mephisto.vpin.ui.recorder.dialogs.RecordingProgressDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.stage.Stage;

public class RecorderDialogs {

  public static void openRecordingDialog(RecorderController recorderController, RecordingDataSummary recordingDataSummary) {
    Stage stage = Dialogs.createStudioDialogStage(RecordingProgressDialogController.class, "recording-progress-dialog.fxml", "Recorder");
    RecordingProgressDialogController controller = (RecordingProgressDialogController) stage.getUserData();
    controller.setData(stage, recorderController, recordingDataSummary);

    stage.showAndWait();
  }

  public static void openRecordingSettings(@NonNull VPinScreen vPinScreen) {
    Stage stage = Dialogs.createStudioDialogStage(FFMpegOptionsDialogController.class, "dialog-ffmpeg-options.fxml", "Recorder Settings", "recorderSettings");
    FFMpegOptionsDialogController controller = (FFMpegOptionsDialogController) stage.getUserData();
    controller.setData(vPinScreen);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(600);
    stage.setMinHeight(350);

    stage.showAndWait();
  }
}
