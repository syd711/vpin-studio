package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class VPSTest {

  private VPS newInstance(InputStream in) {
    VPS instance = new VPS();
    instance.loadTables(in);
    return instance;
  }

  private VPS newInstance()  {
    VPS instance = new VPS();
    try (InputStream in = VPSTest.class.getResourceAsStream("vpsdb.json.1")) {
      instance.loadTables(in);
    } catch(IOException ioe) {
    }
    return instance;
  }

  @Test
  public void testCorruption() throws IOException {
    VPS vps = new VPS();
    vps.reload();

    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      if(table == null || table.getB2sFiles() == null) {
        System.out.println(table);
        continue;
      }

      for (VpsBackglassFile b2sFile : table.getB2sFiles()) {
        if(b2sFile.getId() == null) {
          System.out.println(table.getName());
        }
      }
    }
  }


  @Test
  public void testDiff() throws IOException {
    try (InputStream in1 = VPSTest.class.getResourceAsStream("vpsdb.json.1");
        InputStream in2 = VPSTest.class.getResourceAsStream("vpsdb.json.2")) {
      VPS vpsOld = newInstance(in1);
      VPS vpsNew = newInstance(in2);
      assertNotNull(vpsNew);
      assertNotNull(vpsOld);
      assertFalse(vpsNew.getTables().isEmpty());
      assertFalse(vpsOld.getTables().isEmpty());

      List<VpsDiffer> diff = vpsNew.diff(vpsOld.getTables(), vpsNew.getTables());
      System.out.println(diff.size());
      assertFalse(diff.isEmpty());
//      for (VpsDiffer diffEntry : diff) {
//        System.out.println(diffEntry.getId() + ": " + diffEntry.toString());
//      }
    }
  }
  @Test
  public void testDiffById() throws IOException {
    try (InputStream in1 = VPSTest.class.getResourceAsStream("vpsdb.json.1");
        InputStream in2 = VPSTest.class.getResourceAsStream("vpsdb.json.2")) {
      VPS vpsOld = newInstance(in1);
      VPS vpsNew = newInstance(in2);

      VpsDiffer diff = vpsNew.diffById(vpsOld.getTables(), vpsNew.getTables(), "Gs1n4eIo");
      VPSChanges changes = diff.getChanges();
      assertFalse(changes.isEmpty());
    }
  }


  @Test
  public void testDiff3() throws Exception {
    try (InputStream in1 = VPSTest.class.getResourceAsStream("vps-diff-1.json");
         InputStream in2 = VPSTest.class.getResourceAsStream("vps-diff-2.json")) {
      VPS vpsOld = newInstance(in1);
      VPS vpsNew = newInstance(in2);

      List<VpsDiffer> diff = vpsNew.diff(vpsOld.getTables(), vpsNew.getTables());
      assertFalse(diff.isEmpty());
      assertEquals(2, diff.size());

      VpsDiffer diffTable1 = diff.get(1);
      VPSChanges tableChanges = diffTable1.getTableChanges();
      assertFalse(tableChanges.isEmpty());

      List<VPSChange> changes = tableChanges.getChanges();
      assertEquals(2, changes.size());
      assertTrue(changes.stream().filter(c -> c.getDiffType().equals(VpsDiffTypes.tableVersionUpdate)).findFirst().isPresent());
      assertEquals(1, changes.stream().filter(c -> c.getDiffType().equals(VpsDiffTypes.b2s)).collect(Collectors.toList()).size());
    }
  }

  @Test
  public void testUpdates() {
    VPS vps = new VPS();
    vps.reload();
    vps.update();
  }

  @Test
  public void testTableLoading() {
    VPS vps = newInstance();
    vps.reload();

    List<String> emus = new ArrayList<>();
    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      List<VpsTableVersion> tableFiles = table.getTableFiles();
      for (VpsTableVersion tableFile : tableFiles) {
        String tableFormat = tableFile.getTableFormat();
        if(!emus.contains(tableFormat)) {
          emus.add(tableFormat);
        }
      }
    }

    for (String emu : emus) {
      System.out.println(emu);
    }


  }

  @Test
  public void testTutorials() {
    VPS vps = newInstance();
    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      List<VpsTutorialUrls> tutorialFiles = table.getTutorialFiles();
      if (tutorialFiles != null && !tutorialFiles.isEmpty()) {
        for (VpsTutorialUrls tutorialFile : tutorialFiles) {
          assertNotEquals(0, tutorialFile.getUrls());
        }
      }
    }
  }

  @Test
  public void testSearch() {
    VPS vps = newInstance();
    List<VpsTable> tables = vps.getTables();
    assertFalse(tables.isEmpty());
    List<VpsTable> vpsTables = vps.find("2 in 1");
    for (VpsTable vpsTable : vpsTables) {
      System.out.println(vpsTable.getDisplayName());
    }

    assertEquals(1, vpsTables.size());
    assertEquals("2 in 1 (Bally 1964)", vpsTables.get(0).getDisplayName());

    vpsTables = vps.find("X-Files");
    assertTrue(!vpsTables.isEmpty());
  }

  @Test
  public void testSearch2() {
    VPS vps = newInstance();
    List<VpsTable> tables = vps.getTables();
    assertFalse(tables.isEmpty());
    List<VpsTable> vpsTables = vps.find("Red and");
    assertEquals(1, vpsTables.size());
  }

  @Test
  public void testAll() throws IOException {
    InputStream inputStream = VPSTest.class.getResourceAsStream("tablenames.txt");
    StringBuilder textBuilder = new StringBuilder();
    try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      int c = 0;
      while ((c = reader.read()) != -1) {
        textBuilder.append((char) c);
      }
    }

    String[] entries = textBuilder.toString().split("\n");
    assertNotEquals(0, entries.length);

    VPS vps = newInstance();
    for (String entry : entries) {
      List<VpsTable> vpsTables = vps.find(entry.trim());
      if (vpsTables.isEmpty()) {
        System.out.println(entry);
      }
      assertFalse(vpsTables.isEmpty(), "No entry found for \"" + entry + "\"");
    }
  }
}
