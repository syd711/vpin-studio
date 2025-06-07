package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.archiving.adapters.vpa.TableBackupAdapterVpa;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TableBackupVpaServiceTest extends AbstractVPinServerTest {

  @BeforeAll
  public void init() {
    setupSystem();
  }

  @Test
  public void testEmTable() {
    importExport(EM_TABLE_NAME);
  }

  @Test
  public void testVPRegTable() {
    importExport(VPREG_TABLE_NAME);
  }

  @Test
  public void testNVRamTable() {
    importExport(NVRAM_TABLE_NAME);
  }

  private void importExport(String tableName) {
    try {
      Game game = gameService.getGameByFilename(1, tableName);
      TableBackupAdapterVpa adapter = (TableBackupAdapterVpa) 
        tableBackupAdapterFactory.createAdapter(archiveService.getDefaultArchiveSourceAdapter(), game);
      adapter.simulateBackup();
    }
    catch (IOException ioe) {
      fail(ioe);
    }
  }

}
