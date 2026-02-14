package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.HighscoreBackup;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

@Service
public class HighscoreBackupService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreBackupService.class);

  public static final String FILE_SUFFIX = "hsbckp";
  public static final String DESCRIPTOR_JSON = "descriptor.json";
  public static final String VPREG_STG_JSON = "vpreg-stg.json";

  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private HighscoreResolver highscoreResolver;

  @Autowired
  private SystemService systemService;

  public boolean delete(@NonNull String rom, String filename) {
    File folder = new File(systemService.getBackupFolder(), rom);
    File archive = new File(folder, filename);
    if (archive.exists() && !archive.delete()) {
      throw new UnsupportedOperationException("Failed to delete " + archive.getAbsolutePath());
    }
    LOG.info("Deleted " + archive.getAbsolutePath());
    return true;
  }

  public List<HighscoreBackup> getBackups(@NonNull String rom) {
    File folder = new File(systemService.getBackupFolder(), rom);
    List<HighscoreBackup> result = new ArrayList<>();
    if (folder.exists()) {
      File[] files = folder.listFiles((dir, name) -> name.endsWith("." + FILE_SUFFIX));
      if (files != null) {
        for (File file : files) {
          HighscoreBackup highscoreBackup = readBackupFile(file);
          if (highscoreBackup != null) {
            result.add(highscoreBackup);
          }
        }
      }
    }

    result.sort(Comparator.comparing(HighscoreBackup::getCreationDate));
    Collections.reverse(result);
    return result;
  }

  @Nullable
  public File backup(Game game) {
    String rom = game.getRom();
    if (StringUtils.isEmpty(rom)) {
      rom = game.getTableName();
    }
    if (!StringUtils.isEmpty(rom)) {
      File folder = new File(systemService.getBackupFolder(), rom);
      return writeBackupFile(game, folder);
    }
    return null;
  }

  public boolean restore(@NonNull Game game, @NonNull List<Game> games, @NonNull String filename) {
    String rom = game.getRom();
    if (StringUtils.isEmpty(rom)) {
      rom = game.getTableName();
    }

    File backupRomFolder = new File(systemService.getBackupFolder(), rom);
    File backupFile = new File(backupRomFolder, filename);
    boolean result = restoreBackupFile(game.getEmulator(), backupFile);
    if (result) {
      highscoreService.setPauseHighscoreEvents(true);
      for (Game allRomGames : games) {
        highscoreService.scanScore(allRomGames, EventOrigin.USER_INITIATED);
      }
      highscoreService.setPauseHighscoreEvents(false);
    }
    return result;
  }

  //--------------------------------------

  public HighscoreBackup readBackupFile(@NonNull File archiveFile) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      String json = ZipUtil.readZipFile(archiveFile, DESCRIPTOR_JSON);

      return objectMapper.readValue(json, HighscoreBackup.class);
    }
    catch (Exception e) {
      LOG.error("Failed to read " + archiveFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }

  private File writeDescriptorJson(Game game, File folder, Highscore highscore, String filename) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    HighscoreBackup backup = new HighscoreBackup();
    backup.setCreationDate(new Date());
    backup.setHighscoreType(game.getHighscoreType());
    backup.setRaw(highscore.getRaw());
    backup.setFilename(filename);

    String rom = game.getRom();
    if (StringUtils.isEmpty(rom)) {
      rom = game.getTableName();
    }
    backup.setRom(rom);
    backup.setHighscoreFilename(new File(highscore.getFilename()).getName());

    if (!folder.exists() && !folder.mkdirs()) {
      LOG.error("Failed to create " + folder.getAbsolutePath());
    }

    File descriptorFile = new File(folder, DESCRIPTOR_JSON);
    if (descriptorFile.exists() && !descriptorFile.delete()) {
      LOG.error("Failed to delete existing backup descriptor file {}", descriptorFile.getAbsolutePath());
    }

    String json = objectMapper.writeValueAsString(backup);
    Files.write(descriptorFile.toPath(), json.getBytes());
    LOG.info("Written temporary highscore export descriptor file " + descriptorFile.getAbsolutePath());
    return descriptorFile;
  }

  private boolean writeHighscoreBackup(Game game, GameEmulator emulator, Highscore highscore, File romBackupFolder, File tempFile, String filename) {
    boolean highscoreWritten = false;

    try (FileOutputStream fos = new FileOutputStream(tempFile);
         ZipOutputStream zipOut = new ZipOutputStream(fos)) {

      File descriptorJsonFile = writeDescriptorJson(game, romBackupFolder, highscore, filename);

      ZipUtil.zipFile(descriptorJsonFile, descriptorJsonFile.getName(), zipOut);

      //store highscore
      //zip EM file
      File highscoreTextFile = highscoreResolver.getHighscoreTextFile(game);
      if (highscoreTextFile != null && highscoreTextFile.exists()) {
        ZipUtil.zipFile(highscoreTextFile, highscoreTextFile.getName(), zipOut);
        highscoreWritten = true;
      }

      //zip nvram file
      File nvRamFile = highscoreResolver.getNvRamFile(game);
      if (nvRamFile.exists()) {
        ZipUtil.zipFile(nvRamFile, nvRamFile.getName(), zipOut);
        highscoreWritten = true;
      }

      //write VPReg.stg data
      if (HighscoreType.VPReg.equals(game.getHighscoreType())) {
        File vprRegFile = emulator.getVPRegFile();
        VPReg reg = new VPReg(vprRegFile, game.getRom(), game.getTableName());
        String gameData = reg.toJson();
        if (gameData != null) {
          File regBackupTemp = File.createTempFile("vpreg-stg", "json");
          regBackupTemp.deleteOnExit();
          Files.write(regBackupTemp.toPath(), gameData.getBytes());
          ZipUtil.zipFile(regBackupTemp, VPReg.ARCHIVE_FILENAME, zipOut);
          if (!regBackupTemp.delete()) {
            throw new UnsupportedEncodingException("Failed to delete " + regBackupTemp.getAbsolutePath());
          }
          highscoreWritten = true;
        }
      }

      if (!descriptorJsonFile.delete()) {
        throw new UnsupportedEncodingException("Failed to delete " + descriptorJsonFile.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to create highscore archive file: " + e.getMessage(), e);
    }
    return highscoreWritten;
  }

  @Nullable
  public File writeBackupFile(@NonNull Game game, @NonNull File romBasedBackupFolder) {
    Optional<Highscore> hs = highscoreService.getHighscore(game, true, EventOrigin.USER_INITIATED);
    if (hs.isPresent()) {

      String filename = dateFormatter.format(new Date());
      filename = filename + "." + FILE_SUFFIX;

      if (!romBasedBackupFolder.exists() && !romBasedBackupFolder.mkdirs()) {
        LOG.error("Failed to create highscore backup folder {}", romBasedBackupFolder.getAbsolutePath());
      }

      File target = new File(romBasedBackupFolder, filename);
      target = FileUtils.uniqueFile(target);

      File tempFile = new File(target.getParentFile(), target.getName() + ".bak");
      LOG.info("Creating temporary archive file " + tempFile.getAbsolutePath());

      boolean written = writeHighscoreBackup(game, game.getEmulator(), hs.get(), romBasedBackupFolder, tempFile, filename);
      if (written && !tempFile.renameTo(target)) {
        LOG.error("Failed to rename highscore zip file " + tempFile.getAbsolutePath());
        return null;
      }

      if (!written && !tempFile.delete()) {
        LOG.error("No data written backup and deletion of temp data failed: " + tempFile.getAbsolutePath());
        return null;
      }

      if (written) {
        LOG.info("Written highscore backup " + target.getAbsolutePath());
        return target;
      }
      else {
        LOG.info("No highscore backup created, no matching source found for \"" + game.getRom() + "\"");
        return null;
      }
    }

    LOG.info("Skipped creating highscore backup of \"" + game.getGameDisplayName() + "\", no existing highscore data found.");
    return null;
  }

  public boolean restoreBackupFile(@NonNull GameEmulator gameEmulator, @NonNull File backupFile) {
    HighscoreBackup highscoreBackup = readBackupFile(backupFile);
    HighscoreType highscoreType = highscoreBackup.getHighscoreType();
    String rom = highscoreBackup.getRom();

    switch (highscoreType) {
      case NVRam: {
        File target = new File(gameEmulator.getNvramFolder(), highscoreBackup.getHighscoreFilename());
        return ZipUtil.writeZippedFile(backupFile, highscoreBackup.getHighscoreFilename(), target);
      }
      case EM: {
        File target = new File(gameEmulator.getUserFolder(), highscoreBackup.getHighscoreFilename());
        return ZipUtil.writeZippedFile(backupFile, highscoreBackup.getHighscoreFilename(), target);
      }
      case VPReg: {
        try {
          String json = ZipUtil.readZipFile(backupFile, VPREG_STG_JSON);
          VPReg vpReg = new VPReg(gameEmulator.getVPRegFile(), rom, null);
          vpReg.restore(json);
          LOG.info("Imported VPReg.stg data from " + backupFile.getParentFile().getAbsolutePath());
          return true;
        }
        catch (Exception e) {
          LOG.error("Failed to restore backup for VPReg.stg file and rom " + rom + ": " + e.getMessage(), e);
        }
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unkown highscore type to restore: " + highscoreType);
      }
    }
    return false;
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
