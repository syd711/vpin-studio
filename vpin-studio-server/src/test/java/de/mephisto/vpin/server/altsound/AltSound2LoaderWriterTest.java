package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSound2DuckingProfile;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AltSound2LoaderWriterTest {

  private static final File FIXTURE = new File("../testsystem/vPinball/VisualPinball/VPinMAME/altsound/cftbl_l4");

  @TempDir
  File folder;

  @BeforeEach
  public void setUp() throws Exception {
    FileUtils.copyFile(new File(FIXTURE, "altsound.ini"), new File(folder, "altsound.ini"));
    FileUtils.copyFile(new File(FIXTURE, "g-sound.csv"), new File(folder, "g-sound.csv"));
  }

  @Test
  public void testExistingAudioFilesAreDetected() throws Exception {
    AltSound initial = AltSoundLoaderFactory.load(folder);
    AltSoundEntry first = initial.getEntries().get(0);
    FileUtils.writeStringToFile(new File(folder, first.getFilename()), "audio", StandardCharsets.UTF_8);

    AltSound altSound = AltSoundLoaderFactory.load(folder);
    assertThat(altSound.getFormat()).isEqualTo(AltSoundFormats.gsound);

    AltSoundEntry existing = altSound.getEntries().get(0);
    assertThat(existing.isExists()).isTrue();
    assertThat(existing.getSize()).isEqualTo(5);

    assertThat(altSound.getEntries().get(1).isExists()).isFalse();
    assertThat(altSound.isMissingAudioFiles()).isTrue();
  }

  @Test
  public void testSaveRoundTrip() throws Exception {
    AltSound altSound = AltSoundLoaderFactory.load(folder);
    int entryCount = altSound.getEntries().size();
    assertThat(entryCount).isGreaterThan(0);

    AltSoundEntry entry = altSound.getEntries().get(0);
    entry.setGain(12);
    entry.setChannel("SFX");
    entry.setDuck(1);
    altSound.setRomVolumeControl(!altSound.isRomVolumeControl());
    altSound.setCommandSkipCount(7);

    new AltSound2Writer(folder).write(altSound);

    AltSound reloaded = AltSoundLoaderFactory.load(folder);
    assertThat(reloaded.getEntries()).hasSize(entryCount);
    assertThat(reloaded.getHeaders()).isEqualTo(altSound.getHeaders());
    AltSoundEntry reloadedEntry = reloaded.getEntries().get(0);
    assertThat(reloadedEntry.getId()).isEqualTo(entry.getId());
    assertThat(reloadedEntry.getGain()).isEqualTo(12);
    assertThat(reloadedEntry.getChannel()).isEqualTo("SFX");
    assertThat(reloadedEntry.getDuck()).isEqualTo(1);
    assertThat(reloadedEntry.getFilename()).isEqualTo(entry.getFilename());
    assertThat(reloaded.isRomVolumeControl()).isEqualTo(altSound.isRomVolumeControl());
    assertThat(reloaded.getCommandSkipCount()).isEqualTo(7);
    assertThat(reloaded.getOverlayDuckingProfiles()).hasSameSizeAs(altSound.getOverlayDuckingProfiles());
    assertThat(reloaded.getCalloutDuckingProfiles()).hasSameSizeAs(altSound.getCalloutDuckingProfiles());
    assertThat(reloaded.getSfxDuckingProfiles()).hasSameSizeAs(altSound.getSfxDuckingProfiles());
  }

  @Test
  public void testDuckingProfilesWithGapsAndTwoDigitIds() throws Exception {
    AltSound altSound = AltSoundLoaderFactory.load(folder);
    List<AltSound2DuckingProfile> profiles = altSound.getOverlayDuckingProfiles();
    assertThat(profiles).hasSizeGreaterThanOrEqualTo(2);

    // simulate deleting profile 1 and adding a profile with a two-digit id
    AltSound2DuckingProfile profile10 = new AltSound2DuckingProfile();
    profile10.setId(10);
    profile10.setType(profiles.get(0).getType());
    profile10.setValues(profiles.get(0).getValues());
    profiles.remove(0);
    profiles.add(profile10);
    List<Integer> expectedIds = profiles.stream().map(AltSound2DuckingProfile::getId).sorted().toList();

    new AltSound2Writer(folder).write(altSound);

    AltSound reloaded = AltSoundLoaderFactory.load(folder);
    assertThat(reloaded.getOverlayDuckingProfiles()).extracting(AltSound2DuckingProfile::getId).containsExactlyElementsOf(expectedIds);
  }

  @Test
  public void testMissingIniSectionsDoNotBreakSaving() throws Exception {
    File ini = new File(folder, "altsound.ini");
    String content = FileUtils.readFileToString(ini, StandardCharsets.UTF_8);
    // remove the [solo] section
    content = content.replaceAll("(?s)\\[solo\\].*?(?=\\n\\[|$)", "");
    FileUtils.writeStringToFile(ini, content, StandardCharsets.UTF_8);

    AltSound altSound = AltSoundLoaderFactory.load(folder);
    assertThat(altSound.getSolo()).isNull();
    altSound.getEntries().get(0).setGain(33);

    new AltSound2Writer(folder).write(altSound);

    assertThat(AltSoundLoaderFactory.load(folder).getEntries().get(0).getGain()).isEqualTo(33);
  }
}
