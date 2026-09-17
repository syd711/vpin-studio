package de.mephisto.vpin.server;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerUpdatePreProcessingTest {

  @ParameterizedTest
  @CsvSource({
      "ffmpeg.exe, true",
      "7z.dll, true",
      "downloader.vbs, true",
      "jvm/jinput-dx8_64.dll, true",
      "DOFTest/Readme.txt, true",
      "pinemhi/romfind.ini, true",
      "puppacktweaker/PupPackScreenTweaker.exe, true",
      "scoringdb.json, false",
      "maps/main.zip, false",
      "superhac/roms.json, false",
      "mame-gamelist.txt, false",
      "competition-badges/medal.png, false",
      "maintenance.mp4, false",
  })
  public void testIsWindowsOnly(String name, boolean expected) {
    ServerUpdateFileEntry entry = new ServerUpdateFileEntry(name, null, null, null, null);
    assertEquals(expected, ServerUpdatePreProcessing.isWindowsOnly(entry));
  }
}
