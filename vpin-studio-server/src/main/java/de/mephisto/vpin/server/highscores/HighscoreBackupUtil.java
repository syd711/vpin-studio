package de.mephisto.vpin.server.highscores;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.HighscoreBackup;
import de.mephisto.vpin.restclient.HighscoreType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ZipUtil;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

public class HighscoreBackupUtil {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(HighscoreBackupUtil.class);

  public static final String FILE_SUFFIX = "hsbckp";
  public static final String DESCRIPTOR_JSON = "descriptor.json";
  public static final String VPREG_STG_JSON = "vpreg-stg.json";

  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

  public static HighscoreBackup readBackupFile(@NonNull File archiveFile) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      String json = ZipUtil.readZipFile(archiveFile, DESCRIPTOR_JSON);

      return objectMapper.readValue(json, HighscoreBackup.class);
    } catch (Exception e) {
      LOG.error("Failed to read " + archiveFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }

  private static File writeDescriptorJson(Game game, File folder, Highscore highscore, String filename) throws IOException {
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
    if (descriptorFile.exists()) {
      descriptorFile.delete();
    }

    String json = objectMapper.writeValueAsString(backup);
    Files.write(descriptorFile.toPath(), json.getBytes());
    LOG.error("Written temporary highscore export descriptor file " + descriptorFile.getAbsolutePath());
    return descriptorFile;
  }

  private static boolean writeHighscoreBackup(Game game, Highscore highscore, SystemService systemService, File romBackupFolder, File tempFile, String filename) {
    FileOutputStream fos = null;
    ZipOutputStream zipOut = null;

    boolean highscoreWritten = false;

    try {
      File descriptorJsonFile = writeDescriptorJson(game, romBackupFolder, highscore, filename);

      fos = new FileOutputStream(tempFile);
      zipOut = new ZipOutputStream(fos);

      ZipUtil.zipFile(descriptorJsonFile, descriptorJsonFile.getName(), zipOut);

      //store highscore
      //zip EM file
      if (game.getEMHighscoreFile() != null && game.getEMHighscoreFile().exists()) {
        ZipUtil.zipFile(game.getEMHighscoreFile(), game.getEMHighscoreFile().getName(), zipOut);
        highscoreWritten = true;
      }

      //zip nvram file
      if (game.getNvRamFile().exists()) {
        ZipUtil.zipFile(game.getNvRamFile(), game.getNvRamFile().getName(), zipOut);
        highscoreWritten = true;
      }

      //write VPReg.stg data
      if (HighscoreType.VPReg.equals(game.getHighscoreType())) {
        File vprRegFile = systemService.getVPRegFile();
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
    } catch (Exception e) {
      LOG.error("Failed to create highscore archive file: " + e.getMessage(), e);
    } finally {
      try {
        if (zipOut != null) {
          zipOut.close();
        }
      } catch (IOException e) {
        //ignore
      }

      try {
        if (fos != null) {
          fos.close();
        }
      } catch (IOException e) {
        //ignore
      }
    }
    return highscoreWritten;
  }

  public static boolean writeBackupFile(@NonNull HighscoreService highscoreService, @NonNull SystemService systemService, @NonNull Game game, @NonNull File romBackupFolder) {
    Optional<Highscore> hs = highscoreService.getOrCreateHighscore(game);
    if (hs.isPresent()) {
      String filename = dateFormatter.format(new Date());
      filename = filename + "." + FILE_SUFFIX;

      File target = new File(romBackupFolder, filename);
      target = FileUtils.uniqueFile(target);

      File tempFile = new File(target.getParentFile(), target.getName() + ".bak");
      LOG.info("Creating temporary archive file " + tempFile.getAbsolutePath());

      boolean written = writeHighscoreBackup(game, hs.get(), systemService, romBackupFolder, tempFile, filename);
      if (written && !tempFile.renameTo(target)) {
        LOG.error("Failed to rename highscore zip file " + tempFile.getAbsolutePath());
        return false;
      }

      if (!written && !tempFile.delete()) {
        LOG.error("No data written backup and deletion of temp data failed: " + tempFile.getAbsolutePath());
        return false;
      }

      if(written) {
        LOG.info("Written highscore backup " + target.getAbsolutePath());
      }
      else {
        LOG.info("No highscore backup created, no matching source found for \"" + game.getRom() + "\"");
      }

      return written;
    }
    else {
      LOG.info("Skipped creating highscore backup of \"" + game.getGameDisplayName() + "\", no existing highscore data found.");
    }
    return false;
  }

  public static boolean restoreBackupFile(@NonNull SystemService systemService, @NonNull File
      backupRomFolder, @NonNull String filename) {
    File archiveFile = new File(backupRomFolder, filename);
    HighscoreBackup highscoreBackup = readBackupFile(archiveFile);
    HighscoreType highscoreType = highscoreBackup.getHighscoreType();
    String rom = highscoreBackup.getRom();

    switch (highscoreType) {
      case NVRam: {
        File target = new File(systemService.getNvramFolder(), highscoreBackup.getHighscoreFilename());
        ZipUtil.writeZippedFile(archiveFile, highscoreBackup.getHighscoreFilename(), target);
        return true;
      }
      case EM: {
        File target = new File(systemService.getVisualPinballUserFolder(), highscoreBackup.getHighscoreFilename());
        ZipUtil.writeZippedFile(archiveFile, highscoreBackup.getHighscoreFilename(), target);
        return true;
      }
      case VPReg: {
        try {
          String json = ZipUtil.readZipFile(archiveFile, VPREG_STG_JSON);
          VPReg vpReg = new VPReg(systemService.getVPRegFile(), rom, null);
          vpReg.restore(json);
          LOG.info("Imported VPReg.stg data from " + backupRomFolder.getAbsolutePath());
          return true;
        } catch (Exception e) {
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

}
