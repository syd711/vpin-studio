package de.mephisto.vpin.server.vr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class VRTableIniSwapperTest {

  @TempDir
  Path tempDir;

  private File iniFile;
  private File vrIniFile;
  private File parkedFile;
  private File journalFile;
  private VRTableIniSwapper swapper;

  @BeforeEach
  void setUp() {
    iniFile = tempDir.resolve("Table (Manufacturer 1990).ini").toFile();
    vrIniFile = tempDir.resolve("Table (Manufacturer 1990).vr.ini").toFile();
    parkedFile = tempDir.resolve("Table (Manufacturer 1990).ini.desktop").toFile();
    journalFile = tempDir.resolve("journal.txt").toFile();
    swapper = new VRTableIniSwapper(journalFile);
  }

  @Test
  void naming() {
    assertEquals(vrIniFile, VRTableIniSwapper.getVRIniFile(iniFile));
    assertEquals(parkedFile, VRTableIniSwapper.getParkedDesktopIniFile(iniFile));
  }

  @Test
  void swapIn_withDesktopIni_parksDesktopIniAndActivatesVrIni() throws IOException {
    write(iniFile, "desktop");
    write(vrIniFile, "vr");

    assertTrue(swapper.swapIn(iniFile));

    assertEquals("vr", read(iniFile));
    assertEquals("desktop", read(parkedFile));
    assertFalse(vrIniFile.exists());
    assertTrue(swapper.isSwapped(iniFile));
  }

  @Test
  void swapIn_withoutVrIni_doesNothing() throws IOException {
    write(iniFile, "desktop");

    assertFalse(swapper.swapIn(iniFile));

    assertEquals("desktop", read(iniFile));
    assertFalse(parkedFile.exists());
    assertFalse(swapper.isSwapped(iniFile));
    assertFalse(journalFile.exists());
  }

  @Test
  void swapIn_twice_keepsVrIniActive() throws IOException {
    write(iniFile, "desktop");
    write(vrIniFile, "vr");

    assertTrue(swapper.swapIn(iniFile));
    assertTrue(swapper.swapIn(iniFile));

    assertEquals("vr", read(iniFile));
    assertEquals("desktop", read(parkedFile));
  }

  @Test
  void swapIn_withExistingParkedFile_refusesSwap() throws IOException {
    write(iniFile, "desktop");
    write(vrIniFile, "vr");
    write(parkedFile, "stale");

    assertFalse(swapper.swapIn(iniFile));

    assertEquals("desktop", read(iniFile));
    assertEquals("vr", read(vrIniFile));
    assertFalse(swapper.isSwapped(iniFile));
  }

  @Test
  void swapOut_restoresDesktopIniAndKeepsVrChanges() throws IOException {
    write(iniFile, "desktop");
    write(vrIniFile, "vr");
    swapper.swapIn(iniFile);

    //VPX writes the table options on exit
    write(iniFile, "vr changed");

    assertTrue(swapper.swapOut(iniFile));

    assertEquals("desktop", read(iniFile));
    assertEquals("vr changed", read(vrIniFile));
    assertFalse(parkedFile.exists());
    assertFalse(swapper.isSwapped(iniFile));
    assertFalse(journalFile.exists());
  }

  @Test
  void swapInAndOut_withoutDesktopIni() throws IOException {
    write(vrIniFile, "vr");

    assertTrue(swapper.swapIn(iniFile));
    assertEquals("vr", read(iniFile));
    assertFalse(parkedFile.exists());

    assertTrue(swapper.swapOut(iniFile));
    assertFalse(iniFile.exists());
    assertEquals("vr", read(vrIniFile));
  }

  @Test
  void swapOut_withoutSwap_doesNothing() throws IOException {
    write(iniFile, "desktop");
    write(vrIniFile, "vr");

    assertFalse(swapper.swapOut(iniFile));

    assertEquals("desktop", read(iniFile));
    assertEquals("vr", read(vrIniFile));
  }

  @Test
  void swapOut_twice_secondCallDoesNothing() throws IOException {
    write(iniFile, "desktop");
    write(vrIniFile, "vr");
    swapper.swapIn(iniFile);

    assertTrue(swapper.swapOut(iniFile));
    assertFalse(swapper.swapOut(iniFile));

    assertEquals("desktop", read(iniFile));
    assertEquals("vr", read(vrIniFile));
  }

  @Test
  void restoreAll_usesJournalOfPreviousInstance() throws IOException {
    write(iniFile, "desktop");
    write(vrIniFile, "vr");
    swapper.swapIn(iniFile);

    //e.g. server restart after a crash
    VRTableIniSwapper restarted = new VRTableIniSwapper(journalFile);
    assertEquals(1, restarted.getActiveSwaps().size());
    restarted.restoreAll();

    assertEquals("desktop", read(iniFile));
    assertEquals("vr", read(vrIniFile));
    assertFalse(parkedFile.exists());
    assertTrue(restarted.getActiveSwaps().isEmpty());
  }

  @Test
  void restoreAll_afterInterruptedSwapIn() throws IOException {
    //desktop ini parked, VR ini not moved yet
    write(parkedFile, "desktop");
    write(vrIniFile, "vr");
    Files.writeString(journalFile.toPath(), iniFile.getAbsolutePath() + System.lineSeparator());

    swapper.restoreAll();

    assertEquals("desktop", read(iniFile));
    assertEquals("vr", read(vrIniFile));
    assertFalse(parkedFile.exists());
    assertFalse(swapper.isSwapped(iniFile));
  }

  @Test
  void restoreAll_multipleTables() throws IOException {
    File otherIni = tempDir.resolve("Other.ini").toFile();
    File otherVrIni = tempDir.resolve("Other.vr.ini").toFile();
    write(iniFile, "desktop");
    write(vrIniFile, "vr");
    write(otherIni, "other desktop");
    write(otherVrIni, "other vr");

    swapper.swapIn(iniFile);
    swapper.swapIn(otherIni);
    assertEquals(2, swapper.getActiveSwaps().size());

    swapper.restoreAll();

    assertEquals("desktop", read(iniFile));
    assertEquals("other desktop", read(otherIni));
    assertEquals("vr", read(vrIniFile));
    assertEquals("other vr", read(otherVrIni));
    assertTrue(swapper.getActiveSwaps().isEmpty());
  }

  private static void write(File file, String content) throws IOException {
    Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
  }

  private static String read(File file) throws IOException {
    return Files.readString(file.toPath(), StandardCharsets.UTF_8);
  }
}
