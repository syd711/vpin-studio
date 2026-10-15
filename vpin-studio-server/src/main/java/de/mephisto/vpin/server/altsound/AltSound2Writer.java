package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.*;
import org.jspecify.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AltSound2Writer {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final File altSoundFolder;

  public AltSound2Writer(@NonNull File altSoundFolder) {
    this.altSoundFolder = altSoundFolder;
  }

  /**
   * Returns the numeric id of a "ducking_profile[n]" key or -1 if the key is not a ducking profile.
   */
  static int getDuckingProfileId(String key) {
    String prefix = "ducking_profile";
    if (key != null && key.startsWith(prefix)) {
      try {
        return Integer.parseInt(key.substring(prefix.length()).trim());
      }
      catch (NumberFormatException e) {
        //ignore
      }
    }
    return -1;
  }

  public void write(@NonNull AltSound altSound) throws Exception {
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

      SubnodeConfiguration systemNode = iniConfiguration.getSection("system");
      systemNode.setProperty("record_sound_cmds", altSound.isRecordSoundCmds() ? "1" : "0");
      systemNode.setProperty("rom_volume_ctrl", altSound.isRomVolumeControl() ? "1" : "0");
      systemNode.setProperty("cmd_skip_count", altSound.getCommandSkipCount());

      SubnodeConfiguration formatNode = iniConfiguration.getSection("format");
      if (formatNode != null) {
        String format = formatNode.getString("format");
        if (AltSoundFormats.gsound.equals(format) && gSoundCsv.exists()) {
          saveSampleTypeNode(iniConfiguration, AltSound2SampleType.music, altSound.getMusic());
          saveSampleTypeNode(iniConfiguration, AltSound2SampleType.callout, altSound.getCallout());
          saveSampleTypeNode(iniConfiguration, AltSound2SampleType.sfx, altSound.getSfx());
          saveSampleTypeNode(iniConfiguration, AltSound2SampleType.solo, altSound.getSolo());
          saveSampleTypeNode(iniConfiguration, AltSound2SampleType.overlay, altSound.getOverlay());

          saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.callout, altSound.getCalloutDuckingProfiles());
          saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.sfx, altSound.getSfxDuckingProfiles());
          saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.overlay, altSound.getOverlayDuckingProfiles());
          //music and solos cannot duck other samples
//            saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.solo, altSound.getSoloDuckingProfiles());
//            saveDuckingProfileNode(iniConfiguration, AltSound2SampleType.music, altSound.getMusicDuckingProfiles());

          //write CSV file
          org.apache.commons.io.FileUtils.writeStringToFile(gSoundCsv, altSound.toGSoundCSV(), StandardCharsets.UTF_8);

          try (FileWriter fileWriter = new FileWriter(ini)) {
            iniConfiguration.write(fileWriter);
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
  }

  private void saveDuckingProfileNode(INIConfiguration iniConfiguration, AltSound2SampleType sampleType, List<AltSound2DuckingProfile> profiles) throws IOException {
    String sectionName = sampleType.name() + "_ducking_profiles";
    SubnodeConfiguration sectionNode = iniConfiguration.getSection(sectionName);

    if (profiles.isEmpty()) {
      sectionNode.clear();
    }
    else {
      for (AltSound2DuckingProfile profile : profiles) {
        sectionNode.setProperty("ducking_profile" + profile.getId(), profile.getValues().stream().map(AltSoundDuckingProfileValue::toString).collect(Collectors.joining(", ")));
      }

      List<String> keys = new ArrayList<>();
      sectionNode.getKeys().forEachRemaining(keys::add);
      for (String key : keys) {
        int id = getDuckingProfileId(key);
        if (id > 0 && profiles.stream().noneMatch(p -> p.getId() == id)) {
          sectionNode.clearProperty(key);
        }
      }
    }
  }

  private void saveSampleTypeNode(INIConfiguration iniConfiguration, AltSound2SampleType sampleType, AltSound2Group group) {
    if (group == null) {
      return;
    }
    SubnodeConfiguration sampleTypeNode = iniConfiguration.getSection(sampleType.name());
    sampleTypeNode.setProperty("group_vol", group.getGroupVol());
    if (!group.getPauses().isEmpty()) {
      sampleTypeNode.setProperty("pauses", group.getPauses().stream().map(Enum::name).collect(Collectors.joining(", ")));
    }
    else {
      sampleTypeNode.clearProperty("pauses");
    }

    if (!group.getStops().isEmpty()) {
      sampleTypeNode.setProperty("stops", group.getStops().stream().map(Enum::name).collect(Collectors.joining(", ")));
    }
    else {
      sampleTypeNode.clearProperty("stops");
    }


    if (!(sampleType.equals(AltSound2SampleType.solo) || sampleType.equals(AltSound2SampleType.music))) {
      if (!group.getDucks().isEmpty()) {
        sampleTypeNode.setProperty("ducks", group.getDucks().stream().map(Enum::name).collect(Collectors.joining(", ")));
      }
      else {
        sampleTypeNode.clearProperty("ducks");
      }
    }
  }
}
