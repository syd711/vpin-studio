package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;

@Service
public class VPXCommandLineService {
  private final static Logger LOG = LoggerFactory.getLogger(VPXCommandLineService.class);

  @Autowired
  private SystemService systemService;

  public File execute(@NonNull Game game, @NonNull String commandParam, @NonNull String fileSuffix) {
    File gameFile = game.getGameFile();
    File vpxExe = systemService.getVPXExe();
    File target = new File(gameFile.getParentFile(), FilenameUtils.getBaseName(gameFile.getName()) + "." + fileSuffix);

    try {
      LOG.info("Executing VPX " + commandParam + "command for " + gameFile.getAbsolutePath());
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList(vpxExe.getAbsolutePath(), commandParam, gameFile.getAbsolutePath()));
      executor.executeCommandAsync();

      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (standardErrorFromCommand != null && !StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("VPX command failed:\n" + standardErrorFromCommand);
      }
    } catch (Exception e) {
      LOG.error("Error executing VPX command: " + e.getMessage(), e);
    }

    int count = 0;
    while (!target.exists()) {
      count++;
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        //ignore
      }
      if (count > 20) {
        LOG.error("Timeout waiting for the generation of " + target.getAbsolutePath());
        systemService.killProcesses("VPinballX");
        break;
      }
    }

    return target;
  }
}
