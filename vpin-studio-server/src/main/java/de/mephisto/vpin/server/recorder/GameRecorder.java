package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.*;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameMediaService;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
  private final RecordingData recordingData;
  private final JobDescriptor jobDescriptor;
  private final int totalGamesToRecord;
  private final List<RecordingScreen> recordingScreens;

  private final List<Future<RecordingResult>> futures = new ArrayList<>();
  private final List<ScreenRecorder> screenRecorders = new ArrayList<>();

  private int totalTime;

  public GameRecorder(FrontendConnector frontend, Game game, RecorderSettings recorderSettings, RecordingData recordingData, JobDescriptor jobDescriptor, int totalGamesToRecord, List<RecordingScreen> recordingScreens) {
    this.frontend = frontend;
    this.game = game;
    this.recorderSettings = recorderSettings;
    this.recordingData = recordingData;
    this.jobDescriptor = jobDescriptor;
    this.totalGamesToRecord = totalGamesToRecord;
    this.recordingScreens = recordingScreens;
  }

  public RecordingResult startRecording() {
    LOG.info("Launching recording of \"" + game.getGameDisplayName() + "\"");
    RecordingResult status = new RecordingResult();

    List<Callable<RecordingResult>> callables = new ArrayList<>();
    for (VPinScreen screen : recordingData.getScreens()) {
      RecordingScreenOptions option = recorderSettings.getRecordingScreenOption(screen);
      File target = resolveTargetFile(game, screen, option.getRecordMode());
      if (target == null) {
        LOG.info("Skipped recording for " + screen + ", asset not missing.");
        continue;
      }

      RecordingScreen recordingScreen = recordingScreens.stream().filter(s -> s.getScreen().equals(screen)).findFirst().get();
      int totalDuration = option.getRecordingDuration() + option.getInitialDelay();
      if (totalDuration > totalTime) {
        totalTime = totalDuration;
      }
      if (option.isEnabled()) {
        Callable<RecordingResult> screenRecordable = new Callable<>() {
          @Override
          public RecordingResult call() {
            LOG.info("Starting recording for \"" + game.getGameDisplayName() + "\", " + screen.name() + ": " + target.getAbsolutePath());
            recorderSettings.getRecordingScreenOption(screen);
            ScreenRecorder screenRecorder = new ScreenRecorder(recordingScreen, target);
            screenRecorders.add(screenRecorder);
            RecordingResult result =  screenRecorder.record(option);
            // last renaming of file once done
            switchTargetFile(game, screen, option.getRecordMode(), target);
            return result;
          }
        };
        callables.add(screenRecordable);
      }
    }

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
      for (Future<RecordingResult> future : futures) {
        future.cancel(true);
      }

      for (ScreenRecorder screenRecorder : screenRecorders) {
        // partially generated target file will be deleted here 
        screenRecorder.cancel();
      }

      LOG.info("Finished game recorder cancellation.");
    }
    catch (Exception e) {
      jobDescriptor.setError(e.getMessage());
      LOG.error("Cancellation failed: {}", e.getMessage(), e);
    }
  }

  @Nullable
  private File resolveTargetFile(Game game, VPinScreen screen, RecordMode recordMode) {
    // when several folder possible for a VpinScreen like in pinballX, get the ones for mp4 
    File mediaFolder = frontend.getMediaAccessStrategy().getGameMediaFolder(game, screen, "mp4");
    File target = GameMediaService.buildMediaAsset(mediaFolder, game, "mp4", true);
    switch (recordMode) {
      case overwrite: {
        // even with overwrite, generate in a new file and do the switch at the end if success
        return target;
      }
      case ifMissing: {
        List<File> screenMediaFiles = frontend.getMediaAccessStrategy().getScreenMediaFiles(game, screen);
        // even with ifMissing, generate in a new file, but only if there is no existing file
        return !screenMediaFiles.isEmpty() ? null : target;
      }
      case append: {
        return target;
      }
    }
    throw new UnsupportedOperationException("Invalid record mode " + recordMode);
  }

  @Nullable
  private void switchTargetFile(Game game, VPinScreen screen, RecordMode recordMode, File recordedFile) {
    // when several folder possible for a VpinScreen like in pinballX, get the ones for mp4 
    File mediaFolder = frontend.getMediaAccessStrategy().getGameMediaFolder(game, screen, "mp4");
    File target = GameMediaService.buildMediaAsset(mediaFolder, game, "mp4", false);

    switch (recordMode) {
      case overwrite: {
        List<File> screenMediaFiles = frontend.getMediaAccessStrategy().getScreenMediaFiles(game, screen);
        // delete existing files in the generated folder only whatever their format (and there maybe several, not only mp4)
        // why only in the generated folder is because pinballX has separated folders for images that need to stay
        if (!screenMediaFiles.isEmpty()) {
          for (File screenMediaFile : screenMediaFiles) {
            if (screenMediaFile.getParentFile().equals(mediaFolder) &&
                StringUtils.equalsIgnoreCase(FilenameUtils.getBaseName(screenMediaFile.getName()), game.getGameName())) {
              screenMediaFile.delete();
            }
          }
        }
        recordedFile.renameTo(target);
      }
      case ifMissing: {
        // as no file was there (cf resolveTargetFile method), 
        // nothing to do here because the recordedFile is the target file already
        return;
      }
      case append: {
        // simply switch recorded and target files and keep all other files and format
        if (!StringUtils.equalsIgnoreCase(target.getName(), recordedFile.getName())) {
          // another temporary not existing file that will be deleted
          File tempFile = GameMediaService.buildMediaAsset(mediaFolder, game, "mp4", true);
          if (target.renameTo(tempFile)) {
            if (recordedFile.renameTo(target)) {
              tempFile.renameTo(recordedFile);
            }
          }
        }
      }
    }

    throw new UnsupportedOperationException("Invalid record mode " + recordMode);
  }
}
