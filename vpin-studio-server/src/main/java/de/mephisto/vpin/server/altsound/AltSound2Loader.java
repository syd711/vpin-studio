package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class AltSound2Loader {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final INIConfiguration iniConfiguration;
  private final File gSoundCsv;

  AltSound2Loader(@NonNull INIConfiguration iniConfiguration, @NonNull File gSoundCsv) {
    this.iniConfiguration = iniConfiguration;
    this.gSoundCsv = gSoundCsv;
  }

  @NonNull
  public AltSound load() {
    AltSound altSound = new AltSound();
    altSound.setCsvFile(gSoundCsv);
    altSound.setName(gSoundCsv.getParentFile().getName());
    altSound.setFormat(AltSoundFormats.gsound);
    altSound.setModificationDate(new Date(gSoundCsv.lastModified()));

    SubnodeConfiguration systemSection = iniConfiguration.getSection("system");
    altSound.setRecordSoundCmds(systemSection.getBoolean("record_sound_cmds"));
    altSound.setRomVolumeControl(systemSection.getBoolean("rom_volume_ctrl"));

    if (systemSection.containsKey("cmd_skip_count")) {
      altSound.setCommandSkipCount(systemSection.getInt("cmd_skip_count"));
    }

    altSound.setMusic(loadGroup(iniConfiguration, "music"));
    altSound.setCallout(loadGroup(iniConfiguration, "callout"));
    altSound.setSfx(loadGroup(iniConfiguration, "sfx"));
    altSound.setSolo(loadGroup(iniConfiguration, "solo"));
    altSound.setOverlay(loadGroup(iniConfiguration, "overlay"));

    altSound.setMusicDuckingProfiles(loadGroupProfile(iniConfiguration, "music_ducking_profiles"));
    altSound.setCalloutDuckingProfiles(loadGroupProfile(iniConfiguration, "callout_ducking_profiles"));
    altSound.setSfxDuckingProfiles(loadGroupProfile(iniConfiguration, "sfx_ducking_profiles"));
    altSound.setSoloDuckingProfiles(loadGroupProfile(iniConfiguration, "solo_ducking_profiles"));
    altSound.setOverlayDuckingProfiles(loadGroupProfile(iniConfiguration, "overlay_ducking_profiles"));


    long size = gSoundCsv.length();
    FileReader in = null;
    Map<String, String> audioFiles = new HashMap<>();
    try {
      in = new FileReader(gSoundCsv);
      Iterable<CSVRecord> records = CSVFormat.RFC4180
          .withIgnoreEmptyLines(true)
          .withQuoteMode(QuoteMode.NON_NUMERIC)
          .withQuote('"')
          .withTrim().parse(in);
      Iterator<CSVRecord> iterator = records.iterator();
      CSVRecord header = iterator.next();
      altSound.setHeaders(header.toList());

      while (iterator.hasNext()) {
        CSVRecord record = iterator.next();
        String s = record.get(4);
        if (s == null) {
          LOG.warn("Skipped invalid ALT Sound entry {}", record.toList());
          continue;
        }
        File audioFile = new File(gSoundCsv.getParentFile(), s.replaceAll("\"", ""));

        AltSoundEntry entry = new AltSoundEntry();
        entry.setId(record.get(0));
        entry.setChannel(record.isSet(1) ? record.get(1) : "");
        entry.setGain(record.isSet(2) ? getInt(record.get(2)) : 0);
        entry.setDuck(record.isSet(3) ? getInt(record.get(3)) : 0);
        entry.setFilename(record.isSet(4) ? record.get(4) : "");

        if (audioFile.exists()) {
          entry.setSize(audioFile.length());
        }


        File soundFile = new File(gSoundCsv.getParentFile(), entry.getFilename());
        if (soundFile.exists()) {
          audioFiles.put(entry.getFilename(), entry.getFilename());
          size += soundFile.length();
        }
        else {
          altSound.setMissingAudioFiles(true);
        }

        altSound.getEntries().add(entry);
      }

      in.close();

      altSound.setFilesize(size);
      altSound.setFiles(audioFiles.size());
    }
    catch (Exception e) {
      LOG.error("Failed to read g-sound CSV " + gSoundCsv.getAbsolutePath() + ": " + e.getMessage(), e);
    }

    return altSound;
  }

  @NonNull
  private List<AltSound2DuckingProfile> loadGroupProfile(INIConfiguration iniConfiguration, String profileName) {
    List<AltSound2DuckingProfile> profiles = new ArrayList<>();

    if (!iniConfiguration.getSections().contains(profileName)) {
      return profiles;
    }

    AltSound2SampleType sampleType = AltSound2SampleType.valueOf(profileName.substring(0, profileName.indexOf("_")));

    SubnodeConfiguration section = iniConfiguration.getSection(profileName);
    int index = 1;
    String key = "ducking_profile" + index;
    while (section.containsKey(key)) {
      String profileValue = section.getString(key);
      AltSound2DuckingProfile profile = new AltSound2DuckingProfile();
      profile.setId(index);
      profile.setType(sampleType);
      profile.setValues(toDuckingProfileValues(profileValue));

      profiles.add(profile);

      index++;
      key = "ducking_profile" + index;
    }

    return profiles;
  }

  private List<AltSoundDuckingProfileValue> toDuckingProfileValues(String profileValue) {
    List<AltSoundDuckingProfileValue> values = new ArrayList<>();
    try {
      String[] split = profileValue.split(",");
      for (String s : split) {
        if (StringUtils.isEmpty(s.trim())) {
          continue;
        }

        String[] entries = s.split(":");
        AltSoundDuckingProfileValue value = new AltSoundDuckingProfileValue();
        value.setSampleType(AltSound2SampleType.valueOf(entries[0].trim()));
        value.setVolume(Integer.parseInt(entries[1]));

        values.add(value);
      }
    }
    catch (Exception e) {
      LOG.error("Error parsing ducking profile value \"" + profileValue + "\": " + e.getMessage(), e);
    }
    return values;
  }

  @Nullable
  private AltSound2Group loadGroup(INIConfiguration iniConfiguration, String name) {
    if (!iniConfiguration.getSections().contains(name)) {
      return null;
    }

    SubnodeConfiguration section = iniConfiguration.getSection(name);
    AltSound2Group group = new AltSound2Group();
    try {
      group.setName(AltSound2SampleType.valueOf(name));
    }
    catch (Exception e) {
      //ignore
    }

    if (section.containsKey("group_vol")) {
      group.setGroupVol(section.getInt("group_vol"));
    }
    if (section.containsKey("ducks")) {
      group.setDucks(toSampleTypes(section.getString("ducks")));
    }
    if (section.containsKey("stops")) {
      group.setStops(toSampleTypes(section.getString("stops")));
    }
    if (section.containsKey("pauses")) {
      group.setPauses(toSampleTypes(section.getString("pauses")));
    }
    return group;
  }

  private List<AltSound2SampleType> toSampleTypes(String value) {
    List<AltSound2SampleType> altSound2SampleTypes = new ArrayList<>();
    try {
      String[] split = value.split(",");
      for (String s : split) {
        if (StringUtils.isEmpty(s)) {
          continue;
        }
        AltSound2SampleType altSound2SampleType = AltSound2SampleType.valueOf(s.trim().toLowerCase());
        altSound2SampleTypes.add(altSound2SampleType);
      }
    }
    catch (Exception e) {
      LOG.error("Error parsing sample types for \"" + value + "\": " + e.getMessage(), e);
    }

    return altSound2SampleTypes;
  }

  private int getInt(String value) {
    if (StringUtils.isEmpty(value)) {
      return 0;
    }

    return Integer.parseInt(value.trim());
  }
}
