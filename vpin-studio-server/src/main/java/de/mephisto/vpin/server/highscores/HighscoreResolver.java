package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class HighscoreResolver {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreResolver.class);

  private List<String> supportedRoms;
  private final SystemService systemService;

  public HighscoreResolver(SystemService systemService) {
    this.systemService = systemService;
    this.loadSupportedScores();
    this.refresh();
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
    this.refresh();

    HighscoreMetadata metadata = new HighscoreMetadata();
    metadata.setScanned(new Date());
    try {
      String romName = game.getRom();
      if (StringUtils.isEmpty(romName)) {
        String msg = "No rom name found.";
        metadata.setStatus(msg);
        LOG.info(msg);
        return metadata;
      }

      metadata.setRom(romName);

      String rawScore = readNvHighscore(game, metadata);
      if (rawScore == null) {
        rawScore = readVRegHighscore(game, metadata);
      }
      if (rawScore == null) {
        rawScore = parseHSFileHighscore(game, metadata);
      }

      if (rawScore == null) {
        String msg = "Reading highscore for '" + game.getGameDisplayName() + "' failed, no nvram file, VPReg.stg entry or EM highscore file found for rom name '" + romName + "'";
        if (metadata.getStatus() == null) {
          metadata.setStatus("No nvram file, VPReg.stg entry or EM highscore file found.");
        }
        LOG.info(msg);
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

  /**
   * Refreshes the extraction of the VPReg.stg file.
   */
  private void refresh() {
    File targetFolder = systemService.getExtractedVPRegFolder();
    if (!targetFolder.exists()) {
      boolean mkdirs = targetFolder.mkdirs();
      if (!mkdirs) {
        LOG.error("Failed to create VPReg target directory");
      }
    }

    //check if we have to unzip the score file using the modified date of the target folder
    updateUserScores(targetFolder);
  }

  private String parseHSFileHighscore(Game game, HighscoreMetadata metadata) throws IOException {
    File hsFile = game.getEMHighscoreFile();
    if (hsFile != null && hsFile.exists()) {
      metadata.setType(HighscoreMetadata.TYPE_EM);
      metadata.setFilename(hsFile.getCanonicalPath());
      metadata.setModified(new Date(hsFile.lastModified()));

      List<String> lines = IOUtils.readLines(new FileInputStream(hsFile), "utf-8");
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
    }
    return null;
  }

  /**
   * We use the manual set rom name to find the highscore in the "/User/VPReg.stg" file.
   */
  private String readVRegHighscore(Game game, HighscoreMetadata metadata) throws IOException {
    File tableHighscoreFolder = getVPRegFolder(game);

    if (tableHighscoreFolder != null && tableHighscoreFolder.exists()) {
      metadata.setType(HighscoreMetadata.TYPE_VREG);
      metadata.setFilename(tableHighscoreFolder.getCanonicalPath());
      metadata.setModified(new Date(tableHighscoreFolder.lastModified()));

      StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");

      int index = 1;
      String highScoreValue = null;
      String initials = null;

      File tableHighscoreFile = new File(tableHighscoreFolder, "HighScore" + index);
      if(!tableHighscoreFile.exists()) {
        metadata.setStatus("Found VReg entry, but no highscore entries in it.");
        return null;
      }

      File tableHighscoreNameFile = new File(tableHighscoreFolder, "HighScore" + index + "Name");
      while (tableHighscoreFile.exists() && tableHighscoreNameFile.exists()) {
        highScoreValue = readFileString(tableHighscoreFile);
        if (highScoreValue != null) {
          highScoreValue = formatScore(highScoreValue);
          initials = readFileString(tableHighscoreNameFile);

          builder.append("#");
          builder.append(index);
          builder.append(" ");
          builder.append(initials);
          builder.append("   ");
          builder.append(highScoreValue);
          builder.append("\n");
        }
        index++;
        tableHighscoreFile = new File(tableHighscoreFolder, "HighScore" + index);
        tableHighscoreNameFile = new File(tableHighscoreFolder, "HighScore" + index + "Name");
      }

      return builder.toString();
    }
    else {
      LOG.debug("No VPReg highscore file found for '" + game.getRom() + "'");
    }
    return null;
  }

  public static String formatScore(String score) {
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    decimalFormat.setGroupingUsed(true);
    decimalFormat.setGroupingSize(3);
    return decimalFormat.format(Long.parseLong(score));
  }

  /**
   * Uses 7zip to unzip the stg file into the configured target folder.
   *
   * @param vpRegFolderFile the VPReg file to expand
   */
  private void updateUserScores(File vpRegFolderFile) {
    if (!systemService.getVPRegFile().exists()) {
      LOG.info("Skipped VPReg extraction, file does not exists yet.");
      return;
    }

    String unzipCommand = systemService.get7ZipCommand();
    try {
      List<String> commands = Arrays.asList("\"" + unzipCommand + "\"", "-aoa", "x", "\"" + systemService.getVPRegFile().getCanonicalPath() + "\"", "-o\"" + vpRegFolderFile.getCanonicalPath() + "\"");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
      executor.setDir(vpRegFolderFile);
      executor.executeCommand();

      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("7zip command '" + String.join(" ", commands) + "' failed: {}", standardErrorFromCommand);
      }
      LOG.info("Finished VPReg folder refresh of " + vpRegFolderFile.getCanonicalPath());
    } catch (Exception e) {
      LOG.info("Failed to init VPReg: " + e.getMessage(), e);
    }
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
    Highscore highscore = null;
    try {
      File nvRam = game.getNvRamFile();
      if (nvRam == null || !nvRam.exists()) {
        return null;
      }

      String nvRamFileName = nvRam.getCanonicalFile().getName();
      String nvRamName = FilenameUtils.getBaseName(nvRamFileName);
      metadata.setFilename(nvRam.getCanonicalPath());
      metadata.setModified(new Date(nvRam.lastModified()));

      if (!this.supportedRoms.contains(nvRamName)) {
        String msg = "The resolved NV ram file '" + nvRamName + "' of game '" + game.getGameDisplayName() + "' is not supported by PINemHi.";
        LOG.warn(msg);
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

  private String executePINemHi(String param, HighscoreMetadata metadata) throws Exception {
    metadata.setType(HighscoreMetadata.TYPE_NVRAM);
    File commandFile = systemService.getPinemhiCommandFile();
    try {
      List<String> commands = Arrays.asList(commandFile.getName(), param);
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(commandFile.getParentFile());
      executor.executeCommand();
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        String error = "Pinemhi command (" + commandFile.getCanonicalPath() + ") failed: " + standardErrorFromCommand;
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

  /**
   * Reads the first line of the given file
   */
  private String readFileString(File file) throws IOException {
    BufferedReader brTest = new BufferedReader(new FileReader(file));
    try {
      String text = brTest.readLine();
      if (text != null) {
        return text.replace("\0", "").trim();
      }
      else {
        LOG.debug("Error reading highscore file " + file.getCanonicalPath() + ", reader returned null.");
      }
      return null;
    } catch (IOException e) {
      throw e;
    } finally {
      brTest.close();
    }
  }


  @Nullable
  public File getVPRegFolder(Game game) {
    String rom = game.getRom();
    if (!StringUtils.isEmpty(rom)) {
      File file = new File(systemService.getExtractedVPRegFolder(), rom);
      if (!file.exists() && !StringUtils.isEmpty(game.getTableName())) {
        file = new File(systemService.getExtractedVPRegFolder(), game.getTableName());
      }
      return file;
    }
    return null;
  }
}
