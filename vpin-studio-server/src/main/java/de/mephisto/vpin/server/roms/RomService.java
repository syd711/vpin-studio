package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.util.VPXFileScanner;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class RomService {
  private final static Logger LOG = LoggerFactory.getLogger(RomService.class);

  public RomService() {
  }

  @Autowired
  private PinUPConnector pinUPConnector;

  @NonNull
  public ScanResult scanGameFile(@NonNull Game game) {
    if (Emulator.isVisualPinball(game.getEmulator().getName())) {
      if (game.getGameFile().exists()) {
        return VPXFileScanner.scan(game.getGameFile());
      }

      LOG.info("Skipped reading of " + game.getGameDisplayName() + ", VPX file '" + game.getGameFile().getAbsolutePath() + "' does not exist.");
      return new ScanResult();
    }
    LOG.info("Skipped reading of " + game.getGameDisplayName() + " (emulator '" + game.getEmulator() + "'), only VPX tables can be scanned.");
    return new ScanResult();
  }

  public boolean clearCache() {
    this.pinUPConnector.getGameEmulators().stream().forEach(e -> e.clearCache());
    return true;
  }

  public boolean deleteAliasMapping(int emuId, String alias) throws IOException {
    return this.pinUPConnector.getGameEmulator(emuId).deleteAliasMapping(alias);
  }

  public boolean saveAliasMapping(int emuId, Map<String, Object> values) throws IOException {
    return this.pinUPConnector.getGameEmulator(emuId).saveAliasMapping(values);
  }
}
