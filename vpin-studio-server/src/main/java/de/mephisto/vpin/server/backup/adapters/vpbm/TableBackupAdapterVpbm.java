package de.mephisto.vpin.server.backup.adapters.vpbm;

import de.mephisto.vpin.restclient.*;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.ArchiveSourceAdapter;
import de.mephisto.vpin.server.backup.ArchiveUtil;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.GameMediaItem;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

public class TableBackupAdapterVpbm implements TableBackupAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableBackupAdapterVpbm.class);

  private final VpbmService vpbmService;
  private final Game game;
  private final ArchiveSourceAdapter archiveSourceAdapter;
  private final TableDetails tableDetails;

  private double progress;
  private String status;

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
  public double getProgress() {
    return progress;
  }

  @Override
  public String getStatus() {
    return status;
  }

  public JobExecutionResult execute() {
    return createBackup();
  }

  @Override
  public JobExecutionResult createBackup() {
    LOG.info("Starting VPBM backup of " + game.getGameFileName());

    status = "Creating backup of \"" + game.getGameDisplayName() + "\"";

    JobExecutionResult result = JobExecutionResultFactory.create(vpbmService.backup(game.getId()));

    File archiveFile = new File(this.archiveSourceAdapter.getArchiveSource().getLocation(), tableDetails.getGameName() + ".vpinzip");

    progress = 90;
    status = "Generating Metadata";

    ArchiveDescriptor archiveDescriptor = new ArchiveDescriptor();
    archiveDescriptor.setSource(archiveSourceAdapter.getArchiveSource());

    File wheelIcon = null;
    GameMediaItem gameMediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (gameMediaItem != null) {
      wheelIcon = gameMediaItem.getFile();
    }

    ArchivePackageInfo packageInfo = VpbmArchiveUtil.generatePackageInfo(archiveFile, wheelIcon);

    archiveDescriptor.setCreatedAt(new Date());
    archiveDescriptor.setTableDetails(tableDetails);
    archiveDescriptor.setFilename(tableDetails.getGameName() + ".vpinzip");
    archiveDescriptor.setPackageInfo(packageInfo);
    archiveDescriptor.setSize(archiveFile.length());

    ArchiveUtil.exportArchiveDescriptor(archiveDescriptor);

    progress = 100;

    return result;
  }
}
