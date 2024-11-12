package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.recorder.*;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class RecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderJob.class);

  private final GameService gameService;
  private final FrontendConnector frontend;
  private final FrontendStatusService frontendStatusService;
  private final RecorderSettings settings;
  private final RecordingDataSummary recordingDataSummary;
  private final List<RecordingScreen> recordingScreens;

  private GameRecorder gameRecorder;

  public RecorderJob(GameService gameService, FrontendConnector frontend, FrontendStatusService frontendStatusService, RecorderSettings settings, RecordingDataSummary recordingDataSummary, List<RecordingScreen> recordingScreens) {
    this.gameService = gameService;
    this.frontend = frontend;
    this.frontendStatusService = frontendStatusService;
    this.settings = settings;
    this.recordingDataSummary = recordingDataSummary;
    this.recordingScreens = recordingScreens;
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    LOG.info("***************************** Game Recording Log ******************************************************");
    frontendStatusService.setEventsEnabled(false);
    for (RecordingData data : recordingDataSummary.getRecordingData()) {
      Game game = gameService.getGame(data.getGameId());
      LOG.info("************************ \"" + game.getGameDisplayName() + "\" ************************");
      try {
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }

        boolean recordingRequired = isRecordingRequired(game);
        if (!recordingRequired) {
          LOG.info("Assets found or no overwrite not enabled, skipping recording.");
          jobDescriptor.setTasksExecuted(jobDescriptor.getTasksExecuted() + 1);
        }
        else {
          frontend.initializeRecording();
          updateSingleProgress(jobDescriptor, recordingDataSummary, 10);
          if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
            break;
          }

          jobDescriptor.setGameId(game.getId());
          jobDescriptor.setStatus("Launching Frontend");
          if (!jobDescriptor.isCancelled() && !frontend.restartFrontend(true)) {
            jobDescriptor.setError("Recording cancelled, the frontend could not be launched.");
            jobDescriptor.setErrorHint("Make sure that no frontend processes are running when the recording is started. Check the server logs for details.");
            LOG.error("Recording cancelled, the frontend could not be launched.");
            return;
          }
          if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
            break;
          }
          updateSingleProgress(jobDescriptor, recordingDataSummary, 25);

          jobDescriptor.setStatus("Launching \"" + game.getGameDisplayName() + "\"");
          if (!jobDescriptor.isCancelled() && !frontend.launchGame(game, true)) {
            jobDescriptor.setError("Recording cancelled, the game could not be launched.");
            jobDescriptor.setErrorHint("Make sure that no frontend processes are running when the recording is started. Check the server logs for details.");
            LOG.error("Recording cancelled, the game could not be launched.");
            return;
          }
          if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
            break;
          }
          updateSingleProgress(jobDescriptor, recordingDataSummary, 35);

          jobDescriptor.setStatus("Recording \"" + game.getGameDisplayName() + "\"");

          //create the game recorder which includes all screens
          gameRecorder = new GameRecorder(frontend, game, settings, data, jobDescriptor, recordingDataSummary.size(), recordingScreens);
          gameRecorder.startRecording();

          updateSingleProgress(jobDescriptor, recordingDataSummary, 90);
          LOG.info("Recording for \"" + game.getGameDisplayName() + "\" finished.");
          jobDescriptor.setTasksExecuted(jobDescriptor.getTasksExecuted() + 1);
          double progress = jobDescriptor.getTasksExecuted() * 100d / recordingDataSummary.size() / 100d;
          jobDescriptor.setProgress(progress);
        }
      }
      catch (Exception e) {
        LOG.error("Game recording failed: {}", e.getMessage(), e);
      }
      finally {
        frontend.finalizeRecording();
        frontend.killFrontend();
      }
    }
    LOG.info("Recordings for " + recordingDataSummary.size() + " games finished.");
    jobDescriptor.setProgress(1);
    jobDescriptor.setGameId(-1);

    frontendStatusService.setEventsEnabled(true);
    LOG.info("***************************** /Game Recording Log *****************************************************");
  }

  /**
   * Manual update progress when there is only one game recorded.
   *
   * @param jobDescriptor
   * @param recordingDataSummary
   * @param progress
   */
  private void updateSingleProgress(JobDescriptor jobDescriptor, RecordingDataSummary recordingDataSummary, double progress) {
    if(recordingDataSummary.size() == 1) {
      jobDescriptor.setProgress(progress / 100d);
    }
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    LOG.info("Cancelling recorder job, " + jobDescriptor.getTasksExecuted() + " of " + this.recordingDataSummary.size() + " processed.");
    if (gameRecorder != null) {
      gameRecorder.cancel(jobDescriptor);
    }
    jobDescriptor.setProgress(1);
  }

  @Override
  public boolean isCancelable() {
    return true;
  }

  private boolean isRecordingRequired(Game game) {
    List<RecordingScreenOptions> recordingScreenOptions = settings.getRecordingScreenOptions();
    for (RecordingScreenOptions recordingScreenOption : recordingScreenOptions) {
      if (!recordingScreenOption.isEnabled()) {
        continue;
      }

      if (recordingScreenOption.getRecordMode().equals(RecordMode.overwrite)) {
        return true;
      }

      if (recordingScreenOption.getRecordMode().equals(RecordMode.append)) {
        return true;
      }

      if (recordingScreenOption.getRecordMode().equals(RecordMode.ifMissing)) {
        VPinScreen screen = VPinScreen.valueOfScreen(recordingScreenOption.getDisplayName());
        List<File> screenMediaFiles = frontend.getMediaAccessStrategy().getScreenMediaFiles(game, screen);
        if (screenMediaFiles.isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }
}
