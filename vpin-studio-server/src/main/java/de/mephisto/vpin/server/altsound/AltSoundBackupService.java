package de.mephisto.vpin.server.altsound;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

@Service
public class AltSoundBackupService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void synchronizeBackup(@NonNull File altSoundFolder) {
    File csvFile = new File(altSoundFolder, "altsound.csv");
    checkBackup(csvFile);

    File gSoundCsvFile = new File(altSoundFolder, "g-sound.csv");
    checkBackup(gSoundCsvFile);

    File gSoundIniFile = new File(altSoundFolder, "altsound.ini");
    checkBackup(gSoundIniFile);
  }

  private void checkBackup(File file) {
    File backup = new File(file.getParentFile(), file.getName() + ".bak");
    if (file.exists() && !backup.exists()) {
      try {
        org.apache.commons.io.FileUtils.copyFile(file, backup);
      } catch (IOException e) {
        LOG.error("Error creating alt sound backup file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
      }
    }
  }

  public void restore(@NonNull File altSoundFolder) {
    File csvFile = new File(altSoundFolder, "altsound.csv");
    restoreBackup(csvFile);

    File gSoundCsvFile = new File(altSoundFolder, "g-sound.csv");
    restoreBackup(gSoundCsvFile);

    File gSoundIniFile = new File(altSoundFolder, "altsound.ini");
    restoreBackup(gSoundIniFile);
  }

  private void restoreBackup(File file) {
    File backup = new File(file.getParentFile(), file.getName() + ".bak");
    if (backup.exists()) {
      try {
        if(file.exists()) {
          file.delete();
        }
        org.apache.commons.io.FileUtils.copyFile(backup, file);
      } catch (IOException e) {
        LOG.error("Error restoring backup file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
      }
    }
  }
}
