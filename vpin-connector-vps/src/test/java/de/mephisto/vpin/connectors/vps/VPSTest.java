package de.mephisto.vpin.connectors.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VPSTest {

  @Test
  public void testTableLoading() {
    VPS vps = VPS.getInstance();
    List<VpsTable> tables = vps.getTables();
    assertFalse(tables.isEmpty());
    VpsTable tableById = vps.getTableById("43ma3WQK");
    assertNotNull(tables);
    assertNotNull(tableById);
  }
}
