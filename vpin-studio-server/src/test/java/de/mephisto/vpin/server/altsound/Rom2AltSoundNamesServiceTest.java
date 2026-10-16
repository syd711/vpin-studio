package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundNames;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Rom2AltSoundNamesServiceTest {

  private static AltSoundEntry entry(String id, String filename) {
    AltSoundEntry entry = new AltSoundEntry();
    entry.setId(id);
    entry.setFilename(filename);
    return entry;
  }

  @Test
  void toNameStripsIdPrefixAndExtension() {
    assertThat(Rom2AltSoundNamesService.toName(entry("0x0002", "0x0002-Martian_laugh.ogg"), "afm_113b")).isEqualTo("Martian laugh");
    assertThat(Rom2AltSoundNamesService.toName(entry("0x00a5", "music/0x00A5 Ship destroyed, multiball.ogg"), "afm_113b")).isEqualTo("Ship destroyed, multiball");
  }

  @Test
  void toNameSkipsGenericNames() {
    assertThat(Rom2AltSoundNamesService.toName(entry("0x0001", "0x0001-afm_113b.wav"), "afm_113b")).isNull();
    assertThat(Rom2AltSoundNamesService.toName(entry("0x0001", "0x0001-afm_113b-loop.wav"), "afm_113b")).isNull();
    assertThat(Rom2AltSoundNamesService.toName(entry("0x0001", "0x0001.wav"), "afm_113b")).isNull();
  }

  @Test
  void toNameStripsTwinsAndLoops() {
    assertThat(Rom2AltSoundNamesService.toName(entry("0x0095", "0x0095-sameas-000148-success_in_mission.ogg"), "afm_113b")).isEqualTo("success in mission");
    assertThat(Rom2AltSoundNamesService.toName(entry("0x0003", "0x0003-main_theme-loop.wav"), "afm_113b")).isEqualTo("main theme");
  }

  @Test
  void toCSV() throws Exception {
    Rom2AltSoundNames names = new Rom2AltSoundNames();
    names.setRom("afm_113b");
    names.setSoundRomId("9c0e8b43f153cdab638a6c1c226d42a29f6c524c");
    names.setVersion("0.3.0");
    names.getEntries().add(new Rom2AltSoundNames.Entry("0x0002", "Martian laugh"));
    names.getEntries().add(new Rom2AltSoundNames.Entry("0x00a5", "Ship destroyed, multiball"));

    assertThat(Rom2AltSoundNamesService.toCSV(names)).isEqualTo("rom2altsound names\r\n"
        + "rom: afm_113b\r\n"
        + "sound_rom_id: 9c0e8b43f153cdab638a6c1c226d42a29f6c524c\r\n"
        + "rom2altsound: 0.3.0\r\n"
        + "ID,NAME\r\n"
        + "0x0002,Martian laugh\r\n"
        + "0x00a5,\"Ship destroyed, multiball\"\r\n");
  }
}
