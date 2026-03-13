package de.mephisto.vpin.ui.recorder.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.restclient.recorder.RecordingMode;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.jobs.JobUpdatesListener;
import de.mephisto.vpin.ui.recorder.RecorderController;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerDialogController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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

  @FXML
  private Pane recordingProgressPanel;

  @FXML
  private Pane emulatorRecordingPanel;
  @FXML
  private RadioButton emulatorRecordingRadio;
  @FXML
  private Pane frontendRecordingPanel;
  @FXML
  private RadioButton frontendRecordingRadio;

  @FXML
  private CheckBox customLauncherCheckbox;

  @FXML
  private CheckBox primaryCheckbox;

  @FXML
  private ComboBox<String> launcherCombo;


  private Stage stage;
  private RecorderController recorderController;
  private RecordingDataSummary recordingDataSummary;

  private GameRepresentation game;
  private JobDescriptor jobDescriptor;
  private Thread jobRefreshThread;
  private boolean finished = false;
  private RecorderSettings settings;

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
    TableAssetManagerDialogController.close();

    if (client.getSystemService().isLocal()) {
      new Thread(() -> {
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException ex) {
          //ignore
        }

        Platform.runLater(() -> {
          Studio.stage.setIconified(true);
        });
      }).start();

    }

    finished = false;
    recordBtn.setVisible(false);
    cancelBtn.setVisible(false);
    stopBtn.setVisible(true);
    progressBar.setDisable(false);

    frontendRecordingRadio.setDisable(true);
    emulatorRecordingRadio.setDisable(true);
    launcherCombo.setDisable(true);
    customLauncherCheckbox.setDisable(true);
    primaryCheckbox.setDisable(true);

    recordingProgressPanel.getStyleClass().add("selection-panel-selected");
    emulatorRecordingPanel.getStyleClass().remove("selection-panel-selected");
    frontendRecordingRadio.getStyleClass().remove("selection-panel-selected");

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

    totalRecordingsLabel.setText("Finished " + jobDescriptor.getTasksExecuted() + " of " + recordingDataSummary.size() + " recordings, recorded " + jobDescriptor.getUserData() + " video(s).");
  }

  private void finishRecording(boolean cancelled) {
    finished = true;
    JobPoller.getInstance().removeListener(this);
    jobDescriptor.setProgress(1);

    //re-fetch finished job for errors status
    jobDescriptor = client.getJobsService().getJob(jobDescriptor.getUuid());

    Platform.runLater(() -> {
      Studio.stage.setIconified(false);

      stopBtn.setVisible(false);
      progressBar.setDisable(true);
      cancelBtn.setVisible(true);

      this.pTableLabel.setText("-");
      this.totalRecordingsLabel.setText("-");
      //recorderController.doReload();
      progressBar.setProgress(1);

      Platform.runLater(() -> {
        stage.close();
        if (game != null) {
          //give the server some time to detect the new media files
          try {
            Thread.sleep(2000);
          }
          catch (InterruptedException e) {
            //ignore
          }
          EventManager.getInstance().notifyTableChange(game.getId(), null);
        }
      });

      if (jobDescriptor.getError() != null) {
        WidgetFactory.showAlert(Studio.stage, "Recording Failed", jobDescriptor.getError(), jobDescriptor.getErrorHint());
      }
      else if (cancelled) {
        ProgressDialog.createProgressDialog(new CancelRecordingProgressModel());
        WidgetFactory.showAlert(Studio.stage, "Recording Cancelled", "The recording has been cancelled.", jobDescriptor.getErrorHint());
      }
      else {
        String game = "game";
        String video = "video";
        if (recordingDataSummary.size() > 1) {
          game = "games";
        }
        if (((int) jobDescriptor.getUserData()) > 1) {
          video = "videos";
        }
        WidgetFactory.showInformation(Studio.stage, "Recording Finished", "Finished recording of " + recordingDataSummary.size() + " " + game + ", recorded " + jobDescriptor.getUserData() + " " + video + ".");
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

    List<RecordingData> recordingData = recordingDataSummary.getRecordingData();
    for (RecordingData data : recordingData) {
      GameRepresentation game = client.getGameService().getGame(data.getGameId());
      if (!client.getEmulatorService().isVpxGame(game)) {
        frontendRecordingRadio.setSelected(true);
        emulatorRecordingRadio.setDisable(true);
        launcherCombo.setDisable(true);
        customLauncherCheckbox.setDisable(true);
        primaryCheckbox.setDisable(true);
        break;
      }
      else {
        GameEmulatorRepresentation gameEmulator= client.getEmulatorService().getGameEmulator(game.getEmulatorId());
        List<String> altExeNames = client.getEmulatorService().getAltExeNames(gameEmulator.getId());
        launcherCombo.setItems(FXCollections.observableList(altExeNames));
        break;
      }
    }


    refresh();
  }

  private void refresh() {
    this.recorderController.refreshScreens();
    totalRecordingsLabel.setText("Finished 0 of " + recordingDataSummary.size() + " recordings.");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);

    stopBtn.managedProperty().bindBidirectional(stopBtn.visibleProperty());
    recordBtn.managedProperty().bindBidirectional(recordBtn.visibleProperty());
    cancelBtn.managedProperty().bindBidirectional(cancelBtn.visibleProperty());
    stopBtn.setVisible(false);

    ToggleGroup toggleGroup = new ToggleGroup();
    emulatorRecordingRadio.setToggleGroup(toggleGroup);
    frontendRecordingRadio.setToggleGroup(toggleGroup);

    if (settings.getRecordingMode() == null || settings.getRecordingMode().equals(RecordingMode.emulator)) {
      frontendRecordingPanel.getStyleClass().remove("selection-panel-selected");
      emulatorRecordingRadio.setSelected(true);
    }
    else {
      emulatorRecordingPanel.getStyleClass().remove("selection-panel-selected");
      frontendRecordingRadio.setSelected(true);
    }

    emulatorRecordingRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!emulatorRecordingPanel.getStyleClass().contains("selection-panel-selected")) {
            emulatorRecordingPanel.getStyleClass().add("selection-panel-selected");
          }
          frontendRecordingPanel.getStyleClass().remove("selection-panel-selected");
          settings.setRecordingMode(RecordingMode.emulator);
        }
        else {
          frontendRecordingPanel.getStyleClass().remove("selection-panel-selected");
        }
        client.getPreferenceService().setJsonPreference(settings);
      }
    });

    frontendRecordingRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue) {
          if (!frontendRecordingPanel.getStyleClass().contains("selection-panel-selected")) {
            frontendRecordingPanel.getStyleClass().add("selection-panel-selected");
          }
          emulatorRecordingPanel.getStyleClass().remove("selection-panel-selected");
          settings.setRecordingMode(RecordingMode.frontend);
        }
        else {
          emulatorRecordingPanel.getStyleClass().remove("selection-panel-selected");
        }
        client.getPreferenceService().setJsonPreference(settings);
      }
    });

    if (!StringUtils.isEmpty(settings.getCustomLauncher())) {
      launcherCombo.setValue(settings.getCustomLauncher());
    }

    customLauncherCheckbox.setSelected(settings.isCustomLauncherEnabled());
    customLauncherCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        settings.setCustomLauncherEnabled(newValue);
        launcherCombo.setDisable(!newValue);
        primaryCheckbox.setDisable(!newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
    });

    primaryCheckbox.setSelected(settings.isPrimaryParam());
    primaryCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        settings.setPrimaryParam(newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
    });


    launcherCombo.setDisable(!settings.isCustomLauncherEnabled());
    launcherCombo.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        settings.setCustomLauncher(newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
    });
  }

  @Override
  public void onDialogCancel() {
  }
}
