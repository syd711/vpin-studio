package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;

public class AltSoundLoaderFactory {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundLoaderFactory.class);
  private final File altSoundFolder;

  public AltSoundLoaderFactory(@NonNull File altSoundFolder) {
    this.altSoundFolder = altSoundFolder;
  }

  @NonNull
  public AltSound load() {
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
        } finally {
          fileReader.close();
        }

        SubnodeConfiguration formatNode = iniConfiguration.getSection("format");
        if (formatNode != null) {
          String format = formatNode.getString("format");
          if (format.equals(AltSoundFormats.gsound) && gSoundCsv.exists()) {
            return new AltSound2Loader(iniConfiguration, gSoundCsv).load();
          }
          else if (altSoundCsv.exists()) {
            return new AltSoundLoader(altSoundCsv).load();
          }
        }
      }
      else if (altSoundCsv.exists()) {
        return new AltSoundLoader(altSoundCsv).load();
      }
    } catch (Exception e) {
      LOG.error("Failed to load altsound: " + e.getMessage(), e);
    }

    LOG.warn("Failed to resolve altsound for folder " + altSoundFolder.getAbsolutePath());
    return new AltSound();
  }

}
