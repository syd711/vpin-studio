package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ffmpeg -video_size 800x600 -offset_x 1000 -offset_y 20 -y -rtbufsize 100M -f gdigrab -framerate 30 -t 5 -draw_mouse 1 -i desktop -c:v libx264 -r 30 -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p video_comapre2.mp4
 * ffmpeg -video_size 254x927 -offset_x 716  -offset_y 83 -y -rtbufsize 100M -f gdigrab -framerate 30 -t 3 -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p
 */
public class ScreenRecorder {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenRecorder.class);

  @NonNull
  private final RecordingScreen recordingScreen;
  @NonNull
  private final File target;

  private SystemCommandExecutor executor;

  public ScreenRecorder(@NonNull RecordingScreen recordingScreen, @NonNull File target) {
    this.recordingScreen = recordingScreen;
    this.target = target;
  }

  public RecordingResult record(@NonNull RecordingScreenOptions options) {
    long start = System.currentTimeMillis();
    RecordingResult result = new RecordingResult();
    result.setFileName(target.getAbsolutePath());

    try {
      if (options.getInitialDelay() > 0) {
        LOG.info(this + " is waiting for the initial recording delay of " + options.getInitialDelay() + " seconds.");
        Thread.sleep(options.getInitialDelay() * 1000);
      }

      int width = recordingScreen.getDisplay().getWidth();
      if (width % 2 == 1) {
        width--;
      }

      int height = recordingScreen.getDisplay().getHeight();
      if (height % 2 == 1) {
        height--;
      }

      String videoSize = width + "x" + height;
      String offsetX = String.valueOf(recordingScreen.getDisplay().getX());
      String offsetY = String.valueOf(recordingScreen.getDisplay().getY());
      String duration = String.valueOf(options.getRecordingDuration());

      File resources = new File(SystemInfo.RESOURCES);
      if (!resources.exists()) {
        resources = new File("../" + SystemInfo.RESOURCES);
      }

      List<String> commandList = new ArrayList<>();
      commandList.add("ffmpeg.exe");
      commandList.add("-y");
      commandList.add("-video_size");
      commandList.add(videoSize);
      commandList.add("-offset_x");
      commandList.add(offsetX);
      commandList.add("-offset_y");
      commandList.add(offsetY);
      commandList.add("-rtbufsize");
      commandList.add("100M");
      commandList.add("-f");
      commandList.add("gdigrab");
      commandList.add("-framerate");
      commandList.add("30");
      commandList.add("-t");
      commandList.add(duration);
      commandList.add("-draw_mouse");
      commandList.add("0");
      commandList.add("-i");
      commandList.add("desktop");
      commandList.add("-c:v");
      commandList.add("libx264");
      commandList.add("-r");
      commandList.add("30");
      commandList.add("-preset");
      commandList.add("ultrafast");
      commandList.add("-tune");
      commandList.add("zerolatency");
      commandList.add("-crf");
      commandList.add("25");
      commandList.add("-pix_fmt");
      commandList.add("yuv420p");
      commandList.add(target.getAbsolutePath());

      executor = new SystemCommandExecutor(commandList);
//      executor.enableLogging(true);
      executor.setDir(resources);
      executor.executeCommand();

      String err = executor.getStandardErrorFromCommand().toString();
//      if (!StringUtils.isEmpty(err)) {
//        throw new Exception(err);
//      }

      result.setDuration(System.currentTimeMillis() - start);
    }
    catch (Exception e) {
      LOG.error("Screen recording failed: {}", e.getMessage(), e);
    }
    return result;
  }

  private void stop() {
    try {
      if (executor != null) {
        executor.killProcess();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to kill ffmpeg recording process: {}", e.getMessage(), e);
    }
  }

  public void cancel() {
    stop();
    if (target.exists()) {
      target.delete();
    }
    LOG.info("Finished cancellation of " + this);
  }

  @Override
  public String toString() {
    return "Screen Recorder for '" + this.recordingScreen.getScreen().name() + "'";
  }
}
