package de.mephisto.vpin.server.converter;

import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts the audio track of a video file as mp3 using ffmpeg.
 */
public class AudioExtractor {
  private final static Logger LOG = LoggerFactory.getLogger(AudioExtractor.class);

  private AudioExtractor() {
  }

  public static void extractMp3(MultipartFile upload, String suffix, File targetMp3) throws Exception {
    File temp = File.createTempFile("upload", "." + suffix);
    try {
      UploadUtil.upload(upload, temp);
      extractMp3(temp, targetMp3);
    }
    finally {
      temp.delete();
    }
  }

  public static void extractMp3(File video, File targetMp3) throws Exception {
    File resources = new File(SystemService.RESOURCES);
    if (!resources.exists()) {
      resources = new File("../" + SystemService.RESOURCES);
    }

    List<String> commandList = new ArrayList<>();
    commandList.add(OSUtil.isWindows() ? "ffmpeg.exe" : "ffmpeg");
    commandList.add("-y");
    commandList.add("-i");
    commandList.add(video.getAbsolutePath());
    commandList.add("-vn");
    commandList.add("-c:a");
    commandList.add("libmp3lame");
    commandList.add("-q:a");
    commandList.add("2");
    commandList.add(targetMp3.getAbsolutePath());

    LOG.info("Executing: {}", String.join(" ", commandList));
    SystemCommandExecutor executor = new SystemCommandExecutor(commandList);
    executor.enableLogging(true);
    executor.setDir(resources);
    executor.executeCommand();

    if (!targetMp3.exists() || targetMp3.length() == 0) {
      String output = String.valueOf(executor.getStandardErrorFromCommand());
      LOG.error("Audio extraction failed for {}: {}", video.getAbsolutePath(), output);
      targetMp3.delete();
      if (output.contains("does not contain any stream")) {
        throw new Exception("The video \"" + video.getName() + "\" does not contain an audio track.");
      }
      throw new Exception("No audio track could be extracted from \"" + video.getName() + "\".");
    }
    LOG.info("Extracted audio from \"{}\" to \"{}\"", video.getName(), targetMp3.getAbsolutePath());
  }
}
