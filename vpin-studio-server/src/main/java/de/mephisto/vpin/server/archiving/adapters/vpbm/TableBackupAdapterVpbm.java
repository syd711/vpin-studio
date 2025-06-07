package de.mephisto.vpin.server.archiving.adapters.vpbm;

import de.mephisto.vpin.restclient.archiving.ArchivePackageInfo;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.archiving.ArchiveSourceAdapter;
import de.mephisto.vpin.server.archiving.ArchiveUtil;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

public class TableBackupAdapterVpbm implements TableBackupAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(TableBackupAdapterVpbm.class);

  private final VpbmService vpbmService;
  private final Game game;
  private final ArchiveSourceAdapter archiveSourceAdapter;
  private final TableDetails tableDetails;

  public TableBackupAdapterVpbm(@NonNull VpbmService vpbmService,
                                @NonNull ArchiveSourceAdapter archiveSourceAdapter,
                                @NonNull Game game,
                                @NonNull TableDetails tableDetails) {
    this.vpbmService = vpbmService;
    this.game = game;
    this.archiveSourceAdapter = archiveSourceAdapter;
    this.tableDetails = tableDetails;
  }

  @Override
  public void createBackup(JobDescriptor result) {
    Thread.currentThread().setName("Backup Thread of " + game.getGameDisplayName());
    LOG.info("Starting VPBM backup of " + game.getGameDisplayName());

    result.setStatus("Creating backup of \"" + game.getGameDisplayName() + "\"");

    String backup = vpbmService.backup(game.getId());
    result.setError(backup);

    File archiveFile = new File(this.archiveSourceAdapter.getArchiveSource().getLocation(), tableDetails.getGameName() + ".vpinzip");

    result.setProgress(0.9);
    result.setStatus("Generating Metadata");

    ArchiveDescriptor archiveDescriptor = new ArchiveDescriptor();
    archiveDescriptor.setSource(archiveSourceAdapter.getArchiveSource());

    File wheelIcon = vpbmService.getWheelImage(game);
    ArchivePackageInfo packageInfo = VpbmArchiveUtil.generatePackageInfo(archiveFile, wheelIcon);

    archiveDescriptor.setCreatedAt(new Date());
    archiveDescriptor.setTableDetails(tableDetails);
    archiveDescriptor.setFilename(tableDetails.getGameName() + ".vpinzip");
    archiveDescriptor.setPackageInfo(packageInfo);
    archiveDescriptor.setSize(archiveFile.length());

    ArchiveUtil.exportArchiveDescriptor(archiveDescriptor);

    result.setProgress(1);
  }
}
