package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableDiff;
import org.junit.jupiter.api.Test;

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

    List<VpsTableDiff> diff = vpsNew.diff(vpsOld);
    System.out.println(diff.size());
    assertFalse(diff.isEmpty());
    for (VpsTableDiff diffEntry : diff) {
      System.out.println(diffEntry.getId() + ": " + diffEntry.toString());
    }
  }

  @Test
  public void testDiff2() {
    VPS vpsNew = VPS.loadInstance(VPSTest.class.getResourceAsStream("vpsdb.json.1"));
    VPS vpsOld = VPS.loadInstance(VPSTest.class.getResourceAsStream("vpsdb.json.2"));
    assertNotNull(vpsNew);
    assertNotNull(vpsOld);
    assertFalse(vpsNew.getTables().isEmpty());
    assertFalse(vpsOld.getTables().isEmpty());

    List<VpsTableDiff> diff = vpsNew.diff(vpsOld);
    System.out.println(diff.size());
    assertFalse(diff.isEmpty());
    for (VpsTableDiff diffEntry : diff) {
      if(diffEntry.getId().equals("DY3wUlic")) {
        System.out.println(diffEntry.getId() + ": " + diffEntry.toString());
      }
    }
  }

  @Test
  public void testTableLoading() {
    VPS vps = VPS.getInstance();
    List<VpsTable> tables = vps.getTables();
    assertFalse(tables.isEmpty());
    VpsTable tableById = vps.getTableById("43ma3WQK");
    assertNotNull(tables);
    assertNotNull(tableById);
  }

  @Test
  public void testSearch() {
    VPS vps = VPS.getInstance();
    List<VpsTable> tables = vps.getTables();
    assertFalse(tables.isEmpty());
    List<VpsTable> vpsTables = vps.find("Flintstones, The");
    assertEquals(1, vpsTables.size());

    vpsTables = vps.find("X-Files");
    assertEquals(1, vpsTables.size());
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
      if(vpsTables.isEmpty()) {
        System.out.println(entry);
      }
      assertFalse(vpsTables.isEmpty(), "No entry found for \"" + entry + "\"");
    }
  }
}
