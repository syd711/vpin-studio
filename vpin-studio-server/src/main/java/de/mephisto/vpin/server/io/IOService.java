package de.mephisto.vpin.server.io;

import de.mephisto.vpin.restclient.JobType;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.descriptors.ArchiveDownloadAndInstallDescriptor;
import de.mephisto.vpin.restclient.descriptors.ArchiveInstallDescriptor;
import de.mephisto.vpin.restclient.descriptors.BackupDescriptor;
import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
import de.mephisto.vpin.server.backup.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
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

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IOService {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private CardService cardService;

  @Autowired
  private ArchiveService archiveService;

  public boolean installArchive(@NonNull ArchiveInstallDescriptor descriptor) {
    try {
      ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(descriptor.getArchiveSourceId(), descriptor.getFilename());
      File archiveFile = new File(systemService.getVpaArchiveFolder(), archiveDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_INSTALL, descriptor.getFilename());
      jobDescriptor.setTitle("Import of \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      jobDescriptor.setDescription("Importing table for \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");

      ArchiveInstallerJob job = new ArchiveInstallerJob(descriptor, archiveFile, pinUPConnector, systemService, highscoreService, gameService, cardService);
      jobDescriptor.setDescription("Installing \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      jobDescriptor.setJob(job);

      JobQueue.getInstance().offer(jobDescriptor);
      LOG.info("Offered import job for \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public boolean downloadArchive(ArchiveDownloadAndInstallDescriptor downloadAndInstallDescriptor) {
    try {
      ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(downloadAndInstallDescriptor.getArchiveSourceId(), downloadAndInstallDescriptor.getFilename());
      File archiveFile = new File(systemService.getVpaArchiveFolder(), archiveDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_DOWNLOAD_TO_REPOSITORY, downloadAndInstallDescriptor.getFilename());
      jobDescriptor.setTitle("Download of \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");

      DownloadArchiveAndInstallJob job = new DownloadArchiveAndInstallJob(archiveDescriptor, downloadAndInstallDescriptor, archiveFile, pinUPConnector, systemService, highscoreService, archiveService, gameService, cardService);
      jobDescriptor.setDescription("Downloading \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      if (downloadAndInstallDescriptor.isInstall()) {
        jobDescriptor.setDescription("Downloading and installing \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      }
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
    File targetFolder = systemService.getVpaArchiveFolder();
    boolean result = true;
    for (Integer gameId : gameIds) {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        if (!backupTable(game, exportDescriptor, targetFolder)) {
          result = false;
        }
      }
    }
    return result;
  }

  private boolean backupTable(@NonNull Game game, @NonNull BackupDescriptor exportDescriptor, @NonNull File targetFolder) {
    List<HighscoreVersion> versions = highscoreService.getAllHighscoreVersions(game.getId());
    Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(game);
    File vpRegFile = systemService.getVPRegFile();
    ArchiveSourceAdapter defaultVpaSourceAdapter = archiveService.getDefaultArchiveSourceAdapter();
    JobDescriptor descriptor = new JobDescriptor(JobType.TABLE_BACKUP, UUID.randomUUID().toString());
    descriptor.setTitle("Export of \"" + game.getGameDisplayName() + "\"");
    descriptor.setDescription("Exporting table archive for \"" + game.getGameDisplayName() + "\"");
    descriptor.setJob(new TableBackupJob(pinUPConnector, vpRegFile, systemService.getVPXMusicFolder(), game, exportDescriptor, highscore, defaultVpaSourceAdapter, targetFolder));

    GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (mediaItem != null) {
      descriptor.setImageUrl(mediaItem.getUri());
    }

    JobQueue.getInstance().offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }
}
