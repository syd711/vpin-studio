package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ffmpeg -video_size 800x600 -offset_x 1000 -offset_y 20 -y -rtbufsize 100M -f gdigrab -framerate 30 -t 5 -draw_mouse 1 -i desktop -c:v libx264 -r 30 -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p video_comapre2.mp4
 * ffmpeg -video_size 254x927 -offset_x 716  -offset_y 83 -y -rtbufsize 100M -f gdigrab -framerate 30 -t 3 -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p
 */
public class ScreenRecorder {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenRecorder.class);

//  private final static String CMD = "ffmpeg.exe -video_size %s -offset_x %s -offset_y %s -y -rtbufsize 100M -f gdigrab -framerate 30 -t %s -draw_mouse 0 -i desktop -c:v libx264 -r 30 -preset ultrafast -tune zerolatency -crf 25 -pix_fmt yuv420p %s";

  @NonNull
  private final RecordingScreen recordingScreen;
  private SystemCommandExecutor executor;

  public ScreenRecorder(@NonNull RecordingScreen recordingScreen) {
    this.recordingScreen = recordingScreen;
  }

  public boolean start(@NonNull RecordingScreenOptions options, @NonNull File target) {
    try {
      String videoSize = recordingScreen.getDisplay().getWidth() + "x" + recordingScreen.getDisplay().getHeight();
      int offsetX = recordingScreen.getDisplay().getX();
      int offsetY = recordingScreen.getDisplay().getY();
      int duration = options.getRecordingDuration();

      File resources = new File(SystemInfo.RESOURCES);
      if (!resources.exists()) {
        resources = new File("../" + SystemInfo.RESOURCES);
      }

      List<String> commandList = new ArrayList<>();
      commandList.add("ffmpeg.exe");
      commandList.add("-video_size " + videoSize);
      commandList.add("-offset_x " + offsetX);
      commandList.add("-offset_y " + offsetY);
      commandList.add("-rtbufsize 100M");
      commandList.add("-f gdigrab");
      commandList.add("-framerate 30");
      commandList.add("-t " + duration);
      commandList.add("-draw_mouse 0");
      commandList.add("-i desktop");
      commandList.add("-c:v libx264");
      commandList.add("-r 30");
      commandList.add("-preset ultrafast");
      commandList.add("-tune zerolatency");
      commandList.add("-crf 25");
      commandList.add("-pix_fmt yuv420p");
      commandList.add(target.getAbsolutePath());

      executor = new SystemCommandExecutor(commandList);
//      executor.enableLogging(true);
      executor.setDir(resources);
      executor.executeCommand();

      String err = executor.getStandardErrorFromCommand().toString();
      if (!StringUtils.isEmpty(err)) {
        throw new Exception(err);
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Screen recording failed: {}", e.getMessage(), e);
      return false;
    }
  }

  public void stop() {
    if (executor != null) {
      executor.killProcess();
    }
  }

}
