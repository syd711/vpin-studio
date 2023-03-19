package de.mephisto.vpin.server.io;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.restclient.*;
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
import de.mephisto.vpin.server.vpa.*;
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
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

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
  private VpaService vpaService;

  public boolean importVpa(@NonNull VpaImportDescriptor descriptor) {
    try {
      VpaDescriptor vpaDescriptor = vpaService.getVpaDescriptor(descriptor.getVpaSourceId(), descriptor.getUuid());
      File vpaFile = new File(systemService.getVpaArchiveFolder(), vpaDescriptor.getFilename());

      JobDescriptor jobDescriptor = new JobDescriptor(JobType.VPA_IMPORT, descriptor.getUuid());
      jobDescriptor.setTitle("Import of \"" + vpaDescriptor.getManifest().getGameDisplayName() + "\"");
      jobDescriptor.setDescription("Importing table for \"" + vpaDescriptor.getManifest().getGameDisplayName() + "\"");

      VpaImporterJob job = null;
      if (vpaDescriptor.getSource().getType().equals(VpaSourceType.Http.name())) {
        if (descriptor.isInstall()) {
          jobDescriptor.setDescription("Downloading and installing \"" + vpaDescriptor.getManifest().getGameDisplayName() + "\"");
        }
        else {
          jobDescriptor.setDescription("Downloading \"" + vpaDescriptor.getManifest().getGameDisplayName() + "\"");
        }
        job = new VpaDownloadAndImporterJob(vpaDescriptor, descriptor, vpaFile, pinUPConnector, systemService, highscoreService, vpaService, gameService, cardService);
      }
      else {
        job = new VpaImporterJob(descriptor, vpaFile, pinUPConnector, systemService, highscoreService, gameService, cardService);
      }
      jobDescriptor.setJob(job);

      JobQueue.getInstance().offer(jobDescriptor);
      LOG.info("Offered import job for \"" + vpaDescriptor.getManifest().getGameDisplayName() + "\"");
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public boolean exportVpa(@NonNull ExportDescriptor exportDescriptor) {
    List<Integer> gameIds = exportDescriptor.getGameIds();
    File targetFolder = systemService.getVpaArchiveFolder();

    //single export
    if (gameIds.size() == 1) {
      VpaManifest manifest = exportDescriptor.getManifest();
      Game game = gameService.getGame(gameIds.get(0));
      if (game != null) {
        return exportVpa(game, manifest, exportDescriptor, targetFolder);
      }
    }
    else {
      //multi export
      boolean result = true;
      for (Integer gameId : gameIds) {
        VpaManifest manifest = getManifest(gameId);
        Game game = gameService.getGame(gameId);
        if (game != null) {
          if (!exportVpa(game, manifest, exportDescriptor, targetFolder)) {
            result = false;
          }
        }
      }
      return result;
    }
    return false;
  }

  private boolean exportVpa(@NonNull Game game, @NonNull VpaManifest manifest, @NonNull ExportDescriptor exportDescriptor, @NonNull File targetFolder) {
    List<HighscoreVersion> versions = highscoreService.getAllHighscoreVersions(game.getId());
    Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(game);
    File vpRegFile = systemService.getVPRegFile();
    VpaSourceAdapter defaultVpaSourceAdapter = vpaService.getDefaultVpaSourceAdapter();

    String uuid = UUID.randomUUID().toString();
    manifest.setUuid(uuid);

    JobDescriptor descriptor = new JobDescriptor(JobType.VPA_EXPORT, uuid);
    descriptor.setTitle("Export of \"" + game.getGameDisplayName() + "\"");
    descriptor.setDescription("Exporting table archive for \"" + manifest.getGameDisplayName() + "\"");
    descriptor.setJob(new VpaExporterJob(pinUPConnector, vpRegFile, systemService.getVPXMusicFolder(), game, exportDescriptor, manifest, highscore, versions, defaultVpaSourceAdapter, targetFolder, systemService.getVersion()));

    GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (mediaItem != null) {
      descriptor.setImageUrl(mediaItem.getUri());
    }

    JobQueue.getInstance().offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }

  public VpaManifest getManifest(int id) {
    return pinUPConnector.getGameManifest(id);
  }

}
