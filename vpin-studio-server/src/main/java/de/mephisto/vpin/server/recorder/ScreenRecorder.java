package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.restclient.util.Ffmpeg;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ffmpeg -video_size 800x600 -offset_x 1000 -offset_y 20 -y -rtbufsize 100M -f gdigrab -framerate 30 -t 5 -draw_mouse 1 -i desktop -c:v libx264 -r 30 -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p video_comapre2.mp4
 * ffmpeg -video_size 254x927 -offset_x 716  -offset_y 83 -y -rtbufsize 100M -f gdigrab -framerate 30 -t 3 -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p
 * <p>
 * Popper nvidia=0
 * ffmpeg -y -rtbufsize 100M -report %wAudio% -f gdigrab -t 60 -framerate %curFPS% -probesize 10M -offset_x %1 -offset_y %2 -video_size %3x%4 -i desktop -c:v h264_nvenc -preset:v fast -pix_fmt nv12 -r %curFPS% -b:v %cbitrate%M output.mkv
 * <p>
 * /test
 * ffmpeg.exe -y -video_size 3374x1852 -offset_x 213 -offset_y 161 -rtbufsize 100M -f gdigrab -framerate 30 -t 10 -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset fast -tune zerolatency -crf 25 -pix_fmt yuv420p out85.mp4
 * <p>
 * ffmpeg.exe -y -f dshow -i audio="SPDIF-Schnittstelle (Sound Blaster X4)" -acodec aac -y -video_size 3374x1852 -offset_x 213 -offset_y 161 -rtbufsize 100M -f gdigrab -framerate 30 -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset fast -tune zerolatency -crf 25
 * -pix_fmt yuv420p -t 5 out.mp4
 */
public class ScreenRecorder {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenRecorder.class);

  @NonNull
  private final FrontendPlayerDisplay recordingScreen;

  @NonNull
  private final File temporaryTarget;

  private SystemCommandExecutor executor;
  private boolean cancelled = false;

  public ScreenRecorder(@NonNull FrontendPlayerDisplay recordingScreen, @NonNull File temporaryTarget) {
    this.recordingScreen = recordingScreen;
    this.temporaryTarget = temporaryTarget;
  }

  public RecordingResult record(@NonNull RecordingScreenOptions options) {
    long start = System.currentTimeMillis();
    RecordingResult result = new RecordingResult();
    result.setFileName(temporaryTarget.getAbsolutePath());

    try {
      if (options.getInitialDelay() > 0) {
        LOG.info(this + " is waiting for the initial recording delay of " + options.getInitialDelay() + " seconds.");
        for (int i = 0; i < options.getInitialDelay(); i++) {
          Thread.sleep(1000);
          if (cancelled) {
            return result;
          }
        }
      }

      int width = recordingScreen.getWidth();
      if (width % 2 == 1) {
        width--;
      }

      int height = recordingScreen.getHeight();
      if (height % 2 == 1) {
        height--;
      }
      int x = recordingScreen.getX();
      int y = recordingScreen.getY();
      long duration = options.getRecordingDuration();

      String command = getDefaultCommand(options);
      if (options.isExpertSettingsEnabled() && options.getCustomFfmpegCommand() != null) {
        command = options.getCustomFfmpegCommand();
      }
      String formattedCommand = formatCommand(command, width, height, x, y, duration);
      executeCommand(formattedCommand, result, start);
    }
    catch (Exception e) {
      LOG.error("Screen recording failed: {}", e.getMessage(), e);
      result.setErrorLog(e.getMessage());
    }
    return result;
  }

  private String getDefaultCommand(RecordingScreenOptions options) {
    String command = Ffmpeg.DEFAULT_COMMAND;
    if (VPinScreen.PlayField.equals(recordingScreen.getScreen()) && recordingScreen.isInverted()) {
      command = command + " -vf \"transpose=2,transpose=2\"";
    }

    if (options.isFps60()) {
      command = command.replace("-framerate 30", "-framerate 60");
      command = command.replace("-r 30", "-r 60");
    }

    if (VPinScreen.PlayField.equals(recordingScreen.getScreen()) && options.isRotated()) {
      command = command.replace("ffmpeg.exe", "ffmpeg.exe -display_rotation 180");
    }

    return command;
  }

  private String formatCommand(String cmd, int width, int height, int x, int y, long duration) {
    String command = cmd;
    command = command.replace("[x]", String.valueOf(x));
    command = command.replace("[y]", String.valueOf(y));
    command = command.replace("[duration]", String.valueOf(duration));
    command = command.replace("[width]", String.valueOf(width));
    command = command.replace("[height]", String.valueOf(height));
    return command;
  }

  private void executeCommand(@NonNull String command, @NonNull RecordingResult result, long start) throws Exception {
    List<String> commandList = new ArrayList<>(Arrays.asList(command.split(" ")));
    commandList.add("\"" + temporaryTarget.getAbsolutePath() + "\"");

    File resources = new File(SystemService.RESOURCES);
    if (!resources.exists()) {
      resources = new File("../" + SystemService.RESOURCES);
    }

    result.setCommand(String.join(" ", commandList));
    executor = new SystemCommandExecutor(commandList);
//    executor.enableLogging(true);
    executor.setDir(resources);
    executor.executeCommand();

    String err = executor.getStandardErrorFromCommand().toString();
    String info = executor.getStandardOutputFromCommand().toString();
    result.setErrorLog(err);
    result.setInfoLog(info);
    result.setDuration(System.currentTimeMillis() - start);
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
    this.cancelled = true;
    stop();
    if (temporaryTarget.exists()) {
      temporaryTarget.delete();
    }
    LOG.info("Finished cancellation of " + this);
  }

  @Override
  public String toString() {
    return "Screen Recorder for '" + this.recordingScreen.getScreen().name() + "'";
  }
}
