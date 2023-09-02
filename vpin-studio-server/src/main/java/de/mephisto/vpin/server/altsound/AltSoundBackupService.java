package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class AltSoundBackupService {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundBackupService.class);

  public File getOrCreateBackup(@NonNull Game game) {
    File altSoundFolder = game.getAltSoundFolder();
    File csvFile = new File(altSoundFolder, "altsound.csv");
    if (!csvFile.exists()) {
      csvFile = new File(altSoundFolder, "g-sound.csv");
    }
    File backup = new File(altSoundFolder, csvFile.getName() + ".bak");
    if (!backup.exists()) {
      try {
        org.apache.commons.io.FileUtils.copyFile(csvFile, backup);
      } catch (IOException e) {
        LOG.error("Error creating CSV backup: " + e.getMessage(), e);
      }
    }
    return backup;
  }

  public AltSound restore(@NonNull Game game) {
    try {
      File altSoundFolder = game.getAltSoundFolder();
      File csvFile = new File(altSoundFolder, "altsound.csv");
      File backupFile = new File(altSoundFolder, "altsound.csv.bak");
      if (!backupFile.exists()) {
        csvFile = new File(altSoundFolder, "g-sound.csv");
        backupFile = new File(altSoundFolder, "g-sound.csv.bak");
      }

      if (backupFile.exists()) {
        csvFile.delete();
        org.apache.commons.io.FileUtils.copyFile(backupFile, csvFile);
      }
      else {
        LOG.error("Failed to restore ALT sound backup, the backup file " + backupFile.getAbsolutePath() + " does not exists.");
      }
    } catch (IOException e) {
      LOG.error("Error restoring CSV backup: " + e.getMessage(), e);
    }
    return null;
  }
}
