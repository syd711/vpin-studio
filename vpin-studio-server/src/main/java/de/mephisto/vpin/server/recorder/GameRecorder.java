package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingData;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.restclient.recorder.RecordingWriteMode;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.games.Game;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GameRecorder {
  private final static Logger LOG = LoggerFactory.getLogger(GameRecorder.class);
  private final static int TIMEOUT_MINUTES = 10;

  private final FrontendConnector frontend;
  private final Game game;
  private final RecorderSettings recorderSettings;
  private final RecordingData recordingData;
  private final JobDescriptor jobDescriptor;
  private final List<FrontendPlayerDisplay> recordingScreens;

  private final List<Future<RecordingResult>> futures = new ArrayList<>();
  private final List<ScreenRecorder> screenRecorders = new ArrayList<>();

  private final List<RecordingResult> recordingResults = new ArrayList<>();

  private int totalTime;

  public GameRecorder(FrontendConnector frontend, Game game, RecorderSettings recorderSettings, RecordingData recordingData, JobDescriptor jobDescriptor, List<FrontendPlayerDisplay> recordingScreens) {
    this.frontend = frontend;
    this.game = game;
    this.recorderSettings = recorderSettings;
    this.recordingData = recordingData;
    this.jobDescriptor = jobDescriptor;
    this.recordingScreens = recordingScreens;
  }

  public RecordingResult startRecording() {
    LOG.info("Launching recording of \"{}\"", game.getGameDisplayName());
    RecordingResult status = new RecordingResult();

    List<Callable<RecordingResult>> callables = new ArrayList<>();
    for (VPinScreen screen : recordingData.getScreens()) {
      try {
        RecordingScreenOptions option = validateScreen(screen);
        if (option == null) {
          continue;
        }

        File recordingTempFile = createTemporaryRecordingFile(game, screen, option.getRecordMode());
        List<FrontendPlayerDisplay> collect = recordingScreens.stream().filter(s -> s.getScreen().equals(screen)).toList();
        if (collect.isEmpty()) {
          continue;
        }
        FrontendPlayerDisplay recordingScreen = collect.getFirst();
        int totalDuration = option.getRecordingDuration() + option.getInitialDelay();
        if (totalDuration > totalTime) {
          totalTime = totalDuration;
        }

        if (option.isEnabled()) {
          Callable<RecordingResult> screenRecordable = new Callable<RecordingResult>() {
            @Override
            public RecordingResult call() {
              option.setOpenGlCommand(recorderSettings.isCustomLauncherEnabled() && recorderSettings.getCustomLauncher() != null && recorderSettings.getCustomLauncher().toLowerCase().contains("gl"));
              LOG.info("Starting [glmode={}] recording for \"{}\", {}: {}", option.isOpenGlCommand(), game.getGameDisplayName(), screen.name(), recordingTempFile.getAbsolutePath());

              recorderSettings.getRecordingScreenOption(screen);
              ScreenRecorder screenRecorder = new ScreenRecorder(recordingScreen, recordingTempFile);
              screenRecorders.add(screenRecorder);

              RecordingResult result = screenRecorder.record(option);
              result.setGame(game);
              result.setScreen(screen);
              result.setRecordingScreenOptions(option);
              result.setRecordingTempFile(recordingTempFile);


              int count = 0;
              if (jobDescriptor.getUserData() != null) {
                count = (int) jobDescriptor.getUserData();
              }
              jobDescriptor.setUserData((count + 1));

              return result;
            }
          };
          callables.add(screenRecordable);
        }
      }
      catch (Exception e) {
        LOG.info("Recording of {} failed: {}", screen.name(), e.getMessage(), e);
      }
    }

    if (!callables.isEmpty()) {
      ExecutorService executorService = Executors.newFixedThreadPool(callables.size());
      for (Callable<RecordingResult> callable : callables) {
        Future<RecordingResult> submit = executorService.submit(callable);
        futures.add(submit);
      }

      try {
        for (Future<RecordingResult> future : futures) {
          RecordingResult recordingResult = future.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
          if (recordingResult != null) {
            recordingResults.add(recordingResult);
          }
          LOG.info("Recording finished: {}", recordingResult.toString());
          LOG.info("Recording error log: {}", recordingResult.getErrorLog());
        }
      }
      catch (Exception e) {
        LOG.error("Error waiting for recording errorResult: {}", e.getMessage(), e);
      }
    }
    else {
      LOG.info("Skipped recording of {}, no screens to record.", game.getGameDisplayName());
    }
    return status;
  }

  @Nullable
  protected RecordingScreenOptions validateScreen(VPinScreen screen) {
    RecordingScreenOptions option = recorderSettings.getRecordingScreenOption(screen);
    if (!option.isEnabled()) {
      LOG.info("Skipped recording for {}, screen is not enabled.", screen);
      return null;
    }

    if (!isRecordingRequired(game, screen, option.getRecordMode())) {
      LOG.info("Skipped recording for {}, asset not missing.", screen);
      return null;
    }
    return option;
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

  private boolean isRecordingRequired(Game game, VPinScreen screen, RecordingWriteMode recordingWriteMode) {
    if (recordingWriteMode.equals(RecordingWriteMode.ifMissing)) {
      //passing null here is ok, we are not interested in foreign assets here
      List<File> screenMediaFiles = frontend.getMediaAccessStrategy().getScreenMediaFiles(game, screen, null);
      return screenMediaFiles.isEmpty();
    }
    return true;
  }

  /**
   * Let's not create temporary files inside the screen assets folder.
   */
  @NonNull
  private File createTemporaryRecordingFile(Game game, VPinScreen screen, RecordingWriteMode recordingWriteMode) throws IOException {
    String name = game.getGameDisplayName() + "-" + screen.name();
    name = de.mephisto.vpin.restclient.util.FileUtils.replaceWindowsChars(name);
    File tempFile = File.createTempFile(name, ".mp4");
    tempFile.deleteOnExit();
    return tempFile;
  }

  public void finalizeRecordings() {
    for (RecordingResult recordingResult : recordingResults) {
      finalizeGameRecorder(recordingResult);
    }
  }

  private void finalizeGameRecorder(@NonNull RecordingResult result) {
    VPinScreen screen = result.getScreen();
    File recordingTempFile = result.getRecordingTempFile();
    RecordingWriteMode recordingWriteMode = result.getRecordingScreenOptions().getRecordMode();

    LOG.info("Finalizing temporary recording file {} for screen {}", recordingTempFile.getAbsolutePath(), screen.name());

    try {
      // when several folder possible for a VpinScreen like in pinballX, get the ones for mp4
      File mediaFolder = frontend.getMediaAccessStrategy().getGameMediaFolder(game, screen, "mp4", true);
      File target = frontend.getMediaAccessStrategy().createMedia(game, screen, "mp4", false);

      switch (recordingWriteMode) {
        case overwrite: {
          //passing null here is ok, we are not interested in foreign assets here
          List<File> screenMediaFiles = frontend.getMediaAccessStrategy().getScreenMediaFiles(game, screen, null);
          // delete existing files in the generated folder only whatever their format (and there maybe several, not only mp4)
          // why only in the generated folder is because pinballX has separated folders for images that need to stay
          if (!screenMediaFiles.isEmpty()) {
            for (File screenMediaFile : screenMediaFiles) {
              if (screenMediaFile.getParentFile().equals(mediaFolder) &&
                  Strings.CI.equals(FilenameUtils.getBaseName(screenMediaFile.getName()), game.getGameName())) {
                if (!screenMediaFile.delete()) {
                  LOG.error("Failed to delete {}, can't overwrite file with media recording for {}, file will be appended instead", screenMediaFile.getAbsolutePath(), screen.name());
                }
                else {
                  target = frontend.getMediaAccessStrategy().createMedia(game, screen, "mp4", true);
                }
              }
            }
          }
          copyRecordingToTarget(game, screen, recordingTempFile, target);
          break;
        }
        case ifMissing: {
          copyRecordingToTarget(game, screen, recordingTempFile, target);
          LOG.info("Added recorded file {} to screen {}, copied {}, used ifMissing mode.", target.getAbsolutePath(), recordingTempFile.getAbsolutePath(), screen.name());
          break;
        }
        case append: {
          // simply switch recorded and target files and keep all other files and format
          if (!Strings.CI.equals(target.getName(), recordingTempFile.getName())) {
            // another temporary not existing file that will be deleted
            target = frontend.getMediaAccessStrategy().createMedia(game, screen, "mp4", true);
            copyRecordingToTarget(game, screen, recordingTempFile, target);
            LOG.info("Appended recorded file {} of screen {} with {}, used overwrite mode.", recordingTempFile.getAbsolutePath(), target.getAbsolutePath(), screen.name());
          }
          break;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Error finalizing recording with mode {}: {}", recordingWriteMode.name(), e.getMessage(), e);
    }

    if (recordingTempFile.delete()) {
      LOG.info("Deleted temporary recording file {}", recordingTempFile.getAbsolutePath());
    }
    else {
      LOG.warn("Failed to delete temporary recording file {}", recordingTempFile.getAbsolutePath());
    }
  }

  private void copyRecordingToTarget(Game game, VPinScreen screen, File recordingTempFile, File target) throws IOException {
    try {
      if (target.exists() && !target.delete()) {
        target = frontend.getMediaAccessStrategy().createMedia(game, screen, "mp4", true);
        FileUtils.copyFile(recordingTempFile, target);
        LOG.info("Appending instead of overwriting existing media file \"{}\" of screen {} with \"{}\" (original file was locked).", target.getAbsolutePath(), recordingTempFile.getAbsolutePath(), screen.name());
      }
      else {
        FileUtils.copyFile(recordingTempFile, target);
        LOG.info("Copied media file {} of screen {} with {}.", target.getAbsolutePath(), recordingTempFile.getAbsolutePath(), screen.name());
      }
    }
    catch (IOException e) {
      LOG.error("Copying temporary video file to {} failed, trying to append asset. ({})", target.getAbsolutePath(), e.getMessage());
      target = frontend.getMediaAccessStrategy().createMedia(game, screen, "mp4", true);
      FileUtils.copyFile(recordingTempFile, target);
    }
  }
}
