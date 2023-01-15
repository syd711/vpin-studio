package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class VpaService {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PinUPConnector pinUPConnector;

  public boolean importVpa(@NonNull ImportDescriptor descriptor) {
    LOG.info("Starting import of " + descriptor.getManifest().getGameName());
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
      new Thread(() -> {
        Thread.currentThread().setName("VPA Export Thread for " + game.getGameDisplayName());
        List<HighscoreVersion> versions = highscoreService.getAllHighscoreVersions(game.getId());
        VpaExporter exporter = new VpaExporter(game, exportDescriptor, versions, target, (file, zipPath) -> {
//        System.out.println(zipPath);
        });
        exporter.startExport();
      }).start();
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
