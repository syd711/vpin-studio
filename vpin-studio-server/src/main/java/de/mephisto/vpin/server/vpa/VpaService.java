package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class VpaService {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PinUPConnector pinUPConnector;

  public boolean export(@NonNull ExportDescriptor exportDescriptor) {
    Game game = gameService.getGame(exportDescriptor.getGameId());
    if (game != null) {
      File target = new File(getArchivePath(), game.getGameDisplayName().replaceAll(" ", "-") + ".vpa");
      return export(game, exportDescriptor, target);
    }
    return false;
  }

  public boolean export(@NonNull Game game, @NonNull ExportDescriptor exportDescriptor, @NonNull File target) {
    VpaManifest manifest = exportDescriptor.getManifest();
    if (manifest != null) {
      new Thread(() -> {
        Thread.currentThread().setName("VPA Export Thread for " + game.getGameDisplayName());
        VpaExporter exporter = new VpaExporter(game, exportDescriptor, target, (file, zipPath) -> {
//        System.out.println(zipPath);
        });
        exporter.export();
      }).start();
      return true;
    }
    return false;
  }

  public File getArchivePath() {
    return systemService.getVpaArchiveFolder();
  }

  public VpaManifest getManifest(int id) {
    return pinUPConnector.getGameManifest(id);
  }
}
