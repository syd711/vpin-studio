package de.mephisto.vpin.server.patcher;

import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * jptch "Attack from Mars (Bally 1995) g5k 1.3.11.vpx" "AFM LW.dif" test.vpx
 */
@Service
public class PatchingService {
  private final static Logger LOG = LoggerFactory.getLogger(PatchingService.class);

  private final static String JPTCH_EXE = "jptch.exe";

  public String patch(@NonNull Game game, @NonNull File patchFile, @NonNull File targetVpxFile) {
    try {
      List<String> params = Arrays.asList(JPTCH_EXE, game.getGameFile().getAbsolutePath(), patchFile.getAbsolutePath(), targetVpxFile.getAbsolutePath());
      SystemCommandExecutor executor = new SystemCommandExecutor(params, true);
      executor.setDir(new File(SystemService.RESOURCES));
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Patching failed: {}", standardErrorFromCommand);
        return "Patching failed: " + standardErrorFromCommand;
      }
      LOG.info("Successfully patched \"{}\"", game.getGameDisplayName());
    }
    catch (Exception e) {
      LOG.error("Failed to patch {}: {}", game.getGameFile().getAbsolutePath(), e.getMessage(), e);
      return "Patching failed: " + e.getMessage();
    }
    return null;
  }
}
