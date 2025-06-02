package de.mephisto.vpin.server.scripteval;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.util.VPXFileScanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Opens script.vbs and evaluate each independent scenario
 */
public class ScriptTest {

  @Test
  public void checkExotic() {
    ScanResult result = doScan("Exotic GameName");
    assertEquals("rab_320", result.getGameName());
    assertEquals("ex\"'otic 'and' com'\"plex", result.getTableName());
  }

  @Test
  public void checkStrangerThings() {
    ScanResult result = doScan("Stranger Things (original 2017)");
    assertEquals("stranger_things", result.getRom());
    assertEquals("StrangerThings", result.getTableName());

    assertEquals("UltraDMD", result.getDMDType());
    assertEquals(null, result.getDMDGameName());
    assertEquals("StrangerThings.UltraDMD", result.getDMDProjectFolder());
  }

  @Test
  public void checkStarWars() {
    ScanResult result = doScan("Star Wars (Data East 1992)");
    assertEquals("stwr_107", result.getGameName());
    assertNull(result.getTableName());
    assertTrue(result.isVrRoomSupport());
    assertTrue(result.isVrRoomDisabled());
  }

  @Test
  public void checkAmericaMostHaunted() {
    ScanResult result = doScan("America's Most Haunted");
    assertNull(result.getGameName());
    assertNull(result.getTableName());
    assertEquals("UltraDMD", result.getDMDType());
    assertEquals(null, result.getDMDGameName());
    assertEquals("America's Most Haunted.UltraDMD", result.getDMDProjectFolder());
  }

  @Test
  public void checkGoonies() {
    ScanResult result = doScan("The Goonies (Original 2021)");
    assertEquals("TheGoonies", result.getGameName());
    assertEquals("The_Goonies", result.getTableName());
    assertEquals("FlexDMD", result.getDMDType());
    assertEquals("goonies", result.getDMDGameName());
    assertEquals("TheGooniesDMD", result.getDMDProjectFolder());
  }

  @Test
  public void checkCyberrace() {
    ScanResult result = doScan("Cyber Race (Original 2023)");
    assertEquals("cyber_race", result.getGameName());
    assertEquals(null, result.getTableName());
    assertEquals("FlexDMD", result.getDMDType());
    assertEquals("Cyber Race (Original 2023)", result.getDMDGameName());
    assertEquals("CyberRaceDMD", result.getDMDProjectFolder());
  }

  @Test
  public void checkPinkFloyd() {
    ScanResult result = doScan("Pink Floyd The Wall (Original 2020)");
    assertEquals("Pink_Floyd", result.getRom());
    assertEquals("Pink Floyd", result.getTableName());
    assertEquals("UltraDMD", result.getDMDType());
    assertEquals(null, result.getDMDGameName());
    assertEquals("SEPF.UltraDMD", result.getDMDProjectFolder());
  }



  @Test
  public void testStripComments() {
    assertArrayEquals(new String[] { "abc" }, VPXFileScanner.stripComments("abc"));
    assertArrayEquals(new String[] { "abc \"def\" ghi" }, VPXFileScanner.stripComments("abc \"def\" ghi"));

    assertArrayEquals(new String[] {}, VPXFileScanner.stripComments("' abc def"));
    assertArrayEquals(new String[]{ "abc" }, VPXFileScanner.stripComments("abc 'comments"));
    assertArrayEquals(new String[]{ "abc" }, VPXFileScanner.stripComments("abc    'comments with \"quotes\""));

    assertArrayEquals(new String[]{ "\"ex\"\"'otic\" & \" 'and' \" & \"com'\"\"plex\"" }, 
      VPXFileScanner.stripComments("\"ex\"\"'otic\" & \" 'and' \" & \"com'\"\"plex\" ' with a \"comment\" at the end\"   ' with a \"comment\""));

    assertArrayEquals(new String[] { "abc", "def" }, VPXFileScanner.stripComments("abc : def"));
    assertArrayEquals(new String[] { "abc", "def" }, VPXFileScanner.stripComments("abc : def ' with comment"));
  }

  //--------------------------------------------------------

  private ScanResult doScan(String scenario) {
    try (InputStream in = getClass().getResourceAsStream("script.vbs")) {
      String script = IOUtils.toString(in, StandardCharsets.UTF_8);
      script = script.replaceAll("\r\n", "\n");
      List<String> lines = Arrays.asList(script.split("\n"));
      List<String> scriptLines = null;
      int l = 0;
      while (l < lines.size()) {
        String line = lines.get(l);
        if (line.startsWith("'===========")) {
          // as we already found our scenario, this is the detection of a new one, so interrupt 
          if (scriptLines != null) {
            break;
          }
          // else check the scenario
          line = lines.get(++l);
          if (StringUtils.containsIgnoreCase(line, scenario)) {
            scriptLines = new ArrayList<>();
          }
        }
        else if (scriptLines != null) {
          scriptLines.add(line);
        }
        l++;
      }
      // now run the check if we found our scenario
      if (scriptLines != null) {
        ScanResult result = new ScanResult();
        Collections.reverse(scriptLines);
        VPXFileScanner.scanLines(new File("c:/fake/tables/", scenario + ".vpx"), result, scriptLines);
        return result;
      }
    }
    catch (IOException ioe) {
      fail(ioe.getMessage());
    }
    fail("Scenario not found in script.vbs: " + scenario);
    return null;
  }
}
