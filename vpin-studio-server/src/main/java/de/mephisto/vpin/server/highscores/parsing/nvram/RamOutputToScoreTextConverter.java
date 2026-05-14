package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import net.nvrams.mapping.NVRamParser;
import net.nvrams.mapping.pinemhi.PinemhiRamParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.htmlunit.jetty.io.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * This converter does not ensure a unified output for all text documents that are outputted.
 * Instead, it just converts the output so that unexpected empty lines for missing positions are applied already.
 */
public class RamOutputToScoreTextConverter {
  private final static Logger LOG = LoggerFactory.getLogger(RamOutputToScoreTextConverter.class);

  private static final List<NvRamOutputToRaw> svcs = new ArrayList<>();

  public static void registerParser(NvRamOutputToRaw converter) {
    svcs.add(converter);
  }

  public static boolean isSupportedRom(String rom) {
    for (NvRamOutputToRaw svc : svcs) {
      if (svc.isSupportedRom(rom)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public static String convertRamTextToMachineReadable(@NonNull Game game, @NonNull File ramFile) throws Exception {
    boolean nvOffset = false;
    File originalRamFile = ramFile;
    File backedUpRamFile = ramFile;

    try {
      String ramFileName = ramFile.getCanonicalFile().getName().toLowerCase();
      String ramName = FilenameUtils.getBaseName(ramFileName).toLowerCase();
      Locale locale = Locale.getDefault();

      if (game.isVpxGame()) {
        if (ramFileName.contains(" ")) {
          LOG.info("Stripping NV offset from nvram file \"{}\" to check if supported.", ramFileName);
          SLOG.info("Stripping NV offset from nvram file \"" + ramFileName + "\" to check if supported.");
          ramName = ramFileName.substring(0, ramFileName.indexOf(" "));

          //rename the original nvram file so that we can parse with the original name
          originalRamFile = new File(ramFile.getParentFile(), ramName + ".nv");
          if (originalRamFile.exists()) {
            backedUpRamFile = new File(ramFile.getParentFile(), originalRamFile.getName() + ".bak");
            if (backedUpRamFile.exists()) {
              backedUpRamFile.delete();
            }
            FileUtils.copyFile(originalRamFile, backedUpRamFile);
            LOG.info("Temporary renamed original nvram file {} to {}", originalRamFile.getAbsolutePath(), backedUpRamFile.getAbsolutePath());
            SLOG.info("Temporary renamed original nvram file " + originalRamFile.getAbsolutePath() + " to " + backedUpRamFile.getAbsolutePath());
            FileUtils.copyFile(ramFile, originalRamFile);
            LOG.info("Temporary renamed actual nvram file {} to {}", ramFile.getAbsolutePath(), originalRamFile.getAbsolutePath());
            SLOG.info("Temporary renamed actual nvram file " + ramFile.getAbsolutePath() + " to " + originalRamFile.getAbsolutePath());
          }
          nvOffset = true;
        }

        // try with registered service
        String rom = resolveRomNameFromFileanme(originalRamFile);
        for (NvRamOutputToRaw svc : svcs) {
          if (svc.isSupportedRom(rom)) {
            LOG.info("Used NvRam converter {} for {}", svc, ramName);
            return String.join("\n", svc.getRaw(rom, originalRamFile, locale));
          }
        }
      }
      else if (game.isFpGame()) {
        for (NvRamOutputToRaw svc : svcs) {
          NVRamParser parser = svc.getParser();
          if (parser instanceof PinemhiRamParser) {
            List<String> raw = parser.getRaw(null, ramFile, locale);
            if (raw.isEmpty()) {
              return null;
            }

            return String.join("\n", raw);
          }
        }
      }

      LOG.warn("No registered converted to process ram file {}", originalRamFile.getName());
      return null;
    }
    catch (RuntimeIOException ioe) {
      String error = ioe.getMessage();
      SLOG.error(error);
      LOG.error(error);
      return null;
    }
    catch (Exception e) {
      LOG.error(e.getMessage());
      throw e;
    }
    finally {
      if (nvOffset && originalRamFile.delete()) {
        FileUtils.copyFile(backedUpRamFile, originalRamFile);
        LOG.info("Restored original nvram {}", originalRamFile.getAbsolutePath());
      }
    }
  }

  private static String resolveRomNameFromFileanme(File nvFile) throws IOException {
    String rom = nvFile.getName();
    int dotIndex = rom.lastIndexOf('.');
    if (dotIndex > 0) rom = rom.substring(0, dotIndex);
    int hyphenIndex = rom.indexOf('-');
    if (hyphenIndex > 0) rom = rom.substring(0, hyphenIndex);
    return rom;
  }
}
