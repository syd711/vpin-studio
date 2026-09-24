package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.restclient.system.ScoringDBMapping;
import de.mephisto.vpin.restclient.util.OSUtil;
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
import java.util.List;
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

  /**
   * The folder of the Serum files (.cRZ, .cROMc). VPX 10.8.1 reads them from serum/&lt;rom&gt; next to the table.
   */
  @NonNull
  public File getSerumFolder(@NonNull Game game, String subfolder) {
    if (isPreferLegacyFileStructure(game.getEmulator())) {
      return getLegacyAltColorFolder(subfolder);
    }
    return new File(game.getGameFolder(), "serum/" + subfolder);
  }

  /**
   * The folder of the VNI, PAL and PAC files. VPX 10.8.1 reads them from vni/&lt;rom&gt; next to the table.
   */
  @NonNull
  public File getVniFolder(@NonNull Game game, String subfolder) {
    if (isPreferLegacyFileStructure(game.getEmulator())) {
      return getLegacyAltColorFolder(subfolder);
    }
    return new File(game.getGameFolder(), "vni/" + subfolder);
  }

  /**
   * All folders that may hold the colorization of a ROM: one folder in the legacy layout,
   * the Serum and the VNI folder next to the table otherwise.
   */
  @NonNull
  public List<File> getAltColorFolders(@NonNull Game game, String subfolder) {
    File serum = getSerumFolder(game, subfolder);
    File vni = getVniFolder(game, subfolder);
    return serum.equals(vni) ? List.of(serum) : List.of(serum, vni);
  }

  private File getLegacyAltColorFolder(String subfolder) {
    return new File(vPinMameService.getAltColorFolder(), subfolder);
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
    // the core scripts ship with VPX in both layouts; VPX searches the table folder before this one
    GameEmulator emulator = game.getEmulator();
    return new File(emulator.getInstallationFolder(), "scripts");
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
    if (isPreferLegacyFileStructure(emulator)) {
      return getMusicFolder(emulator);
    }
    // VPX resolves PlayMusic() against the music folder next to the table
    return new File(game.getGameFolder(), "music/");
  }


  @Nullable
  public File getMusicFolder(@NonNull GameEmulator emulator) {
    if (isPreferLegacyFileStructure(emulator)) {
      return new File(emulator.getInstallationFolder(), "Music/");
    }

    // without a table, there is no table folder to put the music next to
    return new File(emulator.getGamesFolder(), "music/");
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

    File stgFile = new File(getUserFolder(emulator), "VPReg.stg");
    if (stgFile.exists()) {
      VPRegFile reg = new VPRegFile(stgFile, game.getRom(), tableName);
      if (reg.isValid()) {
        return reg;
      }
    }

    // the user folder next to the table, where VPX 10.8.1 writes it
    File gameFileParent = game.getGameFile().getParentFile();
    stgFile = new File(gameFileParent, "user/VPReg.stg");
    if (!stgFile.exists() && gameFileParent != null) {
      File grandparent = gameFileParent.getParentFile();
      if (grandparent != null) {
        stgFile = new File(grandparent, "user/VPReg.stg");
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
      return findIgnoreCase(romFolder, game.getRom() + ".zip");
    }
    return null;
  }

  @Nullable
  public File getFpRamFile(@NonNull Game game) {
    if (game.getEmulator() == null) {
      return null;
    }

    File fpRamFolder = new File(game.getEmulator().getInstallationFolder(), "fpRAM");

    String name = FilenameUtils.getBaseName(game.getGameFileName()) + ".fpRAM";
    File fpRamFile = new File(fpRamFolder, name);
    if (fpRamFile.exists()) {
      return fpRamFile;
    }
    return null;
  }

  @Nullable
  public File getNvRamFile(@NonNull Game game) {
    GameEmulator emulator = game.getEmulator();
    if (emulator == null || (isPreferLegacyFileStructure(emulator) && emulator.getMameDirectory() == null)) {
      return null;
    }

    File nvRamFolder = getNvRamFolder(game);
    String rom = game.getRom();
    File defaultNvRam = findIgnoreCase(nvRamFolder, rom + ".nv");
    if (defaultNvRam.exists() && game.getNvOffset() == 0) {
      return defaultNvRam;
    }

    if (!StringUtils.isEmpty(game.getScannedRom())) {
      File defaultNvRam2 = findIgnoreCase(nvRamFolder, game.getScannedRom() + ".nv");
      if (defaultNvRam2.exists() && game.getNvOffset() == 0) {
        return defaultNvRam2;
      }
    }

    //if the text file exists, the version matches with the current table, so this one was played last and the default nvram has the latest score
    File versionTextFile = findIgnoreCase(nvRamFolder, game.getRom() + " v" + game.getNvOffset() + ".txt");
    if (versionTextFile.exists()) {
      return defaultNvRam;
    }

    if (!StringUtils.isEmpty(game.getScannedRom())) {
      File versionTextFile2 = findIgnoreCase(nvRamFolder, game.getScannedRom() + " v" + game.getNvOffset() + ".txt");
      if (versionTextFile2.exists()) {
        return versionTextFile2;
      }
    }

    //else, we can check if a nv file with the alias and version exists which means the another table with the same rom has been played after this table
    File nvOffsettedNvRam = findIgnoreCase(nvRamFolder, rom + " v" + game.getNvOffset() + ".nv");
    if (nvOffsettedNvRam.exists()) {
      return nvOffsettedNvRam;
    }

    return defaultNvRam;
  }

  @Nullable
  public File getCfgFile(@NonNull Game game) {
    File folder = getCfgFolder(game);
    if (!StringUtils.isEmpty(game.getRom()) && folder != null) {
      File f = findIgnoreCase(folder, game.getRom() + ".cfg");
      if (f.exists()) {
        return f;
      }
    }

    if (!StringUtils.isEmpty(game.getScannedRom())) {
      File scannedRom = findIgnoreCase(folder, game.getScannedRom() + ".cfg");
      if (scannedRom.exists()) {
        return scannedRom;
      }
    }
    return null;
  }

  /**
   * PinMAME names its files after the lowercase ROM name, while table scripts may spell the ROM in any case
   * (cGameName = "SS_15"). Finds the existing file regardless of case, for case-sensitive file systems.
   *
   * @return the existing file, or the file with the given name if there is none
   */
  @NonNull
  static File findIgnoreCase(@NonNull File folder, @NonNull String name) {
    File file = new File(folder, name);
    if (!file.exists()) {
      File[] matches = folder.listFiles((dir, candidate) -> candidate.equalsIgnoreCase(name));
      if (matches != null && matches.length > 0) {
        return matches[0];
      }
    }
    return file;
  }

  /**
   * The user folder of the installation, "User" on Windows but "user" in the standalone builds.
   */
  @NonNull
  private File getUserFolder(@NonNull GameEmulator emulator) {
    File installationFolder = emulator.getInstallationFolder();
    File userFolder = new File(installationFolder, "User");
    File lowerCase = new File(installationFolder, "user");
    return !userFolder.exists() && lowerCase.exists() ? lowerCase : userFolder;
  }

  private boolean isPreferLegacyFileStructure(@NonNull GameEmulator emulator) {
    return isPreferLegacyFileStructure(emulator, OSUtil.isWindows());
  }

  /**
   * Windows installs keep PinMAME files in the shared VPinMAME folder. A standalone install without that folder
   * keeps them next to each table, e.g. Tables/Twister/pinmame/roms, as VPX 10.8.1 does.
   */
  public static boolean isPreferLegacyFileStructure(@NonNull GameEmulator emulator, boolean windows) {
    return windows || emulator.getMameDirectory() != null;
  }
}
