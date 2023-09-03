package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.HighscoreType;
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
  public static final String NO_SCORE_FOUND_MSG = "No nvram file, VPReg.stg entry or EM highscore file found.";

  private List<String> supportedRoms;
  private final SystemService systemService;

  public HighscoreResolver(@NonNull SystemService systemService) {
    this.systemService = systemService;
    this.loadSupportedScores();
  }

  private void loadSupportedScores() {
    try {
      String roms = executePINemHi("-lr", new HighscoreMetadata());
      this.supportedRoms = Arrays.asList(roms.split("\n"));
    } catch (Exception e) {
      LOG.error("Failed to load supported rom names from PINemHi: " + e.getMessage(), e);
    }
  }

  /**
   * Return a highscore object for the given table or null if no highscore has been achieved or created yet.
   */
  @NonNull
  public HighscoreMetadata readHighscore(Game game) {
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
    File hsFile = game.getEMHighscoreFile();
    if (hsFile != null && hsFile.exists()) {
      metadata.setType(HighscoreType.EM);
      metadata.setFilename(hsFile.getCanonicalPath());
      metadata.setModified(new Date(hsFile.lastModified()));

      FileInputStream fileInputStream = null;
      try {
        fileInputStream = new FileInputStream(hsFile);
        List<String> lines = IOUtils.readLines(fileInputStream, Charset.defaultCharset());
        if(lines.size() == 16) {
          lines = lines.subList(1, lines.size());
        }

        if (lines.size() >= 15) {
          StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

          int index = 5;
          for (int i = 1; i < 6; i++) {
            String score = lines.get(index);
            String initials = lines.get(index + 5);

            builder.append("#");
            builder.append(i);
            builder.append(" ");
            builder.append(initials);
            builder.append("   ");
            builder.append(score);
            builder.append("\n");

            index++;
          }
          return builder.toString();
        }
        else if (lines.size() == 8) {
          StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

          String score1 = lines.get(1);
          String score2 = lines.get(2);
          builder.append("#1");
          builder.append(" ");
          builder.append("???");
          builder.append("   ");
          builder.append(score2);
          builder.append("\n");

          builder.append("#2");
          builder.append(" ");
          builder.append("???");
          builder.append("   ");
          builder.append(score1);
          builder.append("\n");

          return builder.toString();
        }
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
    VPReg reg = new VPReg(systemService.getVPRegFile(), game.getRom(), game.getTableName());

    //TODO cleanup metadata usage
    if (reg.containsGame()) {
      metadata.setType(HighscoreType.VPReg);
      metadata.setFilename(systemService.getVPRegFile().getCanonicalPath());
      metadata.setModified(new Date(systemService.getVPRegFile().lastModified()));

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

      String nvRamFileName = nvRam.getCanonicalFile().getName();
      String nvRamName = FilenameUtils.getBaseName(nvRamFileName);
      metadata.setFilename(nvRam.getCanonicalPath());
      metadata.setModified(new Date(nvRam.lastModified()));

      if (!this.supportedRoms.contains(nvRamName)) {
        String msg = "The resolved NV ram file '" + nvRamName + "' of game '" + game.getGameDisplayName() + "' is not supported by PINemHi.";
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

  private String executePINemHi(String nvRamFileName, HighscoreMetadata metadata) throws Exception {
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
      return standardOutputFromCommand.toString();
    } catch (Exception e) {
      String msg = commandFile.getCanonicalPath() + " command failed for directory " + commandFile.getCanonicalPath() + ": " + e.getMessage();
      metadata.setStatus(msg);
      LOG.error(msg);
      throw e;
    }
  }
}
