package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class AltSoundBackupService {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundBackupService.class);

  public File getOrCreateBackup(@NonNull Game game) {
    File backup = new File(csvFile.getParentFile(), csvFile.getName() + ".bak");
    if (!backup.exists()) {
      try {
        org.apache.commons.io.FileUtils.copyFile(csvFile, backup);
      } catch (IOException e) {
        LOG.error("Error creating CSV backup: " + e.getMessage(), e);
      }
    }
    return backup;
  }

  public AltSound restore(@Nullable Game game) {
    try {
      if (csvFile != null && csvFile.exists()) {
        File backup = getOrCreateBackup(csvFile);
        if (backup.exists()) {
          org.apache.commons.io.FileUtils.copyFile(backup, csvFile);
        }
        else {
          LOG.error("Failed to restore ALT sound backup, the backup file " + backup.getAbsolutePath() + " does not exists.");
        }
      }
    } catch (IOException e) {
      LOG.error("Error restoring CSV backup: " + e.getMessage(), e);
    }
    return null;
  }
}
