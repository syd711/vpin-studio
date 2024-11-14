package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.notifications.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InGameRecorderJob extends RecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(InGameRecorderJob.class);
  private final NotificationService notificationService;

  public InGameRecorderJob(NotificationService notificationService, GameService gameService, FrontendConnector frontend, FrontendStatusService frontendStatusService, RecorderSettings settings, RecordingDataSummary recordingDataSummary, List<RecordingScreen> recordingScreens) {
    super(gameService, frontend, frontendStatusService, settings, recordingDataSummary, recordingScreens);
    this.notificationService = notificationService;
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    LOG.info("***************************** In-Game Recording Log ******************************************************");
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

        try {
          jobDescriptor.setStatus("Recording \"" + game.getGameDisplayName() + "\"");

          //create the game recorder which includes all screens
          gameRecorder = new GameRecorder(frontend, game, settings, data, jobDescriptor, recordingDataSummary.size(), recordingScreens);
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
    }
    LOG.info("Recordings for " + recordingDataSummary.size() + " games finished.");
    jobDescriptor.setProgress(1);
    jobDescriptor.setGameId(-1);

    LOG.info("***************************** /In-Game Recording Log *****************************************************");
  }
}
