package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingDataSummary;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.server.fp.FPService;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.WindowsUtil;
import de.mephisto.vpin.server.vpx.VPXService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class EmulatorRecorderJob extends FrontendRecorderJob {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorRecorderJob.class);
  private final VPXService vpxService;
  private final FPService fpService;

  GameRecorder gameRecorder;

  public EmulatorRecorderJob(GameService gameService, VPXService vpxService, FPService fpService, FrontendConnector frontend, FrontendStatusService frontendStatusService, RecorderSettings settings, RecordingDataSummary recordingDataSummary, List<RecordingScreen> recordingScreens) {
    super(gameService, frontend, frontendStatusService, settings, recordingDataSummary, recordingScreens);
    this.vpxService = vpxService;
    this.fpService = fpService;
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

        frontend.killFrontend();

        jobDescriptor.setGameId(game.getId());
        jobDescriptor.setStatus("Launching Emulator");
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }

        updateSingleProgress(jobDescriptor, recordingDataSummary, 25);

        jobDescriptor.setStatus("Launching \"" + game.getGameDisplayName() + "\"");

        String altExe = recorderSettings.getCustomLauncher();
        if (!recorderSettings.isCustomLauncherEnabled()) {
          altExe = null;
        }

        if (game.getEmulator().isVpxEmulator()) {
          vpxService.play(game, altExe);
        }
        else if (game.getEmulator().isFpEmulator()) {
          fpService.play(game, altExe);
        }
        else {
          throw new UnsupportedOperationException("Unsupported emulator: " + game.getEmulator());
        }

        int secondToWait = 30;
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
        gameRecorder = new GameRecorder(frontend, game, recorderSettings, data, jobDescriptor, recordingDataSummary.size(), recordingScreens);
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
      }
    }
    LOG.info("Recordings for " + recordingDataSummary.size() + " games finished.");
    jobDescriptor.setProgress(1);
    jobDescriptor.setGameId(-1);

    frontendStatusService.setEventsEnabled(true);
    LOG.info("***************************** /Game Recording Log *****************************************************");
  }
}
