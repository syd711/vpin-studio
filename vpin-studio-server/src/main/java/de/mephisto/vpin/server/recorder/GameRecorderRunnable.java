package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameMediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public class GameRecorderRunnable implements Callable<GameRecordingStatus> {
  private final static Logger LOG = LoggerFactory.getLogger(GameRecorderRunnable.class);

  private final MediaAccessStrategy mediaAccessStrategy;
  private final Game game;
  private final RecorderSettings recorderSettings;
  private final List<RecordingScreen> supportedRecodingScreens;
  private final JobDescriptor jobDescriptor;

  public GameRecorderRunnable(MediaAccessStrategy mediaAccessStrategy, Game game, RecorderSettings recorderSettings, List<RecordingScreen> supportedRecodingScreens, JobDescriptor jobDescriptor) {
    this.mediaAccessStrategy = mediaAccessStrategy;
    this.game = game;
    this.recorderSettings = recorderSettings;
    this.supportedRecodingScreens = supportedRecodingScreens;
    this.jobDescriptor = jobDescriptor;
  }


  @Override
  public GameRecordingStatus call() {
    LOG.info("Launching recording of \"" + game.getGameDisplayName() + "\"");

    GameRecordingStatus status = new GameRecordingStatus();

    for (RecordingScreen screen : supportedRecodingScreens) {
      RecordingScreenOptions option = recorderSettings.getRecordingScreenOption(screen);
      if (option.isEnabled()) {
        File mediaFolder = mediaAccessStrategy.getGameMediaFolder(game, screen.getScreen(), null);
        File target = GameMediaService.buildMediaAsset(mediaFolder, game, "mp4", false);
        ScreenRecorder screenRecorder = new ScreenRecorder(screen, target);
      }
    }

    return status;
  }
}
