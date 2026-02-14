package de.mephisto.vpin.server.highscores.parsing.vpreg;

import de.mephisto.vpin.restclient.system.ScoringDBMapping;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VPRegService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private SystemService systemService;

  private final Map<Integer, List<String>> vpRegEntries = new HashMap<>();

  public boolean isValid(Game game) {
    VPRegFile reg = getVPRegFileForGame(game);
    if (reg != null) {
      return reg.isValid();
    }
    return false;
  }

  @Nullable
  public String getVPRegJson(@NonNull Game game) {
    VPRegFile reg = getVPRegFileForGame(game);
    if (reg != null) {
      return reg.toJson();
    }
    return null;
  }

  @Nullable
  public VPRegFile getVPRegFile(@NonNull Game game) {
    return getVPRegFileForGame(game);
  }

  public boolean resetHighscores(Game game, long score) {
    VPRegFile vpRegFile = getVPRegFileForGame(game);
    if (vpRegFile != null) {
      return vpRegFile.resetHighscores(score);
    }
    return false;
  }

  public void refreshVPRegEntries() {
    try {
      vpRegEntries.clear();
      List<GameEmulator> gameEmulators = emulatorService.getVpxGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        File vpRegFile = gameEmulator.getVPRegFile();
        VPRegFile regFile = new VPRegFile(vpRegFile, null, null);
        vpRegEntries.put(gameEmulator.getId(), regFile.getEntries());
      }
      LOG.info("VPRegService read " + vpRegEntries.size() + " VPReg.stg entries for emulators");
    }
    catch (Exception e) {
      LOG.error("Failed to refresh emulator VPReg entries: " + e.getMessage(), e);
    }
  }

  /**
   * Lookup the game VPReg.stg file based on the game file first.
   * Check the emulator next.
   *
   * @param game the game to retrieve the VPReg.stg file for
   * @return the VPReg.stg file or null
   */
  @Nullable
  private VPRegFile getVPRegFileForGame(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    String tableName = game.getTableName();
    ScoringDBMapping highscoreMapping = systemService.getScoringDatabase().getHighscoreMapping(game.getRom());
    if (StringUtils.isEmpty(tableName) && highscoreMapping != null) {
      tableName = highscoreMapping.getTableName();
    }

    File stgFile = new File(game.getGameFile().getParentFile(), "user/VPReg.stg");
    VPRegFile reg = new VPRegFile(stgFile, game.getRom(), tableName);
    if (reg.isValid()) {
      return reg;
    }

    stgFile = emulator.getVPRegFile();
    reg = new VPRegFile(stgFile, game.getRom(), tableName);
    if (reg.isValid()) {
      return reg;
    }

    return null;
  }
}
