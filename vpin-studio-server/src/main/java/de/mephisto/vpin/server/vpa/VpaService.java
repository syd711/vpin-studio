package de.mephisto.vpin.server.vpa;

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
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class VpaService {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  public final static String DATA_HIGHSCORE_HISTORY = "highscores";
  public final static String DATA_HIGHSCORE = "highscore";
  public final static String DATA_VPREG_HIGHSCORE = "vpregHighscore";

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

  public boolean importVpa(@NonNull ImportDescriptor descriptor) {
    try {
      File vpaFile = new File(systemService.getVpaArchiveFolder(), descriptor.getVpaFileName());
      VpaImporter importer = new VpaImporter(descriptor, vpaFile, pinUPConnector, systemService, highscoreService);
      Game importedGame = importer.startImport();
      if (importedGame != null) {
        gameService.scanGame(importedGame.getId());
        gameService.save(importedGame);

        Game game = gameService.getGame(importedGame.getId());
        cardService.generateCard(game, false);
        return true;
      }
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean exportVpa(@NonNull ExportDescriptor exportDescriptor) {
    List<Integer> gameIds = exportDescriptor.getGameIds();

    //single export
    if (gameIds.size() == 1) {
      VpaManifest manifest = exportDescriptor.getManifest();
      Game game = gameService.getGame(gameIds.get(0));
      if (game != null) {
        File target = new File(systemService.getVpaArchiveFolder(), game.getGameDisplayName().replaceAll(" ", "-") + ".vpa");
        return exportVpa(game, manifest, exportDescriptor, target);
      }
    }
    else {
      //multi export
      boolean result = true;
      for (Integer gameId : gameIds) {
        VpaManifest manifest = this.getManifest(gameId);
        Game game = gameService.getGame(gameId);
        if (game != null) {
          File target = new File(systemService.getVpaArchiveFolder(), game.getGameDisplayName().replaceAll(" ", "-") + ".vpa");
          if(!exportVpa(game, manifest, exportDescriptor, target)) {
            result = false;
          }
        }
      }
      return result;
    }

    return false;
  }

  private boolean exportVpa(@NonNull Game game, @NonNull VpaManifest manifest, @NonNull ExportDescriptor exportDescriptor, @NonNull File target) {
    List<HighscoreVersion> versions = highscoreService.getAllHighscoreVersions(game.getId());
    Highscore highscore = highscoreService.getOrCreateHighscore(game);
    File vpRegFile = systemService.getVPRegFile();

    JobDescriptor descriptor = new JobDescriptor() {
      @Override
      public String getTitle() {
        return "Export of '" + game.getGameDisplayName() + "'";
      }

      @Override
      public String getDescription() {
        return "Exporting table archive " + target.getName();
      }

      @Override
      public Job getJob() {
        return new VpaExporterJob(vpRegFile,  systemService.getVPXMusicFolder(), game, exportDescriptor, manifest, highscore, versions, target);
      }

      @Override
      public String getImageUrl() {
        GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
        if (mediaItem != null) {
          return mediaItem.getUri();
        }
        return super.getImageUrl();
      }
    };

    descriptor.setUuid(UUID.randomUUID().toString());
    JobQueue.getInstance().offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }

  public VpaManifest getManifest(int id) {
    return pinUPConnector.getGameManifest(id);
  }
}
