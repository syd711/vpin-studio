package de.mephisto.vpin.server.frontend.popper;

import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * See
 * https://www.nailbuster.com/wikipinup/doku.php?id=web_remote_control
 */
public class PupEventEmitter {
  private final static Logger LOG = LoggerFactory.getLogger(PupEventEmitter.class);

  private final File installationFolder;

  public PupEventEmitter(File installationFolder) {
    this.installationFolder = installationFolder;
  }

  public boolean sendPupEvent(int id, int secondsWait) {
    try {
      File launchFolder = new File(installationFolder, "Launch");
      List<String> params = Arrays.asList("cmd", "/c", "start", "SendPuPEvent.exe", String.valueOf(id));
      SystemCommandExecutor executor = new SystemCommandExecutor(params, false);
      executor.setDir(launchFolder);
      executor.executeCommandAsync();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Popper SendPuPEvent.exe failed: {}", standardErrorFromCommand);
        return false;
      }

      if (secondsWait > 0) {
        Thread.sleep(secondsWait * 1000);
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("PupEventEmitter failed: {}", e.getMessage(), e);
    }
    return false;
  }
}
