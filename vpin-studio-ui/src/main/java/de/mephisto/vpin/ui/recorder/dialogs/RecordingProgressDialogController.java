package de.mephisto.vpin.ui.recorder.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.jobs.JobUpdatesListener;
import de.mephisto.vpin.ui.recorder.RecorderController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class RecordingProgressDialogController implements Initializable, DialogController, JobUpdatesListener {
  private final static Logger LOG = LoggerFactory.getLogger(RecordingProgressDialogController.class);

  @FXML
  private Button cancelBtn;

  @FXML
  private Button recordBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Label tablesLabel;

  @FXML
  private Label pTableLabel;

  @FXML
  private Label totalRecordingsLabel;

  @FXML
  private Label statusLabel;

  @FXML
  private ProgressBar progressBar;


  private Stage stage;
  private RecorderController recorderController;
  private RecordingDataSummary recordingDataSummary;

  private GameRepresentation game;
  private JobDescriptor jobDescriptor;
  private Thread jobRefreshThread;
  private boolean finished = false;

  @FXML
  private void onCancelClick(ActionEvent e) {
    stage.close();
  }

  @FXML
  private void onStop(ActionEvent e) {
    client.getRecorderService().stopRecording(jobDescriptor);
    finishRecording(true);
  }

  @FXML
  private void onRecord(ActionEvent e) {
    finished = false;
    recordBtn.setVisible(false);
    cancelBtn.setVisible(false);
    stopBtn.setVisible(true);
    progressBar.setDisable(false);

    progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

    jobDescriptor = client.getRecorderService().startRecording(recordingDataSummary);
    JobPoller.getInstance().setPolling();
    JobPoller.getInstance().addListener(this);

    jobRefreshThread = new Thread(() -> {
      while (!this.jobDescriptor.isFinished() && !this.jobDescriptor.isCancelled()) {
        Platform.runLater(() -> {
          refreshJobStatus();
          progressBar.setProgress(jobDescriptor.getProgress());
        });
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException ex) {
          //ignore
        }
      }

      if (!finished) {
        finishRecording(false);
      }
    });
    jobRefreshThread.start();
  }

  @Override
  public void jobsRefreshed(List<JobDescriptor> activeJobs) {
    Optional<JobDescriptor> first = activeJobs.stream().filter(j -> j.getUuid().equals(this.jobDescriptor.getUuid())).findFirst();
    if (first.isPresent()) {
      this.jobDescriptor = first.get();
    }
    else {
      this.jobDescriptor.setProgress(1);
    }
  }

  private void refreshJobStatus() {
    if (jobDescriptor.getProgress() > 0) {
      progressBar.setProgress(jobDescriptor.getProgress());
    }

    statusLabel.setText(jobDescriptor.getStatus());
    int gameId = jobDescriptor.getGameId();
    if (gameId > 0) {
      if (this.game == null || this.game.getId() != gameId) {
        this.game = client.getGameService().getGame(gameId);
        if (this.game != null) {
          this.pTableLabel.setText(game.getGameDisplayName());
        }
      }
    }

    totalRecordingsLabel.setText("Finished " + jobDescriptor.getTasksExecuted() + " of " + recordingDataSummary.size() + " recordings.");
  }

  private void finishRecording(boolean cancelled) {
    finished = true;
    JobPoller.getInstance().removeListener(this);
    jobDescriptor.setProgress(1);

    //refetch finished job for errors status
    jobDescriptor = client.getJobsService().getJob(jobDescriptor.getUuid());

    Platform.runLater(() -> {
      stopBtn.setVisible(false);
      progressBar.setDisable(true);
      cancelBtn.setVisible(true);

      this.pTableLabel.setText("-");
      this.totalRecordingsLabel.setText("-");
      recorderController.doReload();
      progressBar.setProgress(1);


      Platform.runLater(() -> {
        stage.close();
      });

      if (jobDescriptor.getError() != null) {
        WidgetFactory.showAlert(Studio.stage, "Recording Failed", jobDescriptor.getError(), jobDescriptor.getErrorHint());
      }
      else if (cancelled) {
        ProgressDialog.createProgressDialog(new CancelRecordingProgressModel());
        WidgetFactory.showAlert(Studio.stage, "Recording Cancelled", "The recording has been cancelled.", jobDescriptor.getErrorHint());
      }
      else {
        WidgetFactory.showInformation(Studio.stage, "Recording Finished", "Finished recording of " + recordingDataSummary.size() + " game(s).");
      }
    });
  }

  public void setData(Stage stage, RecorderController recorderController, RecordingDataSummary recordingDataSummary) {
    this.stage = stage;
    this.recorderController = recorderController;
    this.recordingDataSummary = recordingDataSummary;
    tablesLabel.setText(recordingDataSummary.size() + " tables selected");
    if (recordingDataSummary.size() == 1) {
      GameRepresentation game = client.getGameService().getGame(recordingDataSummary.getRecordingData().get(0).getGameId());
      tablesLabel.setText(game.getGameDisplayName());
    }

    refresh();
  }

  private void refresh() {
    this.recorderController.refreshScreens();
    totalRecordingsLabel.setText("Finished 0 of " + recordingDataSummary.size() + " recordings.");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    stopBtn.managedProperty().bindBidirectional(stopBtn.visibleProperty());
    recordBtn.managedProperty().bindBidirectional(recordBtn.visibleProperty());
    cancelBtn.managedProperty().bindBidirectional(cancelBtn.visibleProperty());
    stopBtn.setVisible(false);
  }

  @Override
  public void onDialogCancel() {
  }
}
