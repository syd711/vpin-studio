package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.FourColumnScoreAdapter;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.ScoreNvRamAdapter;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.SinglePlayerScoreAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NvRamHighscoreToRawConverter {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamHighscoreToRawConverter.class);

  private final static List<ScoreNvRamAdapter> adapters = new ArrayList<>();

  static {
    adapters.add(new SinglePlayerScoreAdapter("algar_l1.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alienstr.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alpok_b6.nv", 1));
    adapters.add(new FourColumnScoreAdapter("monopoly.nv"));
    adapters.add(new SinglePlayerScoreAdapter());
  }

  public static String convertNvRamTextToMachineReadable(@NonNull File commandFile, @NonNull String nvRamFileName) throws Exception {
    try {
      List<String> commands = Arrays.asList(commandFile.getName(), nvRamFileName);
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(commandFile.getParentFile());
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        String error = "Pinemhi command (" + commandFile.getCanonicalPath() + " " + nvRamFileName + ") failed: " + standardErrorFromCommand;
        throw new Exception(error);
      }
      String stdOut = standardOutputFromCommand.toString();
      return convertOutputToRaw(nvRamFileName, stdOut);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw e;
    }
  }

  @NotNull
  private static String convertOutputToRaw(@NonNull String nvRamFileName, String stdOut) throws Exception {
    //check for pre-formatting
    List<String> lines = Arrays.asList(stdOut.trim().split("\n"));
    for (ScoreNvRamAdapter adapter : adapters) {
      if (adapter.isApplicable(nvRamFileName, lines)) {
        return adapter.convert(nvRamFileName, lines);
      }
    }
    return stdOut;
  }
}
