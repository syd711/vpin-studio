package de.mephisto.vpin.server.io;

import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.tables.descriptors.*;
import de.mephisto.vpin.server.archiving.*;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapterFactory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IOService {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private CardService cardService;

  @Autowired
  private ArchiveService archiveService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private TableBackupAdapterFactory tableBackupAdapterFactory;

  @Autowired
  private TableInstallerAdapterFactory tableInstallerAdapterFactory;

  @Autowired
  private JobQueue jobQueue;

  public boolean installArchive(@NonNull ArchiveRestoreDescriptor installDescriptor) {
    try {
      ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(installDescriptor.getArchiveSourceId(), installDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_INSTALL, installDescriptor.getFilename());
      jobDescriptor.setTitle("Restoring \"" + archiveDescriptor.getFilename() + "\"");
      jobDescriptor.setDescription("Restoring table from \"" + archiveDescriptor.getFilename() + "\"");

      TableInstallerAdapter adapter = tableInstallerAdapterFactory.createAdapter(archiveDescriptor);

      ArchiveInstallerJob job = new ArchiveInstallerJob(adapter, archiveDescriptor, pinUPConnector, cardService, gameService, archiveService, installDescriptor);
      jobDescriptor.setDescription("Restoring \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      jobDescriptor.setJob(job);

      jobQueue.offer(jobDescriptor);
      LOG.info("Offered import job for \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public String bundle(ArchiveBundleDescriptor archiveBundleDescriptor) {
    try {
      List<ArchiveDescriptor> bundleArchiveDescriptors = new ArrayList<>();
      List<String> archiveNames = archiveBundleDescriptor.getArchiveNames();
      for (String archiveName : archiveNames) {
        ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(archiveBundleDescriptor.getArchiveSourceId(), archiveName);
        bundleArchiveDescriptors.add(archiveDescriptor);
      }

//      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_BUNDLING, UUID.randomUUID().toString());
//      jobDescriptor.setTitle("Archive Bundle");
//
      BundleArchivesJob job = new BundleArchivesJob(archiveService, systemService, archiveBundleDescriptor, bundleArchiveDescriptors);
      job.execute();

      return job.getTarget().getName();

//      jobDescriptor.setDescription("Creating bundle of " + bundleArchiveDescriptors.size() + " archived table(s).");
//      jobDescriptor.setJob(job);
//      JobQueue.getInstance().offer(jobDescriptor);
    } catch (Exception e) {
      LOG.error("Bundling failed: " + e.getMessage(), e);
    }
    return null;
  }

  public boolean copyToRepository(ArchiveCopyToRepositoryDescriptor archiveCopyToRepositoryDescriptor) {
    try {
      ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(archiveCopyToRepositoryDescriptor.getArchiveSourceId(), archiveCopyToRepositoryDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_DOWNLOAD_TO_REPOSITORY, archiveCopyToRepositoryDescriptor.getFilename());
      jobDescriptor.setTitle("Download of \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");

      CopyArchiveToRepositoryJob job = new CopyArchiveToRepositoryJob(archiveService, archiveDescriptor, archiveCopyToRepositoryDescriptor.isOverwrite());
      jobDescriptor.setDescription("Downloading \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      jobDescriptor.setJob(job);

      jobQueue.offer(jobDescriptor);
      LOG.info("Offered archive copying for \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
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
    descriptor.setGameId(game.getId());
    descriptor.setDescription("Creating backup of \"" + game.getGameDisplayName() + "\"");

    ArchiveSourceAdapter sourceAdapter = archiveService.getDefaultArchiveSourceAdapter();
    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(sourceAdapter, game);

    descriptor.setJob(new TableBackupJob(pinUPConnector, sourceAdapter, adapter, exportDescriptor, game.getId()));

    GameMediaItem mediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
    if (mediaItem != null) {
      descriptor.setImageUrl(mediaItem.getUri());
    }

    jobQueue.offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }
}
