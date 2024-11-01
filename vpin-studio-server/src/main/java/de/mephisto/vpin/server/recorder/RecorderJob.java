package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class RecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderJob.class);

  private final FrontendConnector frontend;
  private final RecorderSettings settings;
  private final List<Game> games;
  private final List<RecordingScreen> supportedRecodingScreens;
  private final List<FrontendPlayerDisplay> frontendPlayerDisplays;

  private GameRecorder gameRecorder;

  public RecorderJob(FrontendConnector frontend, RecorderSettings settings, List<Game> games, List<RecordingScreen> supportedRecodingScreens, List<FrontendPlayerDisplay> frontendPlayerDisplays) {
    this.frontend = frontend;
    this.settings = settings;
    this.games = games;
    this.supportedRecodingScreens = supportedRecodingScreens;
    this.frontendPlayerDisplays = frontendPlayerDisplays;
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    for (Game game : games) {
      try {
        if (jobDescriptor.isFinished() || jobDescriptor.isCancelled()) {
          break;
        }

        frontend.initializeRecording();

        jobDescriptor.setGameId(game.getId());
        jobDescriptor.setStatus("Launching Frontend");
        if (!frontend.restartFrontend(true)) {
          jobDescriptor.setError("Recording cancelled, the frontend could not be launched.");
          jobDescriptor.setErrorHint("Make sure that no frontend processes are running when the recording is started. Check the server logs for details.");
          LOG.error("Recording cancelled, the frontend could not be launched.");
          return;
        }

        jobDescriptor.setStatus("Launching \"" + game.getGameDisplayName() + "\"");
        if (!frontend.launchGame(game, true)) {
          jobDescriptor.setError("Recording cancelled, the game could not be launched.");
          jobDescriptor.setErrorHint("Make sure that no frontend processes are running when the recording is started. Check the server logs for details.");
          LOG.error("Recording cancelled, the game could not be launched.");
          return;
        }

        long start = System.currentTimeMillis();
        jobDescriptor.setStatus("Recording \"" + game.getGameDisplayName() + "\"");
        gameRecorder = new GameRecorder(frontend, game, settings, supportedRecodingScreens, jobDescriptor, games.size());
        gameRecorder.startRecording();

        double progress = (double) (jobDescriptor.getTasksExecuted() * 100) / games.size() / 100;
        if (jobDescriptor.getProgress() < 1) {
          jobDescriptor.setProgress(progress);
        }
        LOG.info("Recording for \"" + game.getGameDisplayName() + "\" finished.");
        jobDescriptor.setTasksExecuted(jobDescriptor.getTasksExecuted() + 1);

        long duration = System.currentTimeMillis() - start;
        jobDescriptor.setTaskDurationSeconds((int) (duration / 1000));
      }
      catch (Exception e) {
        LOG.error("Game recording failed: {}", e.getMessage(), e);
      }
      finally {
        frontend.killFrontend();
        frontend.finalizeRecording();
      }
    }
    LOG.info("Recordings for " + games.size() + " games finished.");
    jobDescriptor.setProgress(1);
    jobDescriptor.setGameId(-1);
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    LOG.info("Cancelling recorder job, " + jobDescriptor.getTasksExecuted() + " of " + this.games.size() + " processed.");
    gameRecorder.cancel(jobDescriptor);
    jobDescriptor.setProgress(1);
  }

  @Override
  public boolean isCancelable() {
    return true;
  }
}
