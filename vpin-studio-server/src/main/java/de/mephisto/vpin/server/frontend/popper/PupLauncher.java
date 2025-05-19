package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.system.SystemService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class PupLauncher {
  private final static Logger LOG = LoggerFactory.getLogger(PupLauncher.class);

  public boolean launch(int gameId) {
    try {
      File launchFolder = new File(SystemService.RESOURCES);
      List<String> params = Arrays.asList("cmd", "/c", "start", "puplauncher.exe", String.valueOf(gameId));
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(launchFolder);
      executor.executeCommandAsync();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Popper puplauncher.exe failed: {}", standardErrorFromCommand);
        return false;
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("puplauncher failed: {}", e.getMessage(), e);
    }
    return false;
  }
}
