package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
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
  private final File target;

  private SystemCommandExecutor executor;
  private boolean cancelled = false;

  public ScreenRecorder(@NonNull FrontendPlayerDisplay recordingScreen, @NonNull File target) {
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

      String videoSize = width + "x" + height;
      String offsetX = String.valueOf(recordingScreen.getX());
      String offsetY = String.valueOf(recordingScreen.getY());
      String duration = String.valueOf(options.getRecordingDuration());

      File resources = new File(SystemInfo.RESOURCES);
      if (!resources.exists()) {
        resources = new File("../" + SystemInfo.RESOURCES);
      }

      //original: ffmpeg -y -report -t 60 %wAudio% -filter_complex ddagrab=framerate=%curFPS%,hwdownload,format=bgra -c:v libx264 -r %curFPS% -preset ultrafast -crf 0 output.mkv
      List<String> commandList = new ArrayList<>();
      commandList.add("ffmpeg.exe");
      if (recordingScreen.getScreen().equals(VPinScreen.PlayField) && options.isRotated()) {
        commandList.add("-display_rotation");
        commandList.add("180");
      }
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
      if (options.isFps60()) {
        commandList.add("60");
      }
      else {
        commandList.add("30");
      }
      commandList.add("-t");
      commandList.add(duration);

      //-f dshow -i audio="Stereo Mix (Realtek(R) Audio)" -acodec libmp3lame
//      if (options.isRecordAudio()) {
//        commandList.add("-i");
//        commandList.add("audio=\"Stereo Mix (Realtek(R) Audio)\"");
//        commandList.add("-acodec");
//        commandList.add("libmp3lame");
//      }
      commandList.add("-draw_mouse");
      commandList.add("0");
      commandList.add("-i");
      commandList.add("desktop");
      commandList.add("-c:v");
      commandList.add("libx264");
      commandList.add("-r");
      commandList.add("30");
      commandList.add("-preset");
//      commandList.add("ultrafast");
      commandList.add("ultrafast");
      commandList.add("-tune");
      commandList.add("zerolatency");
      commandList.add("-crf");
      commandList.add("25");
      commandList.add("-pix_fmt");
      commandList.add("yuv420p");
      if (VPinScreen.PlayField.equals(recordingScreen.getScreen()) && recordingScreen.isInverted()) {
        commandList.add("-vf");
        commandList.add("\"transpose=2,transpose=2\"");
      }
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
    this.cancelled = true;
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
