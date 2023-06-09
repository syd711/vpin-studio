package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class VPSTest {

  @Test
  public void testTableLoading() {
    VPS vps = new VPS();
    List<VpsTable> tables = vps.getTables();
    assertFalse(tables.isEmpty());
    System.out.println(tables.size() + " tables loaded.");

    List<VpsTable> vpsTables = vps.find("007");
    System.out.println(vpsTables);
    assertFalse(vpsTables.isEmpty());
  }
}
