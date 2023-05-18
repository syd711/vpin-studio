package de.mephisto.vpin.server.io;

import de.mephisto.vpin.restclient.JobType;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.descriptors.ArchiveDownloadDescriptor;
import de.mephisto.vpin.restclient.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.descriptors.BackupDescriptor;
import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
import de.mephisto.vpin.server.backup.*;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.backup.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapterFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.jobs.JobQueue;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IOService {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private CardService cardService;

  @Autowired
  private ArchiveService archiveService;

  @Autowired
  private TableBackupAdapterFactory tableBackupAdapterFactory;

  @Autowired
  private TableInstallerAdapterFactory tableInstallerAdapterFactory;

  public boolean installArchive(@NonNull ArchiveRestoreDescriptor installDescriptor) {
    try {
      ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(installDescriptor.getArchiveSourceId(), installDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_INSTALL, installDescriptor.getFilename());
      jobDescriptor.setTitle("Restoring \"" + archiveDescriptor.getFilename() + "\"");
      jobDescriptor.setDescription("Restoring table from \"" + archiveDescriptor.getFilename() + "\"");

      TableInstallerAdapter adapter = tableInstallerAdapterFactory.createAdapter(archiveDescriptor);

      ArchiveInstallerJob job = new ArchiveInstallerJob(adapter, archiveDescriptor, pinUPConnector, cardService, archiveService, installDescriptor);
      jobDescriptor.setDescription("Restoring \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      jobDescriptor.setJob(job);

      JobQueue.getInstance().offer(jobDescriptor);
      LOG.info("Offered import job for \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public boolean downloadArchive(ArchiveDownloadDescriptor archiveDownloadDescriptor) {
    try {
      ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(archiveDownloadDescriptor.getArchiveSourceId(), archiveDownloadDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_DOWNLOAD_TO_REPOSITORY, archiveDownloadDescriptor.getFilename());
      jobDescriptor.setTitle("Download of \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");

      DownloadArchiveToRepositoryJob job = new DownloadArchiveToRepositoryJob(archiveService, archiveDescriptor);
      jobDescriptor.setDescription("Downloading \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      jobDescriptor.setJob(job);

      JobQueue.getInstance().offer(jobDescriptor);
      LOG.info("Offered archive download job for \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public boolean backupTable(@NonNull BackupDescriptor exportDescriptor) {
    List<Integer> gameIds = exportDescriptor.getGameIds();
    boolean result = true;
    for (Integer gameId : gameIds) {
      Game game = gameService.getGame(gameId);
      if (game != null && game.getGameFile().exists()) {
        if (!backupTable(game, exportDescriptor)) {
          result = false;
        }
      }
      else {
        LOG.error("Cancelled backup for id " + game + ", invalid game data.");
        result = false;
      }
    }
    return result;
  }

  private boolean backupTable(@NonNull Game game, @NonNull BackupDescriptor exportDescriptor) {
    JobDescriptor descriptor = new JobDescriptor(JobType.TABLE_BACKUP, UUID.randomUUID().toString());
    descriptor.setTitle("Backup of \"" + game.getGameDisplayName() + "\"");
    descriptor.setDescription("Creating backup of \"" + game.getGameDisplayName() + "\"");

    ArchiveSourceAdapter sourceAdapter = archiveService.getDefaultArchiveSourceAdapter();
    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(sourceAdapter, game);

    descriptor.setJob(new TableBackupJob(pinUPConnector, sourceAdapter, adapter, exportDescriptor, game.getId()));

    GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (mediaItem != null) {
      descriptor.setImageUrl(mediaItem.getUri());
    }

    JobQueue.getInstance().offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }
}
