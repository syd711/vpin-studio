package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.server.games.Game;
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
  private PinUPConnector pinUPConnector;

  public boolean export(@NonNull Game game) {
    File target = new File(getArchivePath(), game.getGameDisplayName().replaceAll(" ", "-") + ".vpa");
    return export(game, target);
  }

  public boolean export(@NonNull Game game, @NonNull File target) {
    VpaManifest manifest = pinUPConnector.getGameManifest(game.getId());
    if(manifest != null) {
      VpaExporter exporter = new VpaExporter(game, manifest, target, (file, zipPath) -> {
//        System.out.println(zipPath);
      });
      exporter.export();
      return true;
    }
    return false;
  }

  public File getArchivePath() {
    return systemService.getVpaArchiveFolder();
  }
}
