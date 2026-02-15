package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.system.ScoringDBMapping;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import de.mephisto.vpin.server.highscores.parsing.ini.IniHighscoreAdapters;
import de.mephisto.vpin.server.highscores.parsing.nvram.NvRamOutputToScoreTextConverter;
import de.mephisto.vpin.server.highscores.parsing.text.TextHighscoreAdapters;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegFile;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class HighscoreResolver implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolver.class);
  public static final String NO_SCORE_FOUND_MSG = "No nvram file, VPReg.stg entry or highscore text file found.";

  @Autowired
  private SystemService systemService;

  @Autowired
  private TextHighscoreAdapters textHighscoreAdapters;

  @Autowired
  private IniHighscoreAdapters initHighscoreAdapters;

  @Autowired
  private VPRegService vpRegService;

  @Autowired
  private FolderLookupService folderLookupService;


  //--------------------------------------------
  // Resolution of highscore files

  @Nullable
  public File getHighscoreFile(Game game) {
    HighscoreType highscoreType = game.getHighscoreType();
    if (highscoreType != null) {
      switch (highscoreType) {
        case EM: {
          return folderLookupService.getHighscoreTextFile(game);
        }
        case VPReg: {
          VPRegFile vpRegFileForGame = folderLookupService.getVPRegFileForGame(game);
          if(vpRegFileForGame != null) {
            return vpRegFileForGame.getFile();
          }
          break;
        }
        case NVRam: {
          return getNvRamFile(game);
        }
        case Ini: {
          return getHighscoreIniFile(game);
        }
      }
    }
    return null;
  }

  @Nullable
  private File getAlternateHighscoreTextFile(Game game, @NonNull String name) {
    if (!StringUtils.isEmpty(name)) {
      if (!name.endsWith(".txt")) {
        name = name + ".txt";
      }
      return new File(folderLookupService.getUserFolder(game), name);
    }
    return null;
  }

  @Nullable
  public File getHighscoreIniFile(Game game) {
    File iniFile = null;
    if (!StringUtils.isEmpty(game.getRom())) {
      String iniScoreName = FilenameUtils.getBaseName(game.getRom()) + "_glf.ini";
      iniFile = new File(game.getGameFile().getParentFile(), iniScoreName);
    }

    if (iniFile == null || !iniFile.exists()) {
      if (!StringUtils.isEmpty(game.getTableName())) {
        String iniScoreName = FilenameUtils.getBaseName(game.getTableName()) + "_glf.ini";
        iniFile = new File(game.getGameFile().getParentFile(), iniScoreName);
      }
    }
    return iniFile;
  }

  private String getHighscoreIniFilename(Game game) {
    File iniFile = getHighscoreIniFile(game);
    if (iniFile != null && iniFile.exists()) {
      return iniFile.getAbsolutePath();
    }
    return null;
  }

  @Nullable
  public File getNvRamFile(@NonNull Game game) {
    if (game.getEmulator() == null || game.getEmulator().getMameDirectory() == null) {
      return null;
    }

    File nvRamFolder = folderLookupService.getNvRamFolder(game);
    String rom = game.getRom();
    File defaultNvRam = new File(nvRamFolder, rom + ".nv");
    if (defaultNvRam.exists() && game.getNvOffset() == 0) {
      return defaultNvRam;
    }

    //if the text file exists, the version matches with the current table, so this one was played last and the default nvram has the latest score
    File versionTextFile = new File(nvRamFolder, game.getRom() + " v" + game.getNvOffset() + ".txt");
    if (versionTextFile.exists()) {
      return defaultNvRam;
    }

    //else, we can check if a nv file with the alias and version exists which means the another table with the same rom has been played after this table
    File nvOffsettedNvRam = new File(nvRamFolder, rom + " v" + game.getNvOffset() + ".nv");
    if (nvOffsettedNvRam.exists()) {
      return nvOffsettedNvRam;
    }

    return defaultNvRam;
  }


  //--------------------------------------------

  public boolean deleteTextScore(Game game, long score) {
    File hsFile = folderLookupService.getHighscoreTextFile(game);
    if ((hsFile == null || !hsFile.exists())) {
      hsFile = getAlternateHighscoreTextFile(game, game.getTableName());
    }

    if ((hsFile == null || !hsFile.exists())) {
      if (!StringUtils.isEmpty(game.getRom())) {
        ScoringDBMapping highscoreMapping = systemService.getScoringDatabase().getHighscoreMapping(game.getRom());
        if (highscoreMapping != null && !StringUtils.isEmpty(highscoreMapping.getTextFile())) {
          hsFile = getAlternateHighscoreTextFile(game, highscoreMapping.getTextFile());
        }
      }
    }

    if (hsFile != null && hsFile.exists()) {
      return textHighscoreAdapters.resetHighscores(systemService.getScoringDatabase(), hsFile, score);
    }
    return false;
  }

  public boolean deleteIniScore(Game game, long score) {
    File iniFile = getHighscoreIniFile(game);
    if (iniFile != null && iniFile.exists()) {
      return initHighscoreAdapters.resetHighscores(systemService.getScoringDatabase(), iniFile, score);
    }
    return false;
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
        SLOG.info("Game " + game.getGameDisplayName() + " is not a VPX game.");
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
        rawScore = readIniHighscore(game, metadata);
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

    }
    catch (Exception e) {
      LOG.error("Failed to find highscore for table {}: {}", game.getGameFileName(), e.getMessage(), e);
    }
    return metadata;
  }

  private String readHSFileHighscore(@NonNull Game game, @NonNull HighscoreMetadata metadata) throws IOException {
    File hsFile = folderLookupService.getHighscoreTextFile(game);
    if ((hsFile == null || !hsFile.exists())) {
      hsFile = getAlternateHighscoreTextFile(game, game.getTableName());
    }

    if ((hsFile == null || !hsFile.exists())) {
      if (!StringUtils.isEmpty(game.getRom())) {
        ScoringDBMapping highscoreMapping = systemService.getScoringDatabase().getHighscoreMapping(game.getRom());
        if (highscoreMapping != null && !StringUtils.isEmpty(highscoreMapping.getTextFile())) {
          hsFile = getAlternateHighscoreTextFile(game, highscoreMapping.getTextFile());
        }
      }
    }

    if (hsFile != null && hsFile.exists()) {
      metadata.setType(HighscoreType.EM);
      metadata.setFilename(hsFile.getCanonicalPath());
      metadata.setModified(new Date(hsFile.lastModified()));
      metadata.setStatus(null);

      return textHighscoreAdapters.convertTextFileTextToMachineReadable(metadata, systemService.getScoringDatabase(), hsFile);
    }
    return null;
  }

  private String readIniHighscore(@NonNull Game game, @NonNull HighscoreMetadata metadata) throws IOException {
    File iniFile = getHighscoreIniFile(game);
    if (iniFile != null && iniFile.exists()) {
      metadata.setType(HighscoreType.Ini);
      metadata.setFilename(iniFile.getCanonicalPath());
      metadata.setModified(new Date(iniFile.lastModified()));
      metadata.setStatus(null);

      return initHighscoreAdapters.convertTextFileTextToMachineReadable(metadata, systemService.getScoringDatabase(), iniFile);
    }
    return null;
  }

  /**
   * We use the manual set rom name to find the highscore in the "/User/VPReg.stg" file.
   */
  private String readVPRegHighscore(@NonNull Game game, HighscoreMetadata metadata) throws IOException {
    if (game.getRom() != null && systemService.getScoringDatabase().getIgnoredVPRegEntries().contains(game.getRom())) {
      SLOG.info("\"" + game.getGameDisplayName() + "\" was marked as to be ignored for VPReg.stg highscores.");
      return null;
    }

    if (game.getTableName() != null && systemService.getScoringDatabase().getIgnoredVPRegEntries().contains(game.getTableName())) {
      SLOG.info("\"" + game.getGameDisplayName() + "\" was marked as to be ignored for VPReg.stg highscores.");
      return null;
    }

    VPRegFile vpRegFile = vpRegService.getVPRegFile(game);
    if (vpRegFile != null && vpRegFile.isValid()) {
      metadata.setType(HighscoreType.VPReg);
      metadata.setFilename(vpRegFile.getCanonicalPath());
      metadata.setModified(vpRegFile.getLastModified());

      ScoreParsingSummary summary = vpRegFile.getScoreParsingSummary();
      if (summary != null) {
        metadata.setStatus(null);
        metadata.setRaw(summary.toRaw());
      }
      if (StringUtils.isEmpty(metadata.getRaw())) {
        metadata.setStatus("Found VPReg entry, but no highscore entries in it.");
        SLOG.info("Found VPReg entry, but no highscore entries in it.");
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
  private String readNvHighscore(@NonNull Game game, HighscoreMetadata metadata) {
    try {
      File nvRam = getNvRamFile(game);
      if (nvRam == null || !nvRam.exists()) {
        return null;
      }

      String nvRamFileName = nvRam.getCanonicalFile().getName().toLowerCase();
      String nvRamName = FilenameUtils.getBaseName(nvRamFileName).toLowerCase();
      if (nvRamFileName.contains(" ")) {
        LOG.info("Stripping NV offset from nvram file \"" + nvRamFileName + "\" to check if supported.");
        nvRamName = nvRamFileName.substring(0, nvRamFileName.indexOf(" "));
      }

      metadata.setFilename(nvRam.getCanonicalPath());
      metadata.setModified(new Date(nvRam.lastModified()));

      List<String> supportedNvRams = systemService.getScoringDatabase().getSupportedNvRams();
      if (!supportedNvRams.contains(nvRamName) || systemService.getScoringDatabase().getNotSupported().contains(FilenameUtils.getBaseName(nvRamName))) {
        String msg = "The NV ram file \"" + nvRamName + ".nv\" is not supported by PINemHi.";
        SLOG.info(msg);
        metadata.setStatus(msg);
        return null;
      }
      metadata.setType(HighscoreType.NVRam);

      return executePINemHi(nvRam);
    }
    catch (Exception e) {
      String msg = "Failed to parse highscore: " + e.getMessage();
      SLOG.error(msg);
      metadata.setStatus(msg);
      LOG.error(msg, e);
    }
    return null;
  }

  @Nullable
  private String executePINemHi(@NonNull File nvRam) throws Exception {
    File commandFile = systemService.getPinemhiCommandFile();
    try {
      return NvRamOutputToScoreTextConverter.convertNvRamTextToMachineReadable(commandFile, nvRam);
    }
    catch (Exception e) {
      throw e;
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    //nothing
  }
}
