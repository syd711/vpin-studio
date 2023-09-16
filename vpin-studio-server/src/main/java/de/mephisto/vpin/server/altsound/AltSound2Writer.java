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
import java.nio.charset.StandardCharsets;

public class AltSound2Writer {
  private final static Logger LOG = LoggerFactory.getLogger(AltSound2Writer.class);

  private final File altSoundFolder;

  public AltSound2Writer(@NonNull File altSoundFolder) {
    this.altSoundFolder = altSoundFolder;
  }

  public void write(@NonNull AltSound altSound) {
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
        iniConfiguration.read(fileReader);

        SubnodeConfiguration formatNode = iniConfiguration.getSection("format");
        if (formatNode != null) {
          String format = formatNode.getString("format");
          if (format.equals(AltSoundFormats.gsound) && gSoundCsv.exists()) {
//            SubnodeConfiguration formatNode = iniConfiguration.getSection("format");


            org.apache.commons.io.FileUtils.writeStringToFile(altSoundCsv, altSound.toGSoundCSV(), StandardCharsets.UTF_8);
          }
          else if (altSoundCsv.exists()) {
           new AltSoundWriter(altSoundFolder).write(altSound);
          }
        }
      }
      else if (altSoundCsv.exists()) {
        new AltSoundWriter(altSoundFolder).write(altSound);
      }
    } catch (Exception e) {
      LOG.error("Error altsound2: " + e.getMessage(), e);
    }
  }
}
