package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.games.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderJob.class);

  private final MediaAccessStrategy mediaAccessStrategy;
  private final RecorderSettings settings;
  private final List<Game> games;
  private final List<RecordingScreen> supportedRecodingScreens;
  private final List<FrontendPlayerDisplay> frontendPlayerDisplays;

  private boolean cancelled = false;
  private GameRecorder gameRecorder;
  private int processed;

  public RecorderJob(MediaAccessStrategy mediaAccessStrategy, RecorderSettings settings, List<Game> games, List<RecordingScreen> supportedRecodingScreens, List<FrontendPlayerDisplay> frontendPlayerDisplays) {
    this.mediaAccessStrategy = mediaAccessStrategy;
    this.settings = settings;
    this.games = games;
    this.supportedRecodingScreens = supportedRecodingScreens;
    this.frontendPlayerDisplays = frontendPlayerDisplays;
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    processed = 0;
    for (Game game : games) {
      try {
        if (cancelled) {
          return;
        }

        jobDescriptor.setGameId(game.getId());
        gameRecorder = new GameRecorder(mediaAccessStrategy, game, settings, supportedRecodingScreens, jobDescriptor);
        gameRecorder.startRecording();

        processed++;

        double progress = (double) (processed * 100) / games.size();
        jobDescriptor.setProgress(progress);
        LOG.info("Recording for \"" + game.getGameDisplayName() + "\" finished.");
      }
      catch (Exception e) {
        LOG.error("Game recording failed: {}", e.getMessage(), e);
      }
    }
    LOG.info("Recordings for " + games.size() + " games finished.");
    jobDescriptor.setProgress(1);
    jobDescriptor.setGameId(-1);
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    LOG.info("Cancelling recorder job, " + processed + " of " + this.games.size() + " processsed.");
    gameRecorder.cancel(jobDescriptor);
  }

  @Override
  public boolean isCancelable() {
    return true;
  }
}
