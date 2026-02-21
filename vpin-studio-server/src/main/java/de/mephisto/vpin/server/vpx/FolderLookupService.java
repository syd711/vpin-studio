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

  @NonNull
  public File getAltSoundFolder(@NonNull Game game, String subfolder) {
    if (isPreferLegacyFileStructure(game.getEmulator())) {
      File folder = new File(game.getEmulator().getMameFolder(), "altsound");
      return new File(folder, subfolder);
    }

    return new File(game.getGameFolder(), "altsound/" + subfolder);
  }

  @NonNull
  public File getAltColorFolder(@NonNull Game game, String subfolder) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      File folder = new File(game.getEmulator().getMameFolder(), "altcolor");
      return new File(folder, subfolder);
    }

    return new File(game.getGameFolder(), "altcolor/" + subfolder);//TODO wrong for serum
  }

  @NonNull
  public File getNvRamFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "nvram");
    }

    return new File(game.getGameFolder(), "pinmame/nvram/");
  }

  @NonNull
  public File getRomFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "roms");
    }

    return new File(game.getGameFolder(), "pinmame/roms/");
  }

  @NonNull
  public File getScriptsFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "scripts");
    }

    return new File(game.getGameFolder(), "pinmame/scripts/");
  }

  @Nullable
  public File getCfgFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getMameFolder(), "cfg");
    }

    return new File(game.getGameFolder(), "pinmame/cfg/");
  }

  @Nullable
  public File getMusicFolder(@NonNull Game game) {
    if (isPreferLegacyFileStructure(game.getEmulator())) {
      File folder = new File(game.getEmulator().getInstallationFolder(), "Music");
      if (!StringUtils.isEmpty(game.getRom())) {
        File musicFolder = new File(folder, game.getRom());
        if (musicFolder.exists()) {
          return musicFolder;
        }
      }
    }

    return null;
  }

  @NonNull
  public File getUserFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(game.getEmulator().getInstallationFolder(), "User");
    }

    return new File(game.getGameFolder(), "user/");
  }

  public File getHighscoreTextFile(Game game) {
    if (!StringUtils.isEmpty(game.getHsFileName())) {
      return new File(getUserFolder(game), game.getHsFileName());
    }
    return null;
  }

  /**
   * Lookup the game VPReg.stg file based on the game file first.
   * Check the emulator next.
   *
   * @param game the game to retrieve the VPReg.stg file for
   * @return the VPReg.stg file or null
   */
  @NonNull
  public VPRegFile getVPRegFileForGame(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
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

  public boolean isRomExists(@NonNull Game game) {
    File romFile = getRomFile(game);
    return romFile != null && romFile.exists();
  }

  @Nullable
  public File getRomFile(@NonNull Game game) {
    File romFolder = getRomFolder(game);
    if (romFolder.exists() && !StringUtils.isEmpty(game.getRom())) {
      return new File(romFolder, game.getRom() + ".zip");
    }
    return null;
  }

  @Nullable
  public File getNvRamFile(@NonNull Game game) {
    if (game.getEmulator() == null || game.getEmulator().getMameDirectory() == null) {
      return null;
    }

    File nvRamFolder = getNvRamFolder(game);
    String rom = game.getRom();
    File defaultNvRam = new File(nvRamFolder, rom + ".nv");
    if (defaultNvRam.exists() && game.getNvOffset() == 0) {
      return defaultNvRam;
    }

    //if the text file exists, the version matches with the current table, so this one was played last and the default nvram has the latest score
    File versionTextFile = new File(nvRamFolder, game.getRom() + " v" + game.getNvOffset() + ".txt");
    if (versionTextFile.exists()) {
      return defaultNvRam;
    }

    //else, we can check if a nv file with the alias and version exists which means the another table with the same rom has been played after this table
    File nvOffsettedNvRam = new File(nvRamFolder, rom + " v" + game.getNvOffset() + ".nv");
    if (nvOffsettedNvRam.exists()) {
      return nvOffsettedNvRam;
    }

    return defaultNvRam;
  }

  @Nullable
  public File getCfgFile(@NonNull Game game) {
    File folder = getCfgFolder(game);
    if (!StringUtils.isEmpty(game.getRom()) && folder != null) {
      return new File(folder, game.getRom() + ".cfg");
    }
    return null;
  }

  private boolean isPreferLegacyFileStructure(@NonNull GameEmulator emulator) {
    return true; //emulator.getName().contains("10.8.1");//TODO 10.8.1
  }
}
