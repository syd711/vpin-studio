package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.backup.types.TableBackupAdapter;
import de.mephisto.vpin.server.backup.types.TableBackupAdapterFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ArchiveServiceTest extends AbstractVPinServerTest {

  @Autowired
  private TableBackupAdapterFactory tableBackupAdapterFactory;

  @Autowired
  private GameService gameService;

  @Autowired
  private SystemService systemService;

  @Test
  public void testExport() throws IOException {
//    test(VPinServerTest.TEST_GAME_FILENAME);
//    test("Hayburners (WIlliams 1951).vpx");
//    exportTest("Attack from Mars 2.0.1.vpx");
//    test("The Addams Family.vpx");
    exportTest("Jaws in English.vpx");
//    test("Stranger Things.vpx");
  }

  private void exportTest(String name) {
    Game game = gameService.getGameByFilename(name);
    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(game);
    ArchiveDescriptor backup = adapter.createBackup();
    assertTrue(new File(systemService.getVpaArchiveFolder(), backup.getFilename()).exists());
  }
}
