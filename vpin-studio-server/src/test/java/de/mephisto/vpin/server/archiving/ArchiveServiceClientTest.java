package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.commons.utils.ZipUtil;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaArchiveSource;
import de.mephisto.vpin.server.games.Game;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ArchiveServiceClientTest extends AbstractVPinServerTest {

  @BeforeAll
  public void init() {
    setupSystem();
  }

  @Test
  public void testImportExport() {
//    importExport(EM_TABLE_NAME);
//    importExport(VPREG_TABLE_NAME);
//    importExport(NVRAM_TABLE_NAME);
  }

  private void importExport(String tableName) {
    File vpaFile = new File(archiveService.getArchivesFolder(), FilenameUtils.getBaseName(tableName) + ".vpa");
    if(vpaFile.exists()) {
      vpaFile.delete();
    }

    exportTable(tableName);
    importTable(tableName);
  }

  private void importTable(String tableName) {
    String name = FilenameUtils.getBaseName(tableName);
    ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(VpaArchiveSource.DEFAULT_ARCHIVE_SOURCE_ID, name + ".vpa");
    assertNotNull(archiveDescriptor);

    TableInstallerAdapter adapter = tableInstallerAdapterFactory.createAdapter(archiveDescriptor, buildGameEmulator());
    JobDescriptor result = new JobDescriptor(JobType.ARCHIVE_INSTALL, UUID.randomUUID().toString());

    adapter.installTable(result);
    assertNull(result.getError());
  }

  private void exportTable(String tableName) {
    File vpaFile = new File(archiveService.getArchivesFolder(), FilenameUtils.getBaseName(tableName) + ".vpa");
    Game game = gameService.getGameByFilename(1, tableName);
    // change the GameEmulator aand use a test one
    game.setEmulator(buildGameEmulator());
    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(archiveService.getDefaultArchiveSourceAdapter(), game);
    JobDescriptor result = new JobDescriptor(JobType.ARCHIVE_INSTALL, UUID.randomUUID().toString());
    adapter.createBackup(result);
    assertNull(result.getError());

    assertTrue(vpaFile.exists());
    assertTrue(ZipUtil.contains(vpaFile, "package-info.json") != null);
    assertTrue(ZipUtil.contains(vpaFile, "table-details.json") != null);
    assertTrue(ZipUtil.contains(vpaFile, FilenameUtils.getBaseName(tableName) + ".directb2s") != null);
    assertTrue(ZipUtil.contains(vpaFile, tableName) != null);

    archiveService.invalidateCache();
  }
}
