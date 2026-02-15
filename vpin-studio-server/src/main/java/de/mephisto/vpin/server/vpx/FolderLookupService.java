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


@Service
public class FolderLookupService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private SystemService systemService;

  @Nullable
  public File getAltSoundFolder(@NonNull Game game, String subfolder) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      File folder = new File(game.getEmulator().getMameFolder(), "altsound");
      return new File(folder, subfolder);
    }

    return new File(game.getGameFolder(), "altsound/" + subfolder);
  }

  @Nullable
  public File getAltColorFolder(@NonNull Game game, String subfolder) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      File folder = new File(game.getEmulator().getMameFolder(), "altcolor");
      return new File(folder, subfolder);
    }

    return new File(game.getGameFolder(), "altcolor/" + subfolder);//TODO wrong for serum
  }

  @Nullable
  public File getNvRamFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "nvram");
    }

    return new File(game.getGameFolder(), "pinmame/nvram/");
  }

  @Nullable
  public File getRomFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "roms");
    }

    return new File(game.getGameFolder(), "pinmame/roms/");
  }

  @Nullable
  public File getScriptsFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "scripts");
    }

    return new File(game.getGameFolder(), "pinmame/scripts/");
  }

  @Nullable
  public File getCfgFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "cfg");
    }

    return new File(game.getGameFolder(), "pinmame/cfg/");
  }

  @Nullable
  public File getMusicFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getInstallationFolder(), "Music");
    }

    return new File(game.getGameFolder(), "pinmame/music/");
  }

  @Nullable
  public File getUserFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null) {
      return null;
    }

    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getInstallationFolder(), "User");
    }

    return new File(game.getGameFolder(), "user/");
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

    if (isPreferLegacyFileStructure(emulator)) {
      File stgFile = new File(game.getGameFile().getParentFile(), "user/VPReg.stg");
      VPRegFile reg = new VPRegFile(stgFile, game.getRom(), tableName);
      if (reg.isValid()) {
        return reg;
      }
    }

    File stgFile = new File(emulator.getInstallationFolder(), "User/VPReg.stg");
    return new VPRegFile(stgFile, game.getRom(), tableName);
  }

  private boolean isPreferLegacyFileStructure(@NonNull GameEmulator emulator) {
    return emulator.getName().contains("10.8.1");//TODO 10.8.1
  }
}
