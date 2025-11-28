package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.lang.invoke.MethodHandles;

public class AltSoundLoaderFactory {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Nullable
  public static AltSound create(@NonNull File altSoundFolder, int emulatorId) {
    File ini = new File(altSoundFolder, "altsound.ini");
    File gSoundCsv = new File(altSoundFolder, "g-sound.csv");
    File altSoundCsv = new File(altSoundFolder, "altsound.csv");

    AltSound altSound = new AltSound();
    altSound.setName(altSoundFolder.getParentFile().getName());
    altSound.setFolder(altSoundFolder.getAbsolutePath());

    if (ini.exists() || gSoundCsv.exists() || altSoundCsv.exists()) {
      altSound.setEmulatorId(emulatorId);
      altSound.setName(altSoundFolder.getParentFile().getName());
      return altSound;
    }
    return null;
  }

  @NonNull
  public static AltSound load(@NonNull AltSound altSound) {
    String folder = altSound.getFolder();
    File altSoundFolder = new File(folder);
    return load(altSoundFolder, altSound.getEmulatorId());
  }

  @NonNull
  public static AltSound load(@NonNull File altSoundFolder, int emulatorId) {
    try {
      File ini = new File(altSoundFolder, "altsound.ini");
      File gSoundCsv = new File(altSoundFolder, "g-sound.csv");
      File altSoundCsv = new File(altSoundFolder, "altsound.csv");

      if (ini.exists()) {
        INIConfiguration iniConfiguration = new INIConfiguration();
        iniConfiguration.setCommentLeadingCharsUsedInInput(";");
        iniConfiguration.setSeparatorUsedInOutput("=");
        iniConfiguration.setSeparatorUsedInInput("=");

        FileReader fileReader = new FileReader(ini);
        try {
          iniConfiguration.read(fileReader);
        }
        finally {
          fileReader.close();
        }

        SubnodeConfiguration formatNode = iniConfiguration.getSection("format");
        if (formatNode != null) {
          String format = formatNode.getString("format");
          if (format.equals(AltSoundFormats.gsound) && gSoundCsv.exists()) {
            AltSound altSound = new AltSound2Loader(iniConfiguration, gSoundCsv).load();
            altSound.setEmulatorId(emulatorId);
            altSound.setFolder(altSoundFolder.getAbsolutePath());
            return altSound;
          }
          else if (altSoundCsv.exists()) {
            AltSound altSound = new AltSoundLoader(altSoundCsv).load();
            altSound.setEmulatorId(emulatorId);
            altSound.setFolder(altSoundFolder.getAbsolutePath());
            return altSound;
          }
        }
      }
      else if (gSoundCsv.exists()) {
        //init file does not exists, but g-sound.csv, which means that the table has not been started yet.
        AltSound altSound = new AltSound();
        altSound.setEmulatorId(emulatorId);
        altSound.setFolder(altSoundFolder.getAbsolutePath());
        altSound.setName(gSoundCsv.getParentFile().getName());
        altSound.setFormat(AltSoundFormats.gsound);
        altSound.setFilesize(-1);
        return altSound;
      }
      else if (altSoundCsv.exists()) {
        return new AltSoundLoader(altSoundCsv).load();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to load altsound: " + e.getMessage(), e);
    }

    LOG.warn("Failed to resolve altsound for folder " + altSoundFolder.getAbsolutePath());
    return new AltSound();
  }

}
