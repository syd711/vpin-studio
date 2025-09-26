package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This converter does not ensure a unified output for all text documents that are outputted through pinemhi.
 * Instead, it just converts the output from pinemhi so that unexpected empty lines for missing positions are applied already.
 */
public class NvRamOutputToScoreTextConverter {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamOutputToScoreTextConverter.class);

  private final static List<ScoreNvRamAdapter> adapters = new ArrayList<>();

  static {
    adapters.add(new SinglePlayerScoreAdapter("algar_l1.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alienstr.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alpok_b6.nv", 1));
    adapters.add(new FourColumnScoreAdapter("monopoly.nv"));
    adapters.add(new SkipFirstListScoreAdapter("godzilla.nv"));
//    adapters.add(new NewLineAfterFirstScoreAdapter("kiko_a10.nv"));
    adapters.add(new Anonymous5PlayerScoreAdapter("punchy.nv"));
    adapters.add(new FixTitleScoreAdapter("rs_l6.nv", "TODAY'S HIGHEST SCORES", "ALL TIME HIGHEST SCORES"));
    adapters.add(new SinglePlayerScoreAdapter());
    adapters.add(new MultiBlockAdapter("pool_l7.nv", 8));
    adapters.add(new AlteringLinesWithoutPosAdapter("wrldtou2.nv", 5));
  }

  @Nullable
  public static String convertNvRamTextToMachineReadable(@NonNull File commandFile, @NonNull File nvRam) throws Exception {
    boolean nvOffset = false;
    File originalNVRamFile = nvRam;
    File backedUpRamFile = nvRam;

    try {
      String nvRamFileName = nvRam.getCanonicalFile().getName().toLowerCase();
      String pinemHiSupportedNVRamName = FilenameUtils.getBaseName(nvRamFileName).toLowerCase();
      if (nvRamFileName.contains(" ")) {
        LOG.info("Stripping NV offset from nvram file \"" + nvRamFileName + "\" to check if supported.");
        SLOG.info("Stripping NV offset from nvram file \"" + nvRamFileName + "\" to check if supported.");
        pinemHiSupportedNVRamName = nvRamFileName.substring(0, nvRamFileName.indexOf(" "));

        //rename the original nvram file so that we can parse with the original name
        originalNVRamFile = new File(nvRam.getParentFile(), pinemHiSupportedNVRamName + ".nv");
        if (originalNVRamFile.exists()) {
          backedUpRamFile = new File(nvRam.getParentFile(), originalNVRamFile.getName() + ".bak");
          if (backedUpRamFile.exists()) {
            backedUpRamFile.delete();
          }
          FileUtils.copyFile(originalNVRamFile, backedUpRamFile);
          LOG.info("Temporary renamed original nvram file " + originalNVRamFile.getAbsolutePath() + " to " + backedUpRamFile.getAbsolutePath());
          SLOG.info("Temporary renamed original nvram file " + originalNVRamFile.getAbsolutePath() + " to " + backedUpRamFile.getAbsolutePath());
          FileUtils.copyFile(nvRam, originalNVRamFile);
          LOG.info("Temporary renamed actual nvram file " + nvRam.getAbsolutePath() + " to " + originalNVRamFile.getAbsolutePath());
          SLOG.info("Temporary renamed actual nvram file " + nvRam.getAbsolutePath() + " to " + originalNVRamFile.getAbsolutePath());
        }
        nvOffset = true;
      }

      List<String> commands = Arrays.asList(commandFile.getName(), originalNVRamFile.getName().toLowerCase());
//      LOG.info("PinemHI: " + String.join(" ", commands));
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(commandFile.getParentFile());
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        String error = "Pinemhi command (" + commandFile.getCanonicalPath() + " " + pinemHiSupportedNVRamName + ") failed. Error output:\n" + standardErrorFromCommand + "\nStandard output:\n" +standardOutputFromCommand;
//        String error = "Pinemhi command (" + commandFile.getCanonicalPath() + " " + pinemHiSupportedNVRamName + ") failed (details skipped).";
        SLOG.error(error);
        LOG.error(error);
        return null;
      }
      String stdOut = standardOutputFromCommand.toString();
      return convertOutputToRaw(nvRamFileName, stdOut);
    }
    catch (Exception e) {
      LOG.error(e.getMessage());
      throw e;
    }
    finally {
      if (nvOffset && originalNVRamFile.delete()) {
        FileUtils.copyFile(backedUpRamFile, originalNVRamFile);
        LOG.info("Restored original nvram " + originalNVRamFile.getAbsolutePath());
      }
    }
  }

  @NonNull
  private static String convertOutputToRaw(@NonNull String nvRamFileName, String stdOut) throws Exception {
    // replace french space character, displayed ÿ with "."
    /*stdOut = stdOut
        .replaceAll("\u00ff", ".")
        .replaceAll("\u00a0", ".")
        .replaceAll("\u202f", ".")
        .replaceAll("\ufffd", ".");*/

    //check for pre-formatting
    List<String> lines = Arrays.asList(stdOut.trim().split("\n"));
    for (ScoreNvRamAdapter adapter : adapters) {
      if (adapter.isApplicable(nvRamFileName, lines)) {
        LOG.info("Converted score using {}", adapter.getClass().getSimpleName());
        return adapter.convert(nvRamFileName, lines);
      }
    }
    return stdOut;
  }
}
