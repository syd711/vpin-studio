package de.mephisto.vpin.server.ipdb;


import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IpdbDatabaseTest {

  @Test
  public void testUpdate() throws IOException {
    // as the update is not called, no need for settings
    IpdbDatabase db = new IpdbDatabase(null);
    db.reload();
    List<IpdbTable> tables = db.getTables();
    Assertions.assertTrue(tables.size() > 6600);
  }

}
