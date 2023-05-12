package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapterFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ArchiveServiceTest extends AbstractVPinServerTest {

  @Autowired
  private TableBackupAdapterFactory tableBackupAdapterFactory;

  @Autowired
  private TableInstallerAdapterFactory tableInstallerAdapterFactory;

  @Autowired
  private GameService gameService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private ArchiveService archiveService;

  @Test
  public void testExport() {
//    test(VPinServerTest.TEST_GAME_FILENAME);
//    test("Hayburners (WIlliams 1951).vpx");
//    exportTest("Attack from Mars 2.0.1.vpx");
//    test("The Addams Family.vpx");
    exportTest("Jaws in English.vpx");
//    test("Stranger Things.vpx");
  }

  @Test
  public void testImport() {
    ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(-1, "Jaws in English.vpa");
    TableInstallerAdapter adapter = tableInstallerAdapterFactory.createAdapter(archiveDescriptor);
    Game game = adapter.installTable();
    assertNotNull(game);
  }

  private void exportTest(String name) {
    Game game = gameService.getGameByFilename(name);
    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(game);
    ArchiveDescriptor backup = adapter.createBackup();
    assertTrue(new File(systemService.getVpaArchiveFolder(), backup.getFilename()).exists());
  }
}
