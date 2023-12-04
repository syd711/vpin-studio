package de.mephisto.vpin.commons.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AltSoundArchiveAnalyzer {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundArchiveAnalyzer.class);

  public static String analyze(@NonNull File file) {
    int audioCount = 0;
    boolean altSoundFound = false;
    try {
      byte[] buffer = new byte[1024];
      FileInputStream fileInputStream = new FileInputStream(file);
      ZipInputStream zis = new ZipInputStream(fileInputStream);
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null) {
        if (zipEntry.isDirectory()) {
          //ignore
        }
        else {
          String name = zipEntry.getName();
          if (name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".csv")) {
            audioCount++;
          }

          if (name.contains("altsound.csv") || name.contains("g-sound.csv")) {
            altSoundFound = true;
          }
        }
        zis.closeEntry();
        zipEntry = zis.getNextEntry();
      }
      fileInputStream.close();
      zis.closeEntry();
      zis.close();
    } catch (Exception e) {
      LOG.error("Unzipping of " + file.getAbsolutePath() + " failed: " + e.getMessage(), e);
      return "Unzipping of " + file.getAbsolutePath() + " failed: " + e.getMessage();
    }

    if (!altSoundFound) {
      return "The selected archive does not contain an \"altsound.csv\" file.";
    }

    if (audioCount == 0) {
      return "The selected archive does not contain any audio files.";
    }
    return null;
  }
}
