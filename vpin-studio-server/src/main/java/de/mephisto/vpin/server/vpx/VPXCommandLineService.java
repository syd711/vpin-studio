package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Service
public class VPXCommandLineService {
  private final static Logger LOG = LoggerFactory.getLogger(VPXCommandLineService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private PinUPConnector pinUPConnector;

  public boolean execute(@NonNull Game game, @NonNull String commandParam, @Nullable String altExe) {
    File gameFile = game.getGameFile();
    File vpxExe = game.getEmulator().getVPXExe();

    TableDetails tableDetails = pinUPConnector.getTableDetails(game.getId());
    String altLaunchExe = tableDetails.getAltLaunchExe();
    if(altExe != null) {
      vpxExe = new File(game.getEmulator().getInstallationFolder(), altExe);
    }
    else if (!StringUtils.isEmpty(altLaunchExe)) {
      vpxExe = new File(game.getEmulator().getInstallationFolder(), altLaunchExe);
    }

    try {
      List<String> strings = Arrays.asList(vpxExe.getName(), commandParam, "\"" + gameFile.getAbsolutePath() + "\"");
      LOG.info("Executing VPX " + commandParam + "command: " + String.join(" ", strings));
      SystemCommandExecutor executor = new SystemCommandExecutor(strings);
      executor.setDir(vpxExe.getParentFile());
      executor.executeCommandAsync();

      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (standardErrorFromCommand != null && !StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("VPX command failed:\n" + standardErrorFromCommand);
        return false;
      }
      return true;
    } catch (Exception e) {
      LOG.error("Error executing VPX command: " + e.getMessage(), e);
    }
    return false;
  }

  public File export(@NonNull Game game, @NonNull String commandParam, @NonNull String fileSuffix) {
    File gameFile = game.getGameFile();
    File vpxExe = game.getEmulator().getVPXExe();
    File target = new File(gameFile.getParentFile(), FilenameUtils.getBaseName(gameFile.getName()) + "." + fileSuffix);

    try {
      List<String> strings = Arrays.asList(vpxExe.getName(), commandParam, "\"" + gameFile.getAbsolutePath() + "\"");
      LOG.info("Executing VPX " + commandParam + "command: " + String.join(" ", strings));
      SystemCommandExecutor executor = new SystemCommandExecutor(strings);
      executor.setDir(vpxExe.getParentFile());
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

  public boolean launch() {
    GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
    File vpxExe = defaultGameEmulator.getVPXExe();
    try {
      List<String> strings = Arrays.asList(vpxExe.getName());
      SystemCommandExecutor executor = new SystemCommandExecutor(strings);
      executor.setDir(vpxExe.getParentFile());
      executor.executeCommandAsync();

      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (standardErrorFromCommand != null && !StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("VPX command failed:\n" + standardErrorFromCommand);
      }
    } catch (Exception e) {
      LOG.error("Error executing VPX command: " + e.getMessage(), e);
    }
    return false;
  }
}
