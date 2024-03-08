package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import de.mephisto.vpin.server.highscores.parsing.nvram.NvRamHighscoreToRawConverter;
import de.mephisto.vpin.server.highscores.parsing.text.TextHighscoreToRawConverter;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

class HighscoreResolver {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolver.class);
  public static final String NO_SCORE_FOUND_MSG = "No nvram file, VPReg.stg entry or highscore text file found.";

  private final SystemService systemService;
  private final ScoringDB scoringDB;

  public HighscoreResolver(@NonNull SystemService systemService) {
    this.systemService = systemService;
    this.scoringDB = systemService.getScoringDatabase();
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
        rawScore = readHSFileHighscore(game, metadata);
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

  private String readHSFileHighscore(Game game, HighscoreMetadata metadata) throws IOException {
    File hsFile = game.getHighscoreTextFile();
    if ((hsFile == null || !hsFile.exists())) {
      hsFile = game.getAlternateHighscoreTextFile();
    }

    if (hsFile != null && hsFile.exists()) {
      metadata.setType(HighscoreType.EM);
      metadata.setFilename(hsFile.getCanonicalPath());
      metadata.setModified(new Date(hsFile.lastModified()));
      return TextHighscoreToRawConverter.convertTextFileTextToMachineReadable(scoringDB, hsFile);
    }
    return null;
  }

  /**
   * We use the manual set rom name to find the highscore in the "/User/VPReg.stg" file.
   */
  private String readVPRegHighscore(Game game, HighscoreMetadata metadata) throws IOException {
    if (game.getRom() != null && scoringDB.getIgnoredVPRegEntries().contains(game.getRom())) {
      return null;
    }

    if (game.getTableName() != null && scoringDB.getIgnoredVPRegEntries().contains(game.getTableName())) {
      return null;
    }

    File vpRegFile = game.getEmulator().getVPRegFile();
    VPReg reg = new VPReg(vpRegFile, game.getRom(), game.getTableName());
    if (reg.containsGame()) {
      metadata.setType(HighscoreType.VPReg);
      metadata.setFilename(vpRegFile.getCanonicalPath());
      metadata.setModified(new Date(vpRegFile.lastModified()));

      ScoreParsingSummary summary = reg.readHighscores();
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
      if (nvRamFileName.contains(" ")) {
        LOG.info("Stripping NV offset from nvram file \"" + nvRamFileName + "\"");
        nvRamName = nvRamFileName.substring(0, nvRamFileName.indexOf(" "));
        nvRamFileName = nvRamName + ".nv";
      }

      metadata.setFilename(nvRam.getCanonicalPath());
      metadata.setModified(new Date(nvRam.lastModified()));

      List<String> supportedNvRams = systemService.getScoringDatabase().getSupportedNvRams();
      if (!supportedNvRams.contains(nvRamName) || scoringDB.getNotSupported().contains(FilenameUtils.getBaseName(nvRamName))) {
        String msg = "The NV ram file \"" + nvRamName + ".nv\" is not supported by PINemHi.";
        metadata.setStatus(msg);
        return null;
      }

      return executePINemHi(nvRamFileName, metadata, nvRam);
    } catch (Exception e) {
      String msg = "Failed to parse highscore: " + e.getMessage();
      metadata.setStatus(msg);
      LOG.error(msg, e);
    }
    return null;
  }

  private String executePINemHi(@NonNull String nvRamFileName, @NonNull HighscoreMetadata metadata, @NonNull File nvRam) throws Exception {
    metadata.setType(HighscoreType.NVRam);
    File commandFile = systemService.getPinemhiCommandFile();
    try {
      return NvRamHighscoreToRawConverter.convertNvRamTextToMachineReadable(commandFile, nvRamFileName);
    } catch (Exception e) {
      metadata.setStatus(e.getMessage());
      throw e;
    }
  }
}
