package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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
            saveSampleTypeNode(iniConfiguration, AltSound2SampleType.music, altSound.getMusic());
            saveSampleTypeNode(iniConfiguration, AltSound2SampleType.callout, altSound.getCallout());
            saveSampleTypeNode(iniConfiguration, AltSound2SampleType.sfx, altSound.getSfx());
            saveSampleTypeNode(iniConfiguration, AltSound2SampleType.solo, altSound.getSolo());
            saveSampleTypeNode(iniConfiguration, AltSound2SampleType.overlay, altSound.getOverlay());

            saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.music, altSound.getMusicDuckingProfiles());
            saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.callout, altSound.getCalloutDuckingProfiles());
            saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.sfx, altSound.getSfxDuckingProfiles());
            saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.solo, altSound.getSoloDuckingProfiles());
            saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.overlay, altSound.getOverlayDuckingProfiles());

            //write CSV file
            org.apache.commons.io.FileUtils.writeStringToFile(gSoundCsv, altSound.toGSoundCSV(), StandardCharsets.UTF_8);

            FileWriter fileWriter = new FileWriter(ini);
            try {
              iniConfiguration.write(fileWriter);
            } finally {
              fileWriter.close();
            }
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

  private void saveDuckingProfileNode(INIConfiguration iniConfiguration, AltSound2SampleType sampleType, List<AltSound2DuckingProfile> profiles) {
    SubnodeConfiguration sampleTypeNode = iniConfiguration.getSection(sampleType.name() + "_ducking_profiles");
    for (AltSound2DuckingProfile profile : profiles) {
      sampleTypeNode.setProperty("ducking_profile" + profile.getId(), profile.getValues().stream().map(AltSoundDuckingProfileValue::toString).collect(Collectors.joining(", ")));
    }
  }

  private void saveSampleTypeNode(INIConfiguration iniConfiguration, AltSound2SampleType sampleType, AltSound2Group group) {
    SubnodeConfiguration sampleTypeNode = iniConfiguration.getSection(sampleType.name());
    sampleTypeNode.setProperty("ducks", group.getDucks().stream().map(Enum::name).collect(Collectors.joining(", ")));
    sampleTypeNode.setProperty("pauses", group.getPauses().stream().map(Enum::name).collect(Collectors.joining(", ")));
    sampleTypeNode.setProperty("stops", group.getStops().stream().map(Enum::name).collect(Collectors.joining(", ")));
    sampleTypeNode.setProperty("group_vol", group.getGroupVol());
  }
}
