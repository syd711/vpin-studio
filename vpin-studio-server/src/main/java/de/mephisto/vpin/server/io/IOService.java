package de.mephisto.vpin.server.io;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.restclient.*;
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
  private ArchiveService vpaService;

  public boolean importVpa(@NonNull VpaImportDescriptor descriptor) {
    try {
      ArchiveDescriptor vpaDescriptor = vpaService.getArchiveDescriptor(descriptor.getVpaSourceId(), descriptor.getUuid());
      File vpaFile = new File(systemService.getVpaArchiveFolder(), vpaDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.VPA_IMPORT, descriptor.getUuid());
      jobDescriptor.setTitle("Import of \"" + vpaDescriptor.getTableDetails().getGameDisplayName() + "\"");
      jobDescriptor.setDescription("Importing table for \"" + vpaDescriptor.getTableDetails().getGameDisplayName() + "\"");

      VpaImporterJob job = null;
      if (vpaDescriptor.getSource().getType().equals(ArchiveSourceType.Http.name())) {
        if (descriptor.isInstall()) {
          jobDescriptor.setDescription("Downloading and installing \"" + vpaDescriptor.getTableDetails().getGameDisplayName() + "\"");
        }
        else {
          jobDescriptor.setDescription("Downloading \"" + vpaDescriptor.getTableDetails().getGameDisplayName() + "\"");
        }
        job = new VpaDownloadAndImporterJob(vpaDescriptor, descriptor, vpaFile, pinUPConnector, systemService, highscoreService, vpaService, gameService, cardService);
      }
      else {
        job = new VpaImporterJob(descriptor, vpaFile, pinUPConnector, systemService, highscoreService, gameService, cardService);
      }
      jobDescriptor.setJob(job);

      JobQueue.getInstance().offer(jobDescriptor);
      LOG.info("Offered import job for \"" + vpaDescriptor.getTableDetails().getGameDisplayName() + "\"");
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public boolean exportArchive(@NonNull BackupDescriptor exportDescriptor) {
    List<Integer> gameIds = exportDescriptor.getGameIds();
    File targetFolder = systemService.getVpaArchiveFolder();

    //single export
    if (gameIds.size() == 1) {
      Game game = gameService.getGame(gameIds.get(0));
      if (game != null) {
        return exportArchive(game, exportDescriptor, targetFolder);
      }
    }
    else {
      //multi export
      boolean result = true;
      for (Integer gameId : gameIds) {
        Game game = gameService.getGame(gameId);
        if (game != null) {
          if (!exportArchive(game, exportDescriptor, targetFolder)) {
            result = false;
          }
        }
      }
      return result;
    }
    return false;
  }

  private boolean exportArchive(@NonNull Game game, @NonNull BackupDescriptor exportDescriptor, @NonNull File targetFolder) {
    List<HighscoreVersion> versions = highscoreService.getAllHighscoreVersions(game.getId());
    Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(game);
    File vpRegFile = systemService.getVPRegFile();
    ArchiveSourceAdapter defaultVpaSourceAdapter = vpaService.getDefaultArchiveSourceAdapter();
    JobDescriptor descriptor = new JobDescriptor(JobType.VPA_EXPORT, UUID.randomUUID().toString());
    descriptor.setTitle("Export of \"" + game.getGameDisplayName() + "\"");
    descriptor.setDescription("Exporting table archive for \"" + game.getGameDisplayName() + "\"");
    descriptor.setJob(new VpaExporterJob(pinUPConnector, vpRegFile, systemService.getVPXMusicFolder(), game, exportDescriptor, highscore, defaultVpaSourceAdapter, targetFolder, systemService.getVersion()));

    GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (mediaItem != null) {
      descriptor.setImageUrl(mediaItem.getUri());
    }

    JobQueue.getInstance().offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }
}
