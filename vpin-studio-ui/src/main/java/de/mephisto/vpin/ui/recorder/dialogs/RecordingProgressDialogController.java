package de.mephisto.vpin.ui.recorder.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.jobs.JobUpdatesListener;
import de.mephisto.vpin.ui.recorder.RecorderController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
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
  private VBox data;

  @FXML
  private Button recordBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Label tablesLabel;

  @FXML
  private Label pTableLabel;

  @FXML
  private Label pTimeLabel;

  @FXML
  private Label pTotalTimeLabel;

  @FXML
  private ProgressBar progressBar;

  private final List<CheckBox> screenCheckboxes = new ArrayList<>();

  private RecorderController recorderController;
  private List<GameRepresentation> games;

  //just a guess!
  private int popperInitAndLoadingTimeSeconds = 6;

  private GameRepresentation game;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
    JobPoller.getInstance().removeListener(this);
    recorderController.doReload();
  }

  @FXML
  private void onStop(ActionEvent e) {
    client.getRecorderService().stopRecording();
    finishRecording();
  }

  @FXML
  private void onRecord(ActionEvent e) {
    recordBtn.setVisible(false);
    stopBtn.setVisible(true);
    progressBar.setDisable(false);

    screenCheckboxes.stream().forEach(c -> c.setDisable(true));

    RecordingData status = new RecordingData();
    status.setGameIds(games.stream().map(g -> g.getId()).collect(Collectors.toList()));
    client.getRecorderService().startRecording(status);
    JobPoller.getInstance().setPolling();
    JobPoller.getInstance().addListener(this);
  }

  @Override
  public void jobsRefreshed(List<JobDescriptor> activeJobs) {
    Platform.runLater(() -> {
      Optional<JobDescriptor> first = activeJobs.stream().filter(j -> j.getJobType().equals(JobType.RECORDER)).findFirst();
      if (first.isPresent()) {
        JobDescriptor jobDescriptor = first.get();
        progressBar.setProgress(jobDescriptor.getProgress());

        int gameId = jobDescriptor.getGameId();
        if (gameId >= 0) {
          if (this.game == null || this.game.getId() != gameId) {
            this.game = client.getGameService().getGame(gameId);
            if (this.game != null) {
              this.pTableLabel.setText(game.getGameDisplayName());
            }
          }
          if (jobDescriptor.getDuration() > 0) {
            this.pTimeLabel.setText(DurationFormatUtils.formatDuration(jobDescriptor.getDuration() * 1000, "HH 'hours', mm 'minutes', ss 'seconds'", false));
          }
          else {
            this.pTimeLabel.setText("?");
          }
        }
      }
      else {
        finishRecording();
      }
    });
  }

  private void finishRecording() {
    JobPoller.getInstance().removeListener(this);
    stopBtn.setVisible(false);
    progressBar.setDisable(true);
    cancelBtn.setVisible(true);

    this.pTableLabel.setText("-");
    this.pTimeLabel.setText("-");
  }

  public void setData(RecorderController recorderController, List<GameRepresentation> games) {
    this.recorderController = recorderController;
    this.games = games;
    tablesLabel.setText(games.size() + " tables selected");
    if (games.size() == 1) {
      tablesLabel.setText(games.get(0).getGameDisplayName());
    }

    refresh();
  }

  private void refresh() {
    this.recorderController.refreshScreens();

    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    List<RecordingScreen> recordingScreens = client.getRecorderService().getRecordingScreens();

    int maxScreenRecordingLength = popperInitAndLoadingTimeSeconds;
    for (RecordingScreen recordingScreen : recordingScreens) {
      RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);

      int screenDuration = option.getInitialDelay() + option.getRecordingDuration();
      if (screenDuration > maxScreenRecordingLength) {
        maxScreenRecordingLength = screenDuration;
      }
    }

    int totalTime = maxScreenRecordingLength * games.size();
    String totalDurationString = DurationFormatUtils.formatDuration(totalTime * 1000, "HH 'hours', mm 'minutes', ss 'seconds'", false);
    pTotalTimeLabel.setText(totalDurationString);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    stopBtn.managedProperty().bindBidirectional(stopBtn.visibleProperty());
    recordBtn.managedProperty().bindBidirectional(recordBtn.visibleProperty());
    cancelBtn.managedProperty().bindBidirectional(cancelBtn.visibleProperty());
    stopBtn.setVisible(false);

    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    List<RecordingScreen> recordingScreens = client.getRecorderService().getRecordingScreens();

    for (RecordingScreen recordingScreen : recordingScreens) {
      RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);
      CheckBox checkBox = new CheckBox();
      checkBox.getStyleClass().add("default-text");
      checkBox.setText(recordingScreen.getDisplay().getName());
      checkBox.setSelected(option.isEnabled());
      checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
        option.setEnabled(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings);

        recorderController.refreshScreens();
        refresh();
      });
      data.getChildren().add(checkBox);
      screenCheckboxes.add(checkBox);
    }
  }

  @Override
  public void onDialogCancel() {
  }
}
