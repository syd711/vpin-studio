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
  public final static String DATA_VREG_HIGHSCORE = "VRegHighscore";

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
      int gameId = importer.startImport();
      if (gameId != -1) {
        gameService.scanGame(gameId);
        Game game = gameService.getGame(gameId);
        cardService.generateCard(game, false);
        return true;
      }
    } catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean exportVpa(@NonNull ExportDescriptor exportDescriptor) {
    Game game = gameService.getGame(exportDescriptor.getGameId());
    if (game != null) {
      File target = new File(systemService.getVpaArchiveFolder(), game.getGameDisplayName().replaceAll(" ", "-") + ".vpa");
      return exportVpa(game, exportDescriptor, target);
    }
    return false;
  }

  public boolean exportVpa(@NonNull Game game, @NonNull ExportDescriptor exportDescriptor, @NonNull File target) {
    VpaManifest manifest = exportDescriptor.getManifest();
    if (manifest != null) {
      List<HighscoreVersion> versions = highscoreService.getAllHighscoreVersions(game.getId());
      Highscore highscore = highscoreService.getOrCreateHighscore(game);

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
          return new VpaExporterJob(game, exportDescriptor, highscore, versions, target);
        }

        @Override
        public String getImageUrl() {
          GameMediaItem mediaItem = game.getGameMedia().get(PopperScreen.Wheel);
          if(mediaItem != null) {
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
    return false;
  }

  public VpaManifest getManifest(int id) {
    return pinUPConnector.getGameManifest(id);
  }

  public VpaManifest getManifest(File out) {
    return VpaUtil.readManifest(out);
  }
}
