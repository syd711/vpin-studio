package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapterFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ArchiveServiceClientTest extends AbstractVPinServerTest {

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
//    Game game = gameService.getGameByFilename(TEST_FILE);
//    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(archiveService.getDefaultArchiveSourceAdapter(), game);
//    JobExecutionResult msg = adapter.createBackup();
//    assertTrue(msg.getError() == null);
  }

  @Test
  public void testImport() {
//    String name = FilenameUtils.getBaseName(TEST_FILE);
//    ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(-2, name + ".vpinzip");
//    TableInstallerAdapter adapter = tableInstallerAdapterFactory.createAdapter(archiveDescriptor);
//    JobExecutionResult result = adapter.installTable();
//    assertNull(result.getError());
  }
}
