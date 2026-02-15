package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.system.ScoringDBMapping;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegFile;
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

import static de.mephisto.vpin.server.VPinStudioServer.Features;

@Service
public class FolderLookupService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private SystemService systemService;

  public File getAltSoundFolder(@NonNull Game game, String subfolder) {
    if (Features.IS_STANDALONE) {
      return new File(game.getGameFolder(), "vpinmame/altsound/" + subfolder);
    }
    else if (game.getEmulator() != null) {
      return new File(game.getEmulator().getAltSoundFolder(), subfolder);
    }
    // else
    return null;
  }

  /**
   * Lookup the game VPReg.stg file based on the game file first.
   * Check the emulator next.
   *
   * @param game the game to retrieve the VPReg.stg file for
   * @return the VPReg.stg file or null
   */
  @Nullable
  public VPRegFile getVPRegFileForGame(@NonNull Game game) {
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

    return reg;
  }
}
