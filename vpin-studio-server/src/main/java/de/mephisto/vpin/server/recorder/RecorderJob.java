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
import java.util.concurrent.*;

public class RecorderJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderJob.class);

  private final MediaAccessStrategy mediaAccessStrategy;
  private final RecorderSettings settings;
  private final List<Game> games;
  private final List<RecordingScreen> supportedRecodingScreens;
  private final List<FrontendPlayerDisplay> frontendPlayerDisplays;

  private boolean cancelled = false;
  private Future<GameRecordingStatus> recordingFuture;

  public RecorderJob(MediaAccessStrategy mediaAccessStrategy, RecorderSettings settings, List<Game> games, List<RecordingScreen> supportedRecodingScreens, List<FrontendPlayerDisplay> frontendPlayerDisplays) {
    this.mediaAccessStrategy = mediaAccessStrategy;
    this.settings = settings;
    this.games = games;
    this.supportedRecodingScreens = supportedRecodingScreens;
    this.frontendPlayerDisplays = frontendPlayerDisplays;
  }

  @Override
  public void execute(JobDescriptor jobDescriptor) {
    int processed = 0;
    for (Game game : games) {
      try {
        if (cancelled) {
          return;
        }

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        recordingFuture = executorService.submit(new GameRecorderRunnable(mediaAccessStrategy, game, settings, supportedRecodingScreens, jobDescriptor));
        GameRecordingStatus status = recordingFuture.get();

        processed++;

        double progress = (double) (processed * 100) / games.size();
        jobDescriptor.setProgress(progress);
      }
      catch (Exception e) {
        LOG.error("Game recording failed: {}", e.getMessage(), e);
      }
    }
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    if (!recordingFuture.isCancelled()) {
      recordingFuture.cancel(true);
    }
    cancelled = true;
  }

  @Override
  public boolean isCancelable() {
    return true;
  }
}
