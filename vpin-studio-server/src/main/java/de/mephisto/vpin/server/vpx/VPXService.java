package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class VPXService {
  private final static Logger LOG = LoggerFactory.getLogger(VPXService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private SystemService systemService;

  public String getScript(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      File gameFile = game.getGameFile();
      File vpxExe = systemService.getVPXExe();
      File target = new File(gameFile.getParentFile(), FilenameUtils.getBaseName(gameFile.getName()) + ".vbs");

      try {
        LOG.info("Extracting VBS for " + gameFile.getAbsolutePath());
        SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList(vpxExe.getAbsolutePath(), "-ExtractVBS", gameFile.getAbsolutePath()));
        executor.executeCommand();

        StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
        if (standardErrorFromCommand != null && !StringUtils.isEmpty(standardErrorFromCommand.toString())) {
          LOG.error("VPX command failed:\n" + standardErrorFromCommand);
        }
      } catch (Exception e) {
        LOG.error("Error executing shutdown: " + e.getMessage(), e);
      }

      int count = 0;
      while (!target.exists()) {
        count++;
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          //ignore
        }
        if (count > 40) {
          LOG.error("Timeout waiting for the generation of " + target.getAbsolutePath());
        }
      }

      if (target.exists()) {
        try {
          LOG.info("Reading vbs file " + target.getAbsolutePath() + " (" + FileUtils.readableFileSize(target.length()) + ")");
          Path filePath = Path.of(target.toURI());
          return Files.readString(filePath);
        } catch (IOException e) {
          LOG.error("Failed to read " + target.getAbsolutePath() + ": " + e.getMessage(), e);
        } finally {
          if (!target.delete()) {
            LOG.error("Failed to clean up vbs file " + target.getAbsolutePath());
          }
        }
      }
    }
    LOG.error("No game found for id " + gameId);
    return null;
  }
}
