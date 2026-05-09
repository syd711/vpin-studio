package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.system.ScoringDBMapping;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegFile;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * check VPX-10.8.1-FileLayout.md
 */
@Service
public class FolderLookupService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private SystemService systemService;

  @Autowired
  private VPinMameService vPinMameService;


  /*
  public Stream<File> getAltSoundFolders(List<GameEmulator> emulators) {
    return emulators.stream().parallel().filter(e -> e.isVpxEmulator()).flatMap(e -> {
      if (isPreferLegacyFileStructure(e)) {
        File altSoundFolder = new File(e.getMameFolder(), "altsound/");
        if (altSoundFolder.exists()) {
          try {
            return Files.list(altSoundFolder.toPath()).filter(p -> Files.isDirectory(p)).map(p -> p.toFile());
          }
          catch(IOException ioe) {
            LOG.warn("Cannot list files from {}", altSoundFolder.getAbsolutePath());
          }
        }
      } else {
        try {
          // list all directories from the emulator games folder
          return Files.list(e.getGamesFolder().toPath()).parallel().filter(p -> Files.isDirectory(p))
            .flatMap(p -> Files.list(p.resolve("altsound"))).filter(p -> Files.isDirectory(p))
            .map(p -> p.toFile());
        }
        catch(IOException ioe) {
          LOG.warn("Cannot list files from {}", e.getGamesFolder().getAbsolutePath());
        }
      }
      return Stream.empty();
    });
  }
  */

  @NonNull
  public File getAltSoundFolder(@NonNull Game game, String subfolder) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      File folder = new File(emulator.getMameFolder(), "altsound");
      return new File(folder, subfolder);
    }

    return new File(game.getGameFolder(), "altsound/" + subfolder);
  }

  @NonNull
  public File getAltColorFolder(@NonNull Game game, String subfolder) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      File folder = vPinMameService.getAltColorFolder();
      return new File(folder, subfolder);
    }

    return new File(game.getGameFolder(), "altcolor/" + subfolder);//TODO wrong for serum
  }

  @NonNull
  public File getNvRamFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      File folder = vPinMameService.getNvRamFolder();
      if (folder == null) {
        folder = new File(emulator.getMameFolder(), "nvram");
      }
      return folder;
    }

    return new File(game.getGameFolder(), "pinmame/nvram/");
  }

  @NonNull
  public File getRomFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      File folder = vPinMameService.getRomsFolder();
      if (folder == null) {
        folder = new File(emulator.getMameFolder(), "roms");
      }
      return folder;
    }

    return new File(game.getGameFolder(), "pinmame/roms/");
  }

  @NonNull
  public File getScriptsFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(emulator.getInstallationFolder(), "scripts");
    }

    return new File(game.getGameFolder(), "pinmame/scripts/");
  }

  @Nullable
  public File getCfgFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      File folder = vPinMameService.getCfgFolder();
      if (folder == null) {
        folder = new File(emulator.getMameFolder(), "cfg");
      }
      return folder;
    }

    return new File(game.getGameFolder(), "pinmame/cfg/");
  }

  @Nullable
  public File getGameMusicFolder(@NonNull Game game) {
    File musicRoot = getMusicFolder(game);
    if (musicRoot == null) {
      return null;
    }

    String effectiveRom = game.getRom();
    String assetsStr = game.getAssets();

    if (StringUtils.isEmpty(assetsStr)) {
      // No assets scanned — fall back to music root + ROM name as subfolder
      return StringUtils.isEmpty(effectiveRom) ? musicRoot : new File(musicRoot, effectiveRom);
    }

    // Collect distinct folder paths from the asset paths (e.g. "MFDOOM" from "MFDOOM/Attract*.mp3")
    Set<String> folders = new LinkedHashSet<>();
    for (String asset : assetsStr.split("\\|")) {
      if (StringUtils.isEmpty(asset)) {
        continue;
      }
      String folder = StringUtils.strip(FilenameUtils.getPath(asset), "/");
      if (!StringUtils.isEmpty(folder)) {
        folders.add(folder);
      }
    }

    if (folders.isEmpty()) {
      // All assets sit at the root level — return the root
      return musicRoot;
    }

    if (folders.size() == 1) {
      return new File(musicRoot, folders.iterator().next());
    }

    // Multiple folders: prefer the one whose last component matches the ROM name
    if (!StringUtils.isEmpty(effectiveRom)) {
      for (String folder : folders) {
        if (FilenameUtils.getName(folder).equalsIgnoreCase(effectiveRom)) {
          return new File(musicRoot, folder);
        }
      }
    }

    // No ROM match: pick the deepest folder (most path components)
    String deepest = folders.stream()
        .max(Comparator.comparingInt(f -> StringUtils.countMatches(f, '/') + 1))
        .orElseThrow();
    return new File(musicRoot, deepest);
  }


  @Nullable
  public File getMusicFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    return getMusicFolder(emulator);
  }


  @Nullable
  public File getMusicFolder(@NonNull GameEmulator emulator) {
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(emulator.getInstallationFolder(), "Music/");
    }

    File folder = new File(emulator.getGamesFolder(), "music/");
    if (!folder.exists() && !folder.mkdirs()) {
      LOG.warn("Failed to create game music folder {}", folder.getAbsolutePath());
    }
    return folder;
  }

  @NonNull
  public File getUserFolder(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(emulator.getInstallationFolder(), "User");
    }

    return new File(game.getGameFolder(), "user/");
  }

  public File getHighscoreTextFile(Game game) {
    if (!StringUtils.isEmpty(game.getHsFileName())) {
      File f = new File(getUserFolder(game), game.getHsFileName());
      if (f.exists()) {
        return f;
      }

      if (!StringUtils.isEmpty(game.getScannedHsFileName())) {
        f = new File(getUserFolder(game), game.getScannedHsFileName());
        if (f.exists()) {
          return f;
        }
      }

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
    if (highscoreMapping == null) {
      highscoreMapping = systemService.getScoringDatabase().getHighscoreMapping(game.getScannedRom());
    }

    if (StringUtils.isEmpty(tableName) && highscoreMapping != null) {
      tableName = highscoreMapping.getTableName();
    }

    File stgFile = new File(emulator.getInstallationFolder(), "User/VPReg.stg");
    if (isPreferLegacyFileStructure(emulator)) {
      stgFile = new File(game.getGameFile().getParentFile(), "user/VPReg.stg");
      if (!stgFile.exists()) {
        stgFile = new File(game.getGameFile().getParentFile().getParentFile(), "user/VPReg.stg");
      }
    }

    VPRegFile reg = new VPRegFile(stgFile, game.getRom(), tableName);
    if (reg.isValid()) {
      return reg;
    }

    if (!StringUtils.isEmpty(game.getScannedRom())) {
      VPRegFile regScanned = new VPRegFile(stgFile, game.getScannedRom(), tableName);
      if (regScanned.isValid()) {
        return regScanned;
      }
    }

    return reg;
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

    if (!StringUtils.isEmpty(game.getScannedRom())) {
      File defaultNvRam2 = new File(nvRamFolder, game.getScannedRom() + ".nv");
      if (defaultNvRam2.exists() && game.getNvOffset() == 0) {
        return defaultNvRam2;
      }
    }

    //if the text file exists, the version matches with the current table, so this one was played last and the default nvram has the latest score
    File versionTextFile = new File(nvRamFolder, game.getRom() + " v" + game.getNvOffset() + ".txt");
    if (versionTextFile.exists()) {
      return defaultNvRam;
    }

    if (!StringUtils.isEmpty(game.getScannedRom())) {
      File versionTextFile2 = new File(nvRamFolder, game.getScannedRom() + " v" + game.getNvOffset() + ".txt");
      if (versionTextFile2.exists()) {
        return versionTextFile2;
      }
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
      File f = new File(folder, game.getRom() + ".cfg");
      if (f.exists()) {
        return f;
      }
    }

    if (!StringUtils.isEmpty(game.getScannedRom())) {
      File scannedRom = new File(folder, game.getScannedRom() + ".cfg");
      if (scannedRom.exists()) {
        return scannedRom;
      }
    }
    return null;
  }

  private boolean isPreferLegacyFileStructure(@NonNull GameEmulator emulator) {
    return true; //emulator.getName().contains("10.8.1");//TODO 10.8.1
  }
}
