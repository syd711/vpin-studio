package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.*;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VPSTest {

  @Test
  public void testDiff() {
    VPS vpsNew = VPS.loadInstance(VPSTest.class.getResourceAsStream("vpsdb.json.1"));
    VPS vpsOld = VPS.loadInstance(VPSTest.class.getResourceAsStream("vpsdb.json.2"));
    assertNotNull(vpsNew);
    assertNotNull(vpsOld);
    assertFalse(vpsNew.getTables().isEmpty());
    assertFalse(vpsOld.getTables().isEmpty());

    List<VpsDiffer> diff = vpsNew.diff(vpsOld.getTables(), vpsNew.getTables());
    System.out.println(diff.size());
    assertFalse(diff.isEmpty());
    for (VpsDiffer diffEntry : diff) {
      System.out.println(diffEntry.getId() + ": " + diffEntry.toString());
    }
  }


//  @Test
//  public void testDiff3() throws Exception {
//    VPS vpsNew = VPS.loadInstance(new FileInputStream(new File("C:\\Users\\matth\\AppData\\Roaming\\JetBrains\\IdeaIC2023.2\\scratches\\vpsdb.json")));
//    VPS vpsOld = VPS.loadInstance(new FileInputStream(new File("E:\\Development\\workspace\\vpin-studio\\resources/vpsdb.json")));
//
//    assertNotNull(vpsNew);
//    assertNotNull(vpsOld);
//    assertFalse(vpsNew.getTables().isEmpty());
//    assertFalse(vpsOld.getTables().isEmpty());
//
//    List<VpsTableDiff> diff = vpsNew.diff(vpsOld, Arrays.asList("IItQ56T8b1"));
//    System.out.println(diff.size());
//    assertFalse(diff.isEmpty());
//    for (VpsTableDiff diffEntry : diff) {
//      if (!diffEntry.getDifferences().contains(VpsDiffTypes.tutorial)) {
//        continue;
//      }
//
//      System.out.println(diffEntry.getId() + ": " + diffEntry.toString());
//    }
//  }

  @Test
  public void testUpdates() {
    VPS vps = VPS.getInstance();
    vps.addChangeListener(new VpsSheetChangedListener() {
      @Override
      public void vpsSheetChanged(List<VpsDiffer> diff) {
        System.out.println(diff.size());
      }
    });
    vps.update();
  }

  @Test
  public void testTableLoading() {
    VPS vps = VPS.getInstance();
    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
      if(table.getFeatures() == null) {
        System.out.println(table.getDisplayName());
      }
    }
  }

  @Test
  public void testTutorials() {
    VPS vps = VPS.getInstance();
    List<VpsTable> tables = vps.getTables();
    for (VpsTable table : tables) {
//      List<VpsTutorialUrls> tutorialFiles = table.getTutorialFiles();
//      if (tutorialFiles != null && !tutorialFiles.isEmpty()) {
//        System.out.println(table.getName());
//      }

      List<VpsAuthoredUrls> pupPackFiles = table.getPupPackFiles();
      if (pupPackFiles != null && !pupPackFiles.isEmpty()) {
        System.out.println(table.getName());
      }
    }
  }

  @Test
  public void testSearch() {
    VPS vps = VPS.getInstance();
    List<VpsTable> tables = vps.getTables();
    assertFalse(tables.isEmpty());
    List<VpsTable> vpsTables = vps.find("2 in 1");
    for (VpsTable vpsTable : vpsTables) {
      System.out.println(vpsTable.getDisplayName());
    }

    assertEquals(1, vpsTables.size());
    assertEquals("2 in 1 | Bally (1964)", vpsTables.get(0).getDisplayName());

    vpsTables = vps.find("X-Files");
    assertTrue(!vpsTables.isEmpty());
  }

  @Test
  public void testSearch2() {
    VPS vps = VPS.getInstance();
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

    VPS vps = VPS.getInstance();
    for (String entry : entries) {
      List<VpsTable> vpsTables = vps.find(entry.trim());
      if (vpsTables.isEmpty()) {
        System.out.println(entry);
      }
      assertFalse(vpsTables.isEmpty(), "No entry found for \"" + entry + "\"");
    }
  }
}
