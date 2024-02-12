package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import de.mephisto.vpin.server.util.vpreg.VPRegScoreSummary;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class HighscoreResolver {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolver.class);
  public static final String NO_SCORE_FOUND_MSG = "No nvram file, VPReg.stg entry or highscore text file found.";

  private final SystemService systemService;

  public HighscoreResolver(@NonNull SystemService systemService) {
    this.systemService = systemService;
  }

  /**
   * Return a highscore object for the given table or null if no highscore has been achieved or created yet.
   */
  @NonNull
  public synchronized HighscoreMetadata readHighscore(Game game) {
    HighscoreMetadata metadata = new HighscoreMetadata();
    metadata.setScanned(new Date());
    try {
      String romName = game.getRom();
      if (StringUtils.isEmpty(romName)) {
        romName = game.getTableName();
      }

      if (StringUtils.isEmpty(romName)) {
        String msg = "No rom or table name found.";
        metadata.setStatus(msg);
        return metadata;
      }

      metadata.setRom(romName);

      //always check NV ram first, the table might store additional data into the VPReg.stg too
      String rawScore = readNvHighscore(game, metadata);
      if (rawScore == null) {
        rawScore = readVPRegHighscore(game, metadata);
      }
      if (rawScore == null) {
        rawScore = parseHSFileHighscore(game, metadata);
      }

      if (rawScore == null) {
        if (metadata.getStatus() == null) {
          metadata.setStatus(NO_SCORE_FOUND_MSG);
        }
      }
      else {
        LOG.debug("Successfully read highscore for " + game.getGameDisplayName());
        metadata.setRaw(rawScore);
      }

    } catch (Exception e) {
      LOG.error("Failed to find highscore for table {}: {}", game.getGameFileName(), e.getMessage(), e);
    }
    return metadata;
  }

  private String parseHSFileHighscore(Game game, HighscoreMetadata metadata) throws IOException {
    File hsFile = game.getHighscoreTextFile();
    if ((hsFile == null || !hsFile.exists())) {
      hsFile = game.getAlternateHighscoreTextFile();
    }

    if(hsFile != null && hsFile.exists()) {
      metadata.setType(HighscoreType.EM);
      metadata.setFilename(hsFile.getCanonicalPath());
      metadata.setModified(new Date(hsFile.lastModified()));

      FileInputStream fileInputStream = null;
      try {
        fileInputStream = new FileInputStream(hsFile);
        List<String> lines = IOUtils.readLines(fileInputStream, Charset.defaultCharset());
        return HighscoreRawToMachineReadableConverter.convertToMachineReadable(lines);
      } catch (IOException e) {
        LOG.error("Error reading EM highscore file: " + e.getMessage(), e);
      } finally {
        if (fileInputStream != null) {
          fileInputStream.close();
        }
      }
    }
    return null;
  }

  /**
   * We use the manual set rom name to find the highscore in the "/User/VPReg.stg" file.
   */
  private String readVPRegHighscore(Game game, HighscoreMetadata metadata) throws IOException {
    File vpRegFile = game.getEmulator().getVPRegFile();
    VPReg reg = new VPReg(vpRegFile, game.getRom(), game.getTableName());

    //TODO cleanup metadata usage
    if (reg.containsGame()) {
      metadata.setType(HighscoreType.VPReg);
      metadata.setFilename(vpRegFile.getCanonicalPath());
      metadata.setModified(new Date(vpRegFile.lastModified()));

      VPRegScoreSummary summary = reg.readHighscores();
      if (summary != null) {
        metadata.setRaw(summary.toRaw());
      }
      if (StringUtils.isEmpty(metadata.getRaw())) {
        metadata.setStatus("Found VPReg entry, but no highscore entries in it.");
      }
      return metadata.getRaw();
    }
    LOG.debug("No VPReg highscore file found for '" + game.getRom() + "'");
    return null;
  }

  /**
   * Executes a single PINemHi command for the given game.
   *
   * @param game     the game to parse the highscore for
   * @param metadata the metadata that are collected while parsing
   * @return the Highscore object or null if no highscore could be parsed.
   */
  @Nullable
  private String readNvHighscore(Game game, HighscoreMetadata metadata) {
    try {
      File nvRam = game.getNvRamFile();
      if (!nvRam.exists()) {
        return null;
      }

      String nvRamFileName = nvRam.getCanonicalFile().getName().toLowerCase();
      String nvRamName = FilenameUtils.getBaseName(nvRamFileName).toLowerCase();
      metadata.setFilename(nvRam.getCanonicalPath());
      metadata.setModified(new Date(nvRam.lastModified()));

      List<String> supportedNvRams = systemService.getScoringDatabase().getSupportedNvRams();
      if (!supportedNvRams.contains(nvRamName)) {
        String msg = "The NV ram file \"" + nvRamName + ".nv\" is not supported by PINemHi.";
        LOG.info(msg);
        metadata.setStatus(msg);
        return null;
      }

      return executePINemHi(nvRamFileName, metadata);
    } catch (Exception e) {
      String msg = "Failed to parse highscore: " + e.getMessage();
      metadata.setStatus(msg);
      LOG.error(msg, e);
    }
    return null;
  }

  private String readPINemHiStatus(String cmd) {
    File commandFile = systemService.getPinemhiCommandFile();
    try {
      List<String> commands = Arrays.asList(commandFile.getName(), cmd);
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(commandFile.getParentFile());
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      return standardOutputFromCommand.toString();
    } catch (Exception e) {
      LOG.error("Failed to read pinemhi data: " + e.getMessage(), e);
    }
    return "";
  }

  private String executePINemHi(@NonNull String nvRamFileName, @NonNull HighscoreMetadata metadata) throws Exception {
    metadata.setType(HighscoreType.NVRam);
    File commandFile = systemService.getPinemhiCommandFile();
    try {
      List<String> commands = Arrays.asList(commandFile.getName(), nvRamFileName);
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(commandFile.getParentFile());
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        String error = "Pinemhi command (" + commandFile.getCanonicalPath() + " " + nvRamFileName + ") failed: " + standardErrorFromCommand;
        LOG.error(error);
        metadata.setStatus(error);
        throw new Exception(error);
      }
      String stdOut = standardOutputFromCommand.toString();

      //check for pre-formatting
      List<String> list = Arrays.asList(stdOut.trim().split("\n"));
      if(list.size() < 5) {
        LOG.info("Converting nvram highscore data of \"" + nvRamFileName + "\" to a readable format, because output length are only " + list.size() + " lines.");
        String raw = HighscoreRawToMachineReadableConverter.convertToMachineReadable(list);
        if(raw == null) {
          LOG.info("Invalid pinemhi output for " + nvRamFileName + ":\n" + stdOut);
          metadata.setStatus("Invalid parsing output, maybe the nvram has been resetted?");
        }
        else {
          return raw;
        }
      }
      return stdOut;
    } catch (Exception e) {
      String msg = commandFile.getCanonicalPath() + " command failed for directory " + commandFile.getCanonicalPath() + ": " + e.getMessage();
      metadata.setStatus(msg);
      LOG.error(msg);
      throw e;
    }
  }
}
