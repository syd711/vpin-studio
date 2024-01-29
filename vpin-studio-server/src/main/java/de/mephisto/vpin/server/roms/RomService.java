package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
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

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @NonNull
  public ScanResult scanGameFile(@NonNull Game game) {
    if (Emulator.isVisualPinball(game.getEmulator().getName(), game.getEmulator().getDisplayName(), game.getEmulator().getDescription(), game.getEmulator().getGameExt())) {
      if (game.getGameFile().exists()) {
        return VPXFileScanner.scan(game.getGameFile());
      }

      LOG.info("Skipped reading of " + game.getGameDisplayName() + ", VPX file '" + game.getGameFile().getAbsolutePath() + "' does not exist.");
      return new ScanResult();
    }
    LOG.info("Skipped reading of " + game.getGameDisplayName() + " (emulator '" + game.getEmulator() + "'), only VPX tables can be scanned.");
    return new ScanResult();
  }

  public boolean deleteAliasMapping(int emuId, String alias) throws IOException {
    GameEmulator gameEmulator = this.pinUPConnector.getGameEmulator(emuId);
    return mameRomAliasService.deleteAliasMapping(gameEmulator, alias);
  }

  public boolean saveAliasMapping(int emuId, Map<String, String> values) throws IOException {
    GameEmulator gameEmulator = this.pinUPConnector.getGameEmulator(emuId);
    return mameRomAliasService.saveAliasMapping(gameEmulator, values);
  }
}
