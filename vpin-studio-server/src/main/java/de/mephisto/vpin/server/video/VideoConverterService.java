package de.mephisto.vpin.server.video;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.video.VideoOperation;
import de.mephisto.vpin.restclient.video.VideoConversionCommand;
import de.mephisto.vpin.restclient.video.VideoConversionCommand.ImageOp;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class VideoConverterService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VideoConverterService.class);

  private final List<VideoConversionCommand> commands = new ArrayList<>();

  @Autowired
  private FrontendService frontendService;

  public String convert(@NonNull VideoOperation converterParams) {
    LOG.info("Executing video conversion for " + converterParams.getName() + "/" + converterParams.getCommand());
    try {
      Game game = frontendService.getOriginalGame(converterParams.getGameId());
      if (game == null) {
        LOG.info("No game found for " + converterParams.getGameId());
        return "No game found for " + converterParams.getGameId();
      }

      FrontendMediaItem mediaItem = frontendService.getMediaItem(game, converterParams.getScreen(), converterParams.getName());
      if (mediaItem == null) {
        LOG.info("No media item found for " + converterParams.getName());
        return "No media item found for " + converterParams.getName();
      }

      File file = mediaItem.getFile();
      if (file.exists()) {
        return convert(converterParams.getCommand(), file);
      }
      else {
        LOG.warn("Video file \"" + file.getAbsolutePath() + "\" not found.");
      }
    }
    catch (Exception e) {
      LOG.error("Error converting video: " + e.getMessage(), e);
      return "Error converting video: " + e.getMessage();
    }
    return null;
  }

  public String convert(VideoConversionCommand command, File mediaFile) throws Exception {
    if (command.getType() == VideoConversionCommand.TYPE_FILE) {
      File batFile = new File(command.getCommand());
      if (batFile.exists()) {
        return convertWithScript(batFile, mediaFile);
      }
      else {
        LOG.warn("No matching conversion .bat file found for {}", command.getName());
      }
    }
    else if (command.getType() == VideoConversionCommand.TYPE_FFMEPG) {
      File targetFile = FileUtils.uniqueFile(mediaFile);
      convertWithFfmpeg(command, mediaFile, targetFile);
      // now exchange files
      if (mediaFile.delete()) {
        targetFile.renameTo(mediaFile);
      }
    }
    else if (command.getType() == VideoConversionCommand.TYPE_IMAGE) {
      return convertWithImageUtils(command.getCommand(), mediaFile);
    }
    else {
      LOG.warn("Not supported implementation of command {} for {}", command.getClass().getName(), command.getName());
    }
    return null;
  }

  private String convertWithScript(File batFile, File file) throws Exception {
    // List<String> params = Arrays.asList("cmd", "/c", "start", "\"" + batFile.getAbsolutePath() + "\"", "\"" + file.getAbsolutePath() + "\"");
    List<String> params = Arrays.asList("\"" + batFile.getAbsolutePath() + "\"", "\"" + file.getAbsolutePath() + "\"");
    LOG.info("Executing: " + String.join(" ", params));
    SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
    executor.setDir(batFile.getParentFile());
    executor.executeCommand();

    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
    if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
      LOG.info("Conversion: {}", standardErrorFromCommand);
      return null; //"Conversion failed: " + standardErrorFromCommand;
    }
    LOG.info("Video conversion output:");
    LOG.info(standardOutputFromCommand.toString());
    return null;
  }

  public void convertWithFfmpeg(VideoConversionCommand command, File mediaFile, File targetFile) throws Exception {
        // "%_curloc%\ffmpeg" -y -i %1 -vf "transpose=1" "%2"

    String[] args = StringUtils.split(command.getCommand());

    File resources = new File(SystemInfo.RESOURCES);
    if (!resources.exists()) {
      resources = new File("../" + SystemInfo.RESOURCES);
    }

    List<String> commandList = new ArrayList<>();
    commandList.add("ffmpeg.exe");
    commandList.add("-y");
    commandList.add("-i");
    commandList.add(mediaFile.getAbsolutePath());
    for (String arg : args) {
      commandList.add(arg);  
    }

    commandList.add(targetFile.getAbsolutePath());

    LOG.info("Executing: " + String.join(" ", commandList));
    SystemCommandExecutor executor = new SystemCommandExecutor(commandList, false);

    executor = new SystemCommandExecutor(commandList);
//      executor.enableLogging(true);
    executor.setDir(resources);
    executor.executeCommand();

    //StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
    //LOG.info("Conversion failed: {}", standardErrorFromCommand);
    //StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    //LOG.info("Video conversion output: {}", standardOutputFromCommand);
  }

  private String convertWithImageUtils(String command, File mediaFile) {
    try {
      BufferedImage img = ImageUtil.loadImage(mediaFile);
      switch (ImageOp.valueOf(command)) {
      case ROTATE_90:
        img = ImageUtil.rotateRight(img);
        break;
      case ROTATE_90_CCW :
        img = ImageUtil.rotateLeft(img);
        break;
      case ROTATE_180:
        img = ImageUtil.rotate180(img);
        break;
      }
      
      ImageUtil.write(img, mediaFile);
      return null;
    }
    catch (Exception e) {
      return e.getMessage();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    commands.clear();
    boolean isPopper = frontendService.getFrontend().getFrontendType().equals(FrontendType.Popper);
    if (isPopper) {
      File recordingsFolder = new File(frontendService.getFrontendInstallationFolder(), "Recordings");
      if (recordingsFolder.exists()) {
        File[] batFiles = recordingsFolder.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.startsWith("POPMedia") && name.endsWith(".bat");
          }
        });

        if (batFiles != null) {
          for (File batFile : batFiles) {
            String name = batFile.getName();
            String command = FilenameUtils.getBaseName(name);
            command = command.replaceAll("POPMedia_", "");
            command = command.replaceAll("_", " ");
            command = command.replaceAll("CCW", "Counter Clockwise");
            command = command.replaceAll("CW", "Clockwise");
            command = command.replaceAll("90", "90°");
            command = command.replaceAll("180", "180°");

            VideoConversionCommand cmd = new VideoConversionCommand();
            cmd.setFile(batFile.getAbsolutePath());
            cmd.setName(command);
            commands.add(cmd);
          }
        }
      }
    }
    else {
      commands.add(new VideoConversionCommand("Rotate Clockwise 90°").setFFmpegArgs("-vf \"transpose=1\""));
      commands.add(new VideoConversionCommand("Rotate Counter Clockwise 90°").setFFmpegArgs("-vf \"transpose=2\""));
      commands.add(new VideoConversionCommand("Rotate 180°").setFFmpegArgs("-vf \"transpose=2,transpose=2\""));
      commands.add(new VideoConversionCommand("Mute Volume").setFFmpegArgs("-c:v copy -an"));    
    }

    // add image commandes
    commands.add(new VideoConversionCommand("Rotate Clockwise 90°").setImageArgs(ImageOp.ROTATE_90));
    commands.add(new VideoConversionCommand("Rotate Counter Clockwise 90°").setImageArgs(ImageOp.ROTATE_90_CCW));
    commands.add(new VideoConversionCommand("Rotate 180°").setImageArgs(ImageOp.ROTATE_180));
  }

  public List<VideoConversionCommand> getCommandList() {
    return commands;
  }
}
