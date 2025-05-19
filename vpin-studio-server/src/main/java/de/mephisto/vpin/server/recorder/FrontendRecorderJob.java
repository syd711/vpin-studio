package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.recorder.*;
import de.mephisto.vpin.server.dmd.DMDPositionService;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class FrontendRecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendRecorderJob.class);

  final GameLifecycleService gameLifecycleService;
  final DMDPositionService dmdPositionService;
  final GameService gameService;
  final FrontendConnector frontend;
  final FrontendStatusService frontendStatusService;
  final RecorderSettings recorderSettings;
  final RecordingDataSummary recordingDataSummary;
  final List<FrontendPlayerDisplay> recordingScreens;

  GameRecorder gameRecorder;

  public FrontendRecorderJob(GameLifecycleService gameLifecycleService, DMDPositionService dmdPositionService, GameService gameService, FrontendConnector frontend, FrontendStatusService frontendStatusService, RecorderSettings settings, RecordingDataSummary recordingDataSummary, List<FrontendPlayerDisplay> recordingScreens) {
    this.gameLifecycleService = gameLifecycleService;
    this.dmdPositionService = dmdPositionService;
    this.gameService = gameService;
    this.frontend = frontend;
    this.frontendStatusService = frontendStatusService;
    this.recorderSettings = settings;
    this.recordingDataSummary = recordingDataSummary;
    this.recordingScreens = recordingScreens;
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    LOG.info("***************************** Game Recording Log ******************************************************");
    frontendStatusService.setEventsEnabled(false);
    for (RecordingData data : recordingDataSummary.getRecordingData()) {
      Game game = gameService.getGame(data.getGameId());
      boolean recordingRequired = isRecordingRequired(game);
      if (!recordingRequired) {
        LOG.info("Required assets have been found for \"{}\" or the overwrite option was not enabled, skipping recording.", game.getGameDisplayName());
        jobDescriptor.setTasksExecuted(jobDescriptor.getTasksExecuted() + 1);
        continue;
      }

      LOG.info("************************ \"" + game.getGameDisplayName() + "\" ************************");
      try {
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }

        updateSingleProgress(jobDescriptor, recordingDataSummary, 10);
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }

        jobDescriptor.setGameId(game.getId());
        jobDescriptor.setStatus("Launching Frontend");
        if (!jobDescriptor.isCancelled() && !frontend.startFrontendRecording()) {
          jobDescriptor.setError("Recording cancelled, the frontend could not be launched.");
          jobDescriptor.setErrorHint("Make sure that no frontend processes are running when the recording is started. Check the server logs for details.");
          LOG.error("Recording cancelled, the frontend could not be launched.");
          return;
        }
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }
        updateSingleProgress(jobDescriptor, recordingDataSummary, 25);

        try {
          jobDescriptor.setStatus("Launching \"" + game.getGameDisplayName() + "\"");
          if (!jobDescriptor.isCancelled() && !frontend.startGameRecording(game)) {
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
          gameRecorder = new GameRecorder(frontend, game, recorderSettings, data, jobDescriptor, getRecordingScreensForGame(game));
          gameRecorder.startRecording();
        }
        finally {
          frontend.endGameRecording(game);
        }

        updateSingleProgress(jobDescriptor, recordingDataSummary, 90);
        LOG.info("Recording for \"" + game.getGameDisplayName() + "\" finished.");
        jobDescriptor.setTasksExecuted(jobDescriptor.getTasksExecuted() + 1);
        double progress = jobDescriptor.getTasksExecuted() * 100d / recordingDataSummary.size() / 100d;
        jobDescriptor.setProgress(progress);
      }
      catch (Exception e) {
        LOG.error("Game recording failed: {}", e.getMessage(), e);
      }
      finally {
        frontend.endFrontendRecording();
        gameRecorder.finalizeRecordings();
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.FRONTEND_MEDIA, null);
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
  protected void updateSingleProgress(JobDescriptor jobDescriptor, RecordingDataSummary recordingDataSummary, double progress) {
    if (recordingDataSummary.size() == 1) {
      jobDescriptor.setProgress(progress / 100d);
    }
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    frontend.killFrontend();
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

  protected boolean isRecordingRequired(Game game) {
    RecordingData recordingData = recordingDataSummary.get(game.getId());
    if (recordingData != null) {
      List<VPinScreen> screens = recordingData.getScreens();
      for (VPinScreen screen : screens) {
        RecordingScreenOptions recordingScreenOption = recorderSettings.getRecordingScreenOption(screen);
        if (!recordingScreenOption.isEnabled()) {
          continue;
        }

        if (recordingScreenOption.getRecordMode().equals(RecordingWriteMode.overwrite)) {
          return true;
        }

        if (recordingScreenOption.getRecordMode().equals(RecordingWriteMode.append)) {
          return true;
        }

        if (recordingScreenOption.getRecordMode().equals(RecordingWriteMode.ifMissing)) {
          List<File> screenMediaFiles = frontend.getMediaAccessStrategy().getScreenMediaFiles(game, screen);
          if (screenMediaFiles.isEmpty()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Updates the recording screens with custom positions relevant for the given game.
   * E.g. DMDs may have a custom position.
   *
   * @param game the game to customize the recording screens for
   * @return the adapted screens
   */
  protected List<FrontendPlayerDisplay> getRecordingScreensForGame(Game game) {
    return recordingScreens;
  }
}
