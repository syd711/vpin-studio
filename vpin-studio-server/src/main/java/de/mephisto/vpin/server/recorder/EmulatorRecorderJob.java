package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.util.WindowsUtil;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EmulatorRecorderJob extends FrontendRecorderJob {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorRecorderJob.class);

  public static final int EMULATOR_WAITING_TIMEOUT_SECONDS = 60;

  public EmulatorRecorderJob(RecorderService recorderService, RecorderSettings settings, 
                             RecordingDataSummary recordingDataSummary, List<FrontendPlayerDisplay> recordingScreens) {
    super(recorderService, settings, recordingDataSummary, recordingScreens);
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    FrontendConnector frontend = recorderService.getFrontendConnector();

    LOG.info("***************************** Game Recording Log ******************************************************");
    recorderService.setFrontedEventsEnabled(false);
 
    for (RecordingData data : recordingDataSummary.getRecordingData()) {
      Game game = recorderService.getGame(data);

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

        frontend.killFrontend();
        //killing the frontend will also show the taskbar, so give nircmd a little time.
        Thread.sleep(500);
        NirCmd.setTaskBarVisible(false);

        jobDescriptor.setGameId(game.getId());
        jobDescriptor.setStatus("Launching Emulator");
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }

        updateSingleProgress(jobDescriptor, recordingDataSummary, 25);

        jobDescriptor.setStatus("Launching \"" + game.getGameDisplayName() + "\"");

        recorderService.launchGame(game, recorderSettings);

        int secondToWait = EMULATOR_WAITING_TIMEOUT_SECONDS;
        while (!WindowsUtil.isProcessRunning("Future Pinball", "Visual Pinball Player") && secondToWait > 0) {
          Thread.sleep(1000);
          secondToWait--;
        }

        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled() || secondToWait <= 0) {
          jobDescriptor.setStatus("Timeout waiting for emulator.");
          break;
        }
        updateSingleProgress(jobDescriptor, recordingDataSummary, 35);

        jobDescriptor.setStatus("Recording \"" + game.getGameDisplayName() + "\"");

        //create the game recorder which includes all screens
        gameRecorder = new GameRecorder(frontend, game, recorderSettings, data, jobDescriptor, recordingScreens);
        gameRecorder.startRecording();

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
        //this will kill the emulators too
        frontend.killFrontend();
        gameRecorder.finalizeRecordings();
        recorderService.notifyGameAssetsChanged(game.getId(), AssetType.FRONTEND_MEDIA, null);
      }
    }
    LOG.info("Recordings for " + recordingDataSummary.size() + " games finished.");
    jobDescriptor.setProgress(1);
    jobDescriptor.setGameId(-1);

    recorderService.setFrontedEventsEnabled(true);
    LOG.info("***************************** /Game Recording Log *****************************************************");
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    FrontendConnector frontend = recorderService.getFrontendConnector();
    frontend.killFrontend();
    super.cancel(jobDescriptor);
  }
}
