package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.JobExecutionResult;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapterFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ArchiveServiceClientTest extends AbstractVPinServerTest {

  //  private final static String TEST_FILE = "Batman 66.vpx";
  private final static String TEST_FILE = "Hayburners (WIlliams 1951).vpx";

  @Autowired
  private TableBackupAdapterFactory tableBackupAdapterFactory;

  @Autowired
  private TableInstallerAdapterFactory tableInstallerAdapterFactory;

  @Autowired
  private GameService gameService;

  @Autowired
  private ArchiveService archiveService;

  @Test
  public void testExport() {
    Game game = gameService.getGameByFilename(TEST_FILE);
    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(archiveService.getDefaultArchiveSourceAdapter(), game);
    ArchiveDescriptor backup = adapter.createBackup();
    assertTrue(new File(backup.getSource().getLocation(), backup.getFilename()).exists());
  }

  @Test
  public void testImport() {
    String name = FilenameUtils.getBaseName(TEST_FILE);
    ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(-2, name + ".vpinzip");
    TableInstallerAdapter adapter = tableInstallerAdapterFactory.createAdapter(archiveDescriptor);
    JobExecutionResult result = adapter.installTable();
    assertNull(result.getError());
  }
}
