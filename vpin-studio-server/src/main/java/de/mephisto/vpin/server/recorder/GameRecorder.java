package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameMediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GameRecorder {
  private final static Logger LOG = LoggerFactory.getLogger(GameRecorder.class);

  private final FrontendConnector frontend;
  private final Game game;
  private final RecorderSettings recorderSettings;
  private final List<RecordingScreen> supportedRecodingScreens;
  private final JobDescriptor jobDescriptor;
  private final int totalGamesToRecord;

  private final List<Future<RecordingResult>> futures = new ArrayList<>();
  private final List<ScreenRecorder> screenRecorders = new ArrayList<>();
  private Thread jobUpdater;

  private boolean finished = false;

  private int waitingTime;
  private int totalTime;

  public GameRecorder(FrontendConnector frontend, Game game, RecorderSettings recorderSettings, List<RecordingScreen> supportedRecodingScreens, JobDescriptor jobDescriptor, int totalGamesToRecord) {
    this.frontend = frontend;
    this.game = game;
    this.recorderSettings = recorderSettings;
    this.supportedRecodingScreens = supportedRecodingScreens;
    this.jobDescriptor = jobDescriptor;
    this.totalGamesToRecord = totalGamesToRecord;
  }

  public RecordingResult startRecording() {
    LOG.info("Launching recording of \"" + game.getGameDisplayName() + "\"");
    RecordingResult status = new RecordingResult();

    List<Callable<RecordingResult>> callables = new ArrayList<>();
    for (RecordingScreen screen : supportedRecodingScreens) {
      RecordingScreenOptions option = recorderSettings.getRecordingScreenOption(screen);
      int totalDuration = option.getRecordingDuration() + option.getInitialDelay();
      if (totalDuration > totalTime) {
        totalTime = totalDuration;
      }
      if (option.isEnabled()) {
        Callable<RecordingResult> screenRecordable = new Callable<>() {
          @Override
          public RecordingResult call() {
            File mediaFolder = frontend.getMediaAccessStrategy().getGameMediaFolder(game, screen.getScreen(), null);
            File target = GameMediaService.buildMediaAsset(mediaFolder, game, "mp4", false);

            LOG.info("Starting recording for \"" + game.getGameDisplayName() + "\", " + screen.getScreen().name() + ": " + target.getAbsolutePath());
            ScreenRecorder screenRecorder = new ScreenRecorder(screen, target);
            screenRecorders.add(screenRecorder);
            return screenRecorder.record(option);
          }
        };
        callables.add(screenRecordable);
      }
    }

    waitingTime = totalTime;

    ExecutorService executorService = Executors.newFixedThreadPool(callables.size());
    for (Callable<RecordingResult> callable : callables) {
      Future<RecordingResult> submit = executorService.submit(callable);
      futures.add(submit);
    }

    try {
      for (Future<RecordingResult> future : futures) {
        RecordingResult recordingResult = future.get();
        LOG.info("Recording finished: {}", recordingResult.toString());
      }
    }
    catch (Exception e) {
      LOG.error("Error waiting for recording result: {}", e.getMessage(), e);
    }
    return status;
  }

  public void cancel(JobDescriptor jobDescriptor) {
    try {
      finished = true;

      for (Future<RecordingResult> future : futures) {
        future.cancel(true);
      }

      for (ScreenRecorder screenRecorder : screenRecorders) {
        screenRecorder.cancel();
      }

      LOG.info("Finished game recorder cancellation.");
    }
    catch (Exception e) {
      jobDescriptor.setError(e.getMessage());
      LOG.error("Cancellation failed: {}", e.getMessage(), e);
    }
  }
}