package de.mephisto.vpin.server.backup.adapters.vpinzip;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.ArchivePackageInfo;
import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.ArchiveSource;
import de.mephisto.vpin.server.backup.ArchiveSourceAdapter;
import de.mephisto.vpin.server.backup.ArchiveUtil;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TableBackupAdapterVpinzip implements TableBackupAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableBackupAdapterVpinzip.class);

  private final SystemService systemService;
  private final Game game;
  private final ArchiveSourceAdapter archiveSourceAdapter;
  private final TableDetails tableDetails;

  private double progress;
  private String status;

  public TableBackupAdapterVpinzip(@NonNull SystemService systemService,
                                   @NonNull ArchiveSourceAdapter archiveSourceAdapter,
                                   @NonNull Game game,
                                   @NonNull TableDetails tableDetails) {
    this.systemService = systemService;
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

  public boolean execute() {
    return createBackup() != null;
  }

  @Override
  public ArchiveDescriptor createBackup() {
    LOG.info("Starting vpinzip backup of " + game.getGameFileName());

    status = "Creating backup of \"" + game.getGameDisplayName() + "\"";

    try {
      File dir = new File(systemService.RESOURCES, VpinzipArchiveSource.FOLDER_NAME);
      List<String> commands = Arrays.asList("vPinBackupManager.exe", "-b", String.valueOf(game.getId()));
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.enableLogging(true);
      executor.setDir(dir);
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Vpinzip Command Error:\n" + standardErrorFromCommand);
      }
      if (!StringUtils.isEmpty(standardOutputFromCommand.toString())) {
        LOG.error("Vpinzip Command StdOut:\n" + standardOutputFromCommand);
      }
      executor.executeCommand();
    } catch (Exception e) {
      LOG.error("Failed to execute vpinzip back job: " + e.getMessage(), e);
      return null;
    }

    File archiveFile = new File(this.archiveSourceAdapter.getArchiveSource().getLocation(), tableDetails.getGameName() + ".vpinzip");

    progress = 90;
    status = "Generating Metadata";

    ArchiveDescriptor archiveDescriptor = new ArchiveDescriptor();
    archiveDescriptor.setSource(archiveSourceAdapter.getArchiveSource());

    File wheelIcon = null;
    GameMediaItem gameMediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if(gameMediaItem != null) {
      wheelIcon = gameMediaItem.getFile();
    }

    ArchivePackageInfo packageInfo = VpinzipArchiveUtil.generatePackageInfo(archiveFile, wheelIcon);

    archiveDescriptor.setCreatedAt(new Date());
    archiveDescriptor.setTableDetails(tableDetails);
    archiveDescriptor.setFilename(tableDetails.getGameName() + ".vpinzip");
    archiveDescriptor.setPackageInfo(packageInfo);
    archiveDescriptor.setSize(archiveFile.length());

    ArchiveUtil.exportArchiveDescriptor(archiveDescriptor);

    progress = 100;

    archiveSourceAdapter.invalidate();
    return archiveDescriptor;
  }
}
