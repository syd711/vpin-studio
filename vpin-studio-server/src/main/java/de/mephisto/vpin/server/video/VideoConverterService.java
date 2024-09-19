package de.mephisto.vpin.server.video;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.video.VideoConversion;
import de.mephisto.vpin.restclient.video.VideoConversionCommand;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class VideoConverterService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VideoConverterService.class);

  private final List<VideoConversionCommand> commands = new ArrayList<>();

  @Autowired
  private FrontendService frontendService;


  public String convert(@NonNull VideoConversion converterParams) {
    LOG.info("Executing video conversion for " + converterParams.getName() + "/" + converterParams.getCommand());
    try {
      Game game = frontendService.getGame(converterParams.getGameId());
      if (game == null) {
        LOG.info("No game found for " + converterParams.getGameId());
        return "No game found for " + converterParams.getGameId();
      }

      FrontendMediaItem mediaItem = game.getGameMedia().getMediaItem(converterParams.getScreen(), converterParams.getName());
      if (mediaItem == null) {
        LOG.info("No media item found for " + converterParams.getName());
        return "No media item found for " + converterParams.getName();
      }

      File file = mediaItem.getFile();
      if (file.exists()) {
        File batFile = new File(converterParams.getCommand().getFile());
        if (batFile.exists()) {
//          List<String> params = Arrays.asList("cmd", "/c", "start", "\"" + batFile.getAbsolutePath() + "\"", "\"" + file.getAbsolutePath() + "\"");
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
        }
        else {
          LOG.warn("No matching conversion .bat file found for " + converterParams.getCommand());
        }
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

  @Override
  public void afterPropertiesSet() throws Exception {
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
  }

  public List<VideoConversionCommand> getCommandList() {
    return commands;
  }
}
