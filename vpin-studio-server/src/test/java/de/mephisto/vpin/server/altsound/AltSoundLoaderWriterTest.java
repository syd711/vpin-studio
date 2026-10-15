package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.altsound.AltSoundEntry;
import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AltSoundLoaderWriterTest {

  private static final String CSV = """
      "ID","CHANNEL","DUCK","GAIN","LOOP","STOP","NAME","FNAME","GROUP","SHAKER","SERIAL","PRELOAD","STOPCMD"
      0x0002,0,100,85,100,0,"normal_prelaunch","0x0002-normal_prelaunch.ogg",1,,,0,
      0x0003,,40,120,0,1,"callout","sub/0x0003-callout.ogg",2,shake,ser,1,0x00
      """;

  @TempDir
  File folder;

  private AltSound load() {
    return AltSoundLoaderFactory.load(folder);
  }

  private void writeCsv(String content) throws Exception {
    FileUtils.writeStringToFile(new File(folder, "altsound.csv"), content, StandardCharsets.UTF_8);
  }

  @Test
  public void testLoad() throws Exception {
    writeCsv(CSV);
    FileUtils.writeStringToFile(new File(folder, "0x0002-normal_prelaunch.ogg"), "audio", StandardCharsets.UTF_8);

    AltSound altSound = load();
    assertThat(altSound.getFormat()).isEqualTo(AltSoundFormats.altsound);
    assertThat(altSound.getHeaders()).isEqualTo(AltSoundEntry.DEFAULT_HEADERS);
    assertThat(altSound.getEntries()).hasSize(2);

    AltSoundEntry first = altSound.getEntries().get(0);
    assertThat(first.getId()).isEqualTo("0x0002");
    assertThat(first.getChannel()).isEqualTo("0");
    assertThat(first.getDuck()).isEqualTo(100);
    assertThat(first.getGain()).isEqualTo(85);
    assertThat(first.getLoop()).isEqualTo(100);
    assertThat(first.getStop()).isEqualTo(0);
    assertThat(first.getName()).isEqualTo("normal_prelaunch");
    assertThat(first.getFilename()).isEqualTo("0x0002-normal_prelaunch.ogg");
    assertThat(first.getGroup()).isEqualTo(1);
    assertThat(first.getShaker()).isEmpty();
    assertThat(first.getPreload()).isEqualTo(0);
    assertThat(first.isExists()).isTrue();
    assertThat(first.getSize()).isEqualTo(5);

    AltSoundEntry second = altSound.getEntries().get(1);
    assertThat(second.getChannel()).isEmpty();
    assertThat(second.getGain()).isEqualTo(120);
    assertThat(second.getStop()).isEqualTo(1);
    assertThat(second.getFilename()).isEqualTo("sub/0x0003-callout.ogg");
    assertThat(second.getShaker()).isEqualTo("shake");
    assertThat(second.getSerial()).isEqualTo("ser");
    assertThat(second.getPreload()).isEqualTo(1);
    assertThat(second.getStopCmd()).isEqualTo("0x00");
    assertThat(second.isExists()).isFalse();
    assertThat(altSound.isMissingAudioFiles()).isTrue();
  }

  @Test
  public void testSaveRoundTrip() throws Exception {
    writeCsv(CSV);
    AltSound altSound = load();

    AltSoundEntry entry = altSound.getEntries().get(0);
    entry.setChannel("3");
    entry.setDuck(20);
    entry.setGain(150);
    entry.setLoop(0);
    entry.setStop(1);
    entry.setShaker("a,b");
    new AltSoundWriter(folder).write(altSound);

    AltSound reloaded = load();
    assertThat(reloaded.getHeaders()).isEqualTo(altSound.getHeaders());
    assertThat(reloaded.getEntries()).hasSize(2);
    AltSoundEntry saved = reloaded.getEntries().get(0);
    assertThat(saved.getId()).isEqualTo("0x0002");
    assertThat(saved.getChannel()).isEqualTo("3");
    assertThat(saved.getDuck()).isEqualTo(20);
    assertThat(saved.getGain()).isEqualTo(150);
    assertThat(saved.getLoop()).isEqualTo(0);
    assertThat(saved.getStop()).isEqualTo(1);
    assertThat(saved.getName()).isEqualTo("normal_prelaunch");
    assertThat(saved.getFilename()).isEqualTo("0x0002-normal_prelaunch.ogg");
    assertThat(saved.getGroup()).isEqualTo(1);
    assertThat(saved.getShaker()).isEqualTo("a,b");

    AltSoundEntry untouched = reloaded.getEntries().get(1);
    assertThat(untouched.getChannel()).isEmpty();
    assertThat(untouched.getStopCmd()).isEqualTo("0x00");
    assertThat(untouched.getPreload()).isEqualTo(1);

    // a second save without changes must produce the identical file
    String firstWrite = FileUtils.readFileToString(new File(folder, "altsound.csv"), StandardCharsets.UTF_8);
    new AltSoundWriter(folder).write(reloaded);
    assertThat(FileUtils.readFileToString(new File(folder, "altsound.csv"), StandardCharsets.UTF_8)).isEqualTo(firstWrite);
  }

  @Test
  public void testShortRowDoesNotDropFollowingRows() throws Exception {
    writeCsv("""
        ID,CHANNEL,DUCK,GAIN,LOOP,STOP,NAME,FNAME
        0x0001,0,100,85
        0x0002,1,100,85,0,0,"two","two.ogg"
        """);

    AltSound altSound = load();
    assertThat(altSound.getEntries()).hasSize(2);
    assertThat(altSound.getEntries().get(0).getId()).isEqualTo("0x0001");
    assertThat(altSound.getEntries().get(0).getGain()).isEqualTo(85);
    assertThat(altSound.getEntries().get(0).isExists()).isFalse();
    assertThat(altSound.getEntries().get(1).getFilename()).isEqualTo("two.ogg");
  }

  @Test
  public void testColumnsAreMappedByHeader() throws Exception {
    writeCsv("""
        "ID","CHANNEL","DUCK","GAIN","LOOP","STOP","NAME","FNAME","PRELOAD","GROUP"
        0x0002,0,100,85,100,0,"a","a.ogg",1,4
        """);

    AltSound altSound = load();
    AltSoundEntry entry = altSound.getEntries().get(0);
    assertThat(entry.getPreload()).isEqualTo(1);
    assertThat(entry.getGroup()).isEqualTo(4);

    new AltSoundWriter(folder).write(altSound);
    List<String> lines = FileUtils.readLines(new File(folder, "altsound.csv"), StandardCharsets.UTF_8);
    assertThat(lines.get(0)).isEqualTo("\"ID\",\"CHANNEL\",\"DUCK\",\"GAIN\",\"LOOP\",\"STOP\",\"NAME\",\"FNAME\",\"PRELOAD\",\"GROUP\"");
    assertThat(lines.get(1)).isEqualTo("\"0x0002\",0,100,85,100,0,\"a\",\"a.ogg\",1,4");
  }

  @Test
  public void testByteOrderMarkIsIgnored() throws Exception {
    writeCsv("﻿" + CSV);

    AltSound altSound = load();
    assertThat(altSound.getHeaders().get(0)).isEqualTo(AltSoundEntry.ID);
    assertThat(altSound.getEntries().get(0).getId()).isEqualTo("0x0002");
  }
}
