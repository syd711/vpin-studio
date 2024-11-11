package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.ui.recorder.dialogs.RecordingProgressDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

import java.util.List;

public class RecorderDialogs {

  public static void openRecordingDialog(RecorderController recorderController, RecordingDataSummary recordingDataSummary) {
    Stage stage = Dialogs.createStudioDialogStage(RecordingProgressDialogController.class, "recording-progress-dialog.fxml", "Recorder");
    RecordingProgressDialogController controller = (RecordingProgressDialogController) stage.getUserData();
    controller.setData(stage, recorderController, recordingDataSummary);

    stage.showAndWait();
  }
}
