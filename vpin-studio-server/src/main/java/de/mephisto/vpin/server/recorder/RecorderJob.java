package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.recorder.RecordMode;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class RecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderJob.class);

  private final FrontendConnector frontend;
  private final FrontendStatusService frontendStatusService;
  private final RecorderSettings settings;
  private final List<Game> games;
  private final List<RecordingScreen> supportedRecodingScreens;

  private GameRecorder gameRecorder;

  public RecorderJob(FrontendConnector frontend, FrontendStatusService frontendStatusService, RecorderSettings settings, List<Game> games, List<RecordingScreen> supportedRecodingScreens) {
    this.frontend = frontend;
    this.frontendStatusService = frontendStatusService;
    this.settings = settings;
    this.games = games;
    this.supportedRecodingScreens = supportedRecodingScreens;
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    LOG.info("***************************** Game Recording Log ******************************************************");
    frontendStatusService.setEventsEnabled(false);
    for (Game game : games) {
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
          updateSingleProgress(jobDescriptor, games, 10);
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
          updateSingleProgress(jobDescriptor, games, 25);

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
          updateSingleProgress(jobDescriptor, games, 35);

          jobDescriptor.setStatus("Recording \"" + game.getGameDisplayName() + "\"");
          gameRecorder = new GameRecorder(frontend, game, settings, supportedRecodingScreens, jobDescriptor, games.size());
          gameRecorder.startRecording();

          updateSingleProgress(jobDescriptor, games, 90);
          LOG.info("Recording for \"" + game.getGameDisplayName() + "\" finished.");
          jobDescriptor.setTasksExecuted(jobDescriptor.getTasksExecuted() + 1);
          double progress = jobDescriptor.getTasksExecuted() * 100d / games.size() / 100d;
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
    LOG.info("Recordings for " + games.size() + " games finished.");
    jobDescriptor.setProgress(1);
    jobDescriptor.setGameId(-1);

    frontendStatusService.setEventsEnabled(true);
    LOG.info("***************************** /Game Recording Log *****************************************************");
  }

  /**
   * Manual update progress when there is only one game recorded.
   *
   * @param jobDescriptor
   * @param games
   * @param progress
   */
  private void updateSingleProgress(JobDescriptor jobDescriptor, List<Game> games, double progress) {
    if(games.size() == 1) {
      jobDescriptor.setProgress(progress / 100d);
    }
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    LOG.info("Cancelling recorder job, " + jobDescriptor.getTasksExecuted() + " of " + this.games.size() + " processed.");
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