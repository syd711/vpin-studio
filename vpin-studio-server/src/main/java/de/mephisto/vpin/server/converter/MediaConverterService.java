package de.mephisto.vpin.server.converter;

import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.restclient.converter.MediaConversionCommand.ImageOp;
import de.mephisto.vpin.restclient.converter.MediaOperation;
import de.mephisto.vpin.restclient.converter.MediaOperationResult;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.MediaService;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.playlists.PlaylistMediaService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.commons.fx.ImageUtil;
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
public class MediaConverterService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MediaConverterService.class);

  private final List<MediaConversionCommand> commands = new ArrayList<>();

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private PlaylistMediaService playlistMediaService;

  public MediaOperationResult convert(@NonNull MediaOperation operation) {
    MediaOperationResult result = new MediaOperationResult();
    result.setMediaOperation(operation);
    LOG.info("Executing video conversion for {} / {} ", operation.getFilename(), operation.getCommand());
    try {
      List<File> mediaItemFiles = getMediaItemFiles(result, operation);
      List<File> filteredFiles = filterMediaFiles(mediaItemFiles, operation.getCommand().getType());

      for (File mediaItemFile : filteredFiles) {
        convert(operation.getCommand(), result, mediaItemFile);
      }
    }
    catch (Exception e) {
      LOG.error("Error converting media: {}", e.getMessage(), e);
      result.setResult("Error converting media: " + e.getMessage());
    }
    return result;
  }

  private List<File> filterMediaFiles(List<File> mediaItemFiles, int type) {
    List<File> filtered = new ArrayList<>();
    for (File mediaItemFile : mediaItemFiles) {
      String suffix = FilenameUtils.getExtension(mediaItemFile.getName()).toLowerCase();
      boolean isVideo = suffix.equals("mp4");
      boolean isImage = suffix.equals("png") || suffix.equals("jpeg") || suffix.equals("jpg");

      if (type == MediaConversionCommand.TYPE_FILE && isVideo) {
        filtered.add(mediaItemFile);
      }
      else if (type == MediaConversionCommand.TYPE_IMAGE && isImage) {
        filtered.add(mediaItemFile);
      }
      else if (type == MediaConversionCommand.TYPE_FFMEPG && isVideo) {
        filtered.add(mediaItemFile);
      }
    }

    return filtered;
  }

  public void convert(@NonNull MediaConversionCommand command, @NonNull MediaOperationResult operationResult, @NonNull File mediaFile) throws Exception {
    if (command.getType() == MediaConversionCommand.TYPE_FILE) {
      File batFile = new File(command.getCommand());
      if (batFile.exists()) {
        operationResult.setResult(convertWithScript(batFile, mediaFile));
      }
      else {
        LOG.warn("No matching conversion .bat file found for {}", command.getName());
        operationResult.setResult("No matching conversion .bat file found for " + command.getName());
      }
    }
    else if (command.getType() == MediaConversionCommand.TYPE_FFMEPG) {
      operationResult.setResult(convertWithFfmpeg(operationResult, command, mediaFile));
    }
    else if (command.getType() == MediaConversionCommand.TYPE_IMAGE) {
      convertWithImageUtils(operationResult, ImageOp.valueOf(command.getCommand()), mediaFile);
    }
    else {
      LOG.warn("Not supported implementation of command {} for {}", command.getClass().getName(), command.getName());
      operationResult.setResult("Not supported implementation of command " + command.getClass().getName() + " for " + command.getName());
    }
  }

  private List<File> getMediaItemFiles(@NonNull MediaOperationResult operationResult, @NonNull MediaOperation operation) {
    MediaService mediaService = operation.isPlaylistMode() ? playlistMediaService : gameMediaService;
    List<File> result = new ArrayList<>();
    if (operation.getFilename() != null) {
      File mediaFile = mediaService.getMediaFile(operation.getObjectId(), operation.getScreen(), operation.getFilename());
      if (mediaFile == null || !mediaFile.exists()) {
        LOG.info("No media item found for " + operation.getFilename());
        operationResult.setResult("No media item found for " + operation.getFilename());
        return result;
      }
      result.add(mediaFile);
    }
    else {
      List<File> mediaFiles = mediaService.getMediaFiles(operation.getObjectId(), operation.getScreen());
      for (File mediaFile : mediaFiles) {
        if (mediaFile.exists()) {
          result.add(mediaFile);
        }
      }
    }
    return result;
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
      return standardErrorFromCommand.toString();
    }
    LOG.info("Video conversion output:");
    LOG.info(standardOutputFromCommand.toString());
    return standardOutputFromCommand.toString();
  }

  public String convertWithFfmpeg(MediaOperationResult operationResult, MediaConversionCommand command, File mediaFile) throws Exception {
    File targetFile = FileUtils.uniqueFile(mediaFile);
    String output = convertWithFfmpeg(command, mediaFile, targetFile);
    // now exchange files
    if (mediaFile.delete() && !targetFile.renameTo(mediaFile)) {
      operationResult.setResult("Target file renaming failed: " + mediaFile.getAbsolutePath());
    }
    return output;
  }

  public String convertWithFfmpeg(MediaConversionCommand command, File mediaFile, File targetFile) throws Exception {
    String[] args = StringUtils.split(command.getCommand());

    File resources = new File(SystemService.RESOURCES);
    if (!resources.exists()) {
      resources = new File("../" + SystemService.RESOURCES);
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
    executor.enableLogging(true);
    executor.setDir(resources);
    executor.executeCommand();

    StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
    LOG.info("Conversion failed: {}", standardErrorFromCommand);
    StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
    LOG.info("Video conversion output: {}", standardOutputFromCommand);

    if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
      return standardOutputFromCommand.toString();
    }
    return standardOutputFromCommand.toString();
  }

  public void convertWithFfmpeg(File mediaFile, File targetFile) throws Exception {
    // "%_curloc%\ffmpeg" -y -i %1 "%2"
    //Simple convert (can be used to convert apng (Animated PNG) to gif (Animated GIF))

    File resources = new File(SystemService.RESOURCES);
    if (!resources.exists()) {
      resources = new File("../" + SystemService.RESOURCES);
    }

    List<String> commandList = new ArrayList<>();
    commandList.add("ffmpeg.exe");
    commandList.add("-y");
    commandList.add("-i");
    commandList.add(mediaFile.getAbsolutePath());


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

  private void convertWithImageUtils(MediaOperationResult result, ImageOp command, File mediaFile) {
    try {
      BufferedImage img = ImageUtil.loadImage(mediaFile);
      switch (command) {
        case ROTATE_90:
          img = ImageUtil.rotateRight(img);
          break;
        case ROTATE_90_CCW:
          img = ImageUtil.rotateLeft(img);
          break;
        case ROTATE_180:
          img = ImageUtil.rotate180(img);
          break;
      }

      ImageUtil.write(img, mediaFile);
      result.setResult("Converted file " + mediaFile.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Image conversion failed for {}: {}", mediaFile.getAbsolutePath(), e.getMessage(), e);
      result.setResult("Image conversion failed for " + mediaFile.getAbsolutePath() + ": " + e.getMessage());
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

            MediaConversionCommand cmd = new MediaConversionCommand();
            cmd.setFile(batFile.getAbsolutePath());
            cmd.setName(command);
            commands.add(cmd);
          }
        }
      }
    }
    else {
      commands.add(new MediaConversionCommand("Rotate Clockwise 90°").setFFmpegArgs("-vf \"transpose=1\""));
      commands.add(new MediaConversionCommand("Rotate Counter Clockwise 90°").setFFmpegArgs("-vf \"transpose=2\""));
      commands.add(new MediaConversionCommand("Rotate 180°").setFFmpegArgs("-vf \"transpose=2,transpose=2\""));
      commands.add(new MediaConversionCommand("Mute Volume").setFFmpegArgs("-c:v copy -an"));
    }

    // add image commands
    commands.add(new MediaConversionCommand("Rotate Clockwise 90°").setImageArgs(ImageOp.ROTATE_90));
    commands.add(new MediaConversionCommand("Rotate Counter Clockwise 90°").setImageArgs(ImageOp.ROTATE_90_CCW));
    commands.add(new MediaConversionCommand("Rotate 180°").setImageArgs(ImageOp.ROTATE_180));
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  public List<MediaConversionCommand> getCommandList() {
    return commands;
  }

  //-----------------------------------------------
  // some useful conversion

  public void rotateImage180(File mediaFile) {
    MediaOperationResult result = new MediaOperationResult();
    convertWithImageUtils(result, ImageOp.ROTATE_180, mediaFile);
  }

  public void rotateVideo180(File mediaFile) throws Exception {
    MediaOperationResult result = new MediaOperationResult();
    MediaConversionCommand cmd = new MediaConversionCommand().setFFmpegArgs("-vf \"transpose=2,transpose=2\"");
    convertWithFfmpeg(result, cmd, mediaFile);
  }

}
