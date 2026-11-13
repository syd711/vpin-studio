package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.dmd.DMDBackupData;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.SubfolderNaming;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.dmd.DMDDeviceIniService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import de.mephisto.vpin.restclient.util.SystemUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

/**
 * Moves or copies a VPX table between VPX emulators, see {@link #moveOrCopyGame(int, int, boolean, boolean, SubfolderNaming)}.
 */
@Service
public class GameMoveCopyService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private GameService gameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private DMDService dmdService;

  @Autowired
  private DMDDeviceIniService dmdDeviceIniService;

  @Autowired
  private MusicService musicService;

  @Autowired
  private FolderLookupService folderLookupService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private SystemService systemService;

  /**
   * Moves or copies a VPX table to a different VPX emulator, copying the table file, its directly
   * associated files (.directb2s/.pov/.ini/.vr.ini/.res), frontend media, ROM, NVRAM, PinMAME .cfg,
   * highscore text file / VPReg.stg, ALT sound/color packages, FlexDMD/UltraDMD package, music files
   * and (for the VPX 10.8.1 layout) the PUP pack, whenever present. For a move, the
   * original table entry and its VPX-adjacent files (media, .directb2s/.pov/.ini/.res/.vbs) are deleted
   * once the copy at the target emulator has succeeded. The ROM/NVRAM/ALT sound/ALT color/music/DMD
   * assets are only ever copied, never deleted from the source: they are commonly keyed by ROM name
   * rather than by table, so another table under the source emulator may still reference the very same
   * files (this also matches how the ROM/NVRAM folders are frequently a single, registry-configured
   * location shared by every VPX emulator on the machine, in which case source and target resolve to
   * the same file and the copy is a no-op).
   * <p>
   * When {@code createSubfolder} is set, the table is placed into a new subfolder of the target
   * emulator's games folder instead of preserving any existing subfolder structure, named after the
   * table according to {@code subfolderNaming}.
   * <p>
   * The file layout is decided by the target emulator, see {@link GameEmulator#isPerTableFileStructureSupported()}:
   * <ul>
   *   <li>VPX 10.8.1 or newer: the table always gets its own subfolder, which uses the VPX 10.8.1 file layout, see
   *   {@link FolderLookupService#isPerTableFileStructure(Game)}. All files except the frontend media are placed into
   *   it (pinmame/roms, pinmame/nvram, pinmame/cfg, altsound, serum, vni, music, user, pupvideos). The frontend
   *   media stays in the media folders of the frontend, as PinUP Popper does not support the medias folder of the
   *   VPX 10.8.1 layout yet.</li>
   *   <li>Older versions: the subfolder is optional, the files are placed into the shared folders of the
   *   installation (VPinMAME roms/nvram/cfg/altsound/altcolor, Music, User, PUPVideos).</li>
   * </ul>
   * When a table in the VPX 10.8.1 layout is moved, its files belong to this table only, so the source table folder
   * is deleted with all its content once the move has succeeded, unless it still holds another table.
   */
  public Game moveOrCopyGame(int gameId, int targetEmulatorId, boolean move, boolean createSubfolder, SubfolderNaming subfolderNaming) throws Exception {
    Game original = gameService.getGame(gameId);
    if (original == null) {
      throw new Exception("No game found for id " + gameId);
    }
    if (!original.isVpxGame()) {
      throw new Exception("Only VPX tables can be moved or copied between emulators.");
    }

    GameEmulator targetEmulator = emulatorService.getGameEmulator(targetEmulatorId);
    if (targetEmulator == null || !targetEmulator.isVpxEmulator()) {
      throw new Exception("No VPX emulator found for id " + targetEmulatorId);
    }
    if (targetEmulator.getId() == original.getEmulatorId()) {
      throw new Exception("Source and target emulator are identical.");
    }

    File sourceFile = original.getGameFile();
    if (!sourceFile.exists()) {
      throw new Exception("The table file \"" + sourceFile.getName() + "\" does not exist.");
    }

    LOG.info("Starting {} of \"{}\" from emulator \"{}\" to \"{}\"", move ? "move" : "copy",
        original.getGameDisplayName(), original.getEmulator().getName(), targetEmulator.getName());

    //VPX 10.8.1 keeps the ROM, NVRAM, ... next to the table, so every table needs its own folder there
    boolean perTableTarget = targetEmulator.isPerTableFileStructureSupported();
    if (perTableTarget) {
      createSubfolder = true;
    }
    boolean perTableSource = folderLookupService.isPerTableFileStructure(original);
    LOG.info("File layout of the source: {}, of the target: {} (VPX version {})", perTableSource ? "per table" : "shared",
        perTableTarget ? "per table" : "shared", targetEmulator.getEmulatorVersion());

    //resolve the target file, preserving a possible subfolder structure
    String relativeName = original.getGameFileName();
    File target;
    File targetSubFolder = null;
    if (createSubfolder) {
      String subFolderName = resolveSubfolderName(original, subfolderNaming);
      targetSubFolder = FileUtils.uniqueFolder(new File(targetEmulator.getGamesFolder(), subFolderName));
      targetSubFolder.mkdirs();
      if (perTableTarget) {
        //must exist before the moved table is resolved, so that all its files are looked up next to it
        folderLookupService.createPerTableFileStructure(targetSubFolder);
      }
      target = new File(targetSubFolder, sourceFile.getName());
    }
    else if (FileUtils.lastSeparatorIndex(relativeName) >= 0) {
      String subFolderName = relativeName.substring(0, FileUtils.lastSeparatorIndex(relativeName));
      targetSubFolder = FileUtils.uniqueFolder(new File(targetEmulator.getGamesFolder(), subFolderName));
      targetSubFolder.mkdirs();
      target = new File(targetSubFolder, sourceFile.getName());
    }
    else {
      target = FileUtils.uniqueFile(new File(targetEmulator.getGamesFolder(), sourceFile.getName()));
    }

    org.apache.commons.io.FileUtils.copyFile(sourceFile, target);
    LOG.info("Copied \"{}\" to \"{}\"", sourceFile.getAbsolutePath(), target.getAbsolutePath());

    int returningGameId = frontendService.importGame(target, true, -1, targetEmulator.getId());
    if (returningGameId < 0) {
      throw new Exception("Failed to register \"" + target.getName() + "\" with emulator \"" + targetEmulator.getName() + "\".");
    }

    Game importedGame = gameService.scanGame(returningGameId, true);

    String targetFileName = targetSubFolder != null ? FileUtils.joinGameFileName(targetSubFolder.getName(), target.getName()) : target.getName();
    TableDetails targetTableDetails = gameMediaService.getTableDetails(returningGameId);
    targetTableDetails.setEmulatorId(targetEmulator.getId());
    targetTableDetails.setGameFileName(targetFileName);
    targetTableDetails.setGameName(importedGame.getGameName());
    targetTableDetails.setGameDisplayName(move ? original.getGameDisplayName() : original.getGameDisplayName() + " (copy)");
    gameMediaService.saveTableDetails(targetTableDetails, returningGameId, false);
    frontendService.updateTableFileUpdated(returningGameId);

    //copy media and directly associated files
    gameMediaService.cloneGameMedia(original, importedGame);
    File targetParent = target.getParentFile();
    FileUtils.cloneFile(original.getDirectB2SFile(), targetParent, target.getName());
    FileUtils.cloneFile(original.getPOVFile(), targetParent, target.getName());
    FileUtils.cloneFile(original.getIniFile(), targetParent, target.getName());
    FileUtils.cloneFile(original.getResFile(), targetParent, target.getName());
    gameMediaService.cloneVRIniFile(original, target);

    //carry over ROM/NVRAM, ALT sound/color, DMD package and music assets, see method javadoc
    copyRomAndNvRam(original, importedGame);
    copyCfg(original, importedGame);
    copyHighscoreFiles(original, importedGame);
    copyFolderIfPresent(altSoundService.getAltSoundFolder(original), altSoundService.getAltSoundFolder(importedGame), "ALT sound package");
    altColorService.copyAltColor(original, importedGame);
    copyDmdPackage(original, importedGame);
    copyMusic(original, importedGame);
    copyPupPack(original, importedGame);

    //must go through the GameService: the VPS mapping is stored in the GameDetails too (the only storage when the
    //frontend has no VPS mapping columns) and the cached game has to be refreshed, the frontend only is not enough
    if (StringUtils.isNotEmpty(original.getExtTableId())) {
      gameService.vpsLink(importedGame.getId(), original.getExtTableId(), original.getExtTableVersionId());
    }

    if (move) {
      //clean up the original table now that the copy at the target emulator succeeded;
      //assets not covered by copying above are intentionally left in place, see method javadoc
      DeleteDescriptor descriptor = new DeleteDescriptor();
      descriptor.setGameIds(Collections.singletonList(original.getId()));
      descriptor.setDeleteTable(true);
      descriptor.setDeleteDirectB2s(true);
      descriptor.setDeletePov(true);
      descriptor.setDeleteIni(true);
      descriptor.setDeleteRes(true);
      descriptor.setDeleteVbs(true);
      descriptor.setDeleteFromFrontend(true);
      descriptor.setKeepAssets(false);
      descriptor.setDeleteBAMCfg(false);
      descriptor.setDeleteHighscores(false);
      descriptor.setDeleteDMDs(false);
      descriptor.setDeletePinVol(false);
      descriptor.setDeleteAlias(false);
      descriptor.setDeleteB2STableSettings(false);
      descriptor.setDeleteDMDDeviceIni(false);
      descriptor.setDeletePupPack(false);
      descriptor.setDeleteMusic(false);
      descriptor.setDeleteAltSound(false);
      descriptor.setDeleteAltColor(false);
      descriptor.setDeleteCfg(false);
      descriptor.setDeleteRom(false);
      if (!gameMediaService.deleteGame(descriptor)) {
        LOG.warn("Failed to fully clean up the original table \"{}\" after moving it.", original.getGameDisplayName());
      }

      if (perTableSource) {
        deleteSourceTableFolder(sourceFile.getParentFile(), original.getEmulator());
      }
    }

    LOG.info("{} of \"{}\" to emulator \"{}\" successful.", move ? "Move" : "Copy", importedGame.getGameDisplayName(), targetEmulator.getName());
    return gameService.getGame(returningGameId);
  }

  /**
   * Copies the ROM and NVRAM/highscore file resolved for the original game to wherever they resolve
   * to for the imported game (usually the target emulator's own MAME folder, unless ROM/NVRAM storage
   * is configured machine-wide via the VPinMAME registry settings, in which case source and target are
   * the very same file and nothing is copied).
   */
  private void copyRomAndNvRam(Game original, Game importedGame) {
    try {
      File sourceRom = folderLookupService.getRomFile(original);
      if (sourceRom != null && sourceRom.exists()) {
        File targetRomFolder = folderLookupService.getRomFolder(importedGame);
        File targetRom = new File(targetRomFolder, sourceRom.getName());
        if (!sourceRom.getAbsoluteFile().equals(targetRom.getAbsoluteFile())) {
          targetRomFolder.mkdirs();
          org.apache.commons.io.FileUtils.copyFile(sourceRom, targetRom);
          LOG.info("Copied ROM \"{}\" to \"{}\"", sourceRom.getAbsolutePath(), targetRom.getAbsolutePath());
        }
      }

      File sourceNvRam = folderLookupService.getNvRamFile(original);
      if (sourceNvRam != null && sourceNvRam.exists()) {
        File targetNvRamFolder = folderLookupService.getNvRamFolder(importedGame);
        File targetNvRam = new File(targetNvRamFolder, sourceNvRam.getName());
        if (!sourceNvRam.getAbsoluteFile().equals(targetNvRam.getAbsoluteFile())) {
          targetNvRamFolder.mkdirs();
          org.apache.commons.io.FileUtils.copyFile(sourceNvRam, targetNvRam);
          LOG.info("Copied NVRAM \"{}\" to \"{}\"", sourceNvRam.getAbsolutePath(), targetNvRam.getAbsolutePath());
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy ROM/NVRAM for \"{}\": {}", original.getGameDisplayName(), e.getMessage(), e);
    }
  }

  /**
   * Copies the PinMAME .cfg file (DIP switches, key mappings) of the ROM.
   */
  private void copyCfg(Game original, Game importedGame) {
    try {
      File sourceCfg = folderLookupService.getCfgFile(original);
      File targetCfgFolder = folderLookupService.getCfgFolder(importedGame);
      if (sourceCfg != null && sourceCfg.exists() && targetCfgFolder != null) {
        copyAssetFile(sourceCfg, new File(targetCfgFolder, sourceCfg.getName()), "PinMAME .cfg file", true);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy PinMAME .cfg file for \"{}\": {}", original.getGameDisplayName(), e.getMessage(), e);
    }
  }

  /**
   * Copies the highscore files kept in the user folder: the highscore text file of EM tables, or the
   * VPReg.stg file of tables saving their highscores via the script. The VPReg.stg file holds the entries of
   * all tables, so an existing one of the target is never overwritten.
   */
  private void copyHighscoreFiles(Game original, Game importedGame) {
    try {
      File targetUserFolder = folderLookupService.getUserFolder(importedGame);

      File sourceHsFile = folderLookupService.getHighscoreTextFile(original);
      if (sourceHsFile != null && sourceHsFile.exists()) {
        copyAssetFile(sourceHsFile, new File(targetUserFolder, sourceHsFile.getName()), "highscore text file", true);
      }

      if (HighscoreType.VPReg.equals(original.getHighscoreType())) {
        File sourceStg = folderLookupService.getVPRegFileForGame(original).getFile();
        if (sourceStg != null && sourceStg.exists()) {
          copyAssetFile(sourceStg, new File(targetUserFolder, sourceStg.getName()), "VPReg.stg file", false);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy highscore files for \"{}\": {}", original.getGameDisplayName(), e.getMessage(), e);
    }
  }

  /**
   * Copies the PUP pack into the pupvideos folder next to the table, where the PinUP Player plugin of VPX 10.8.1
   * looks for it. The PUP pack in the PinUP Popper folder is kept, as PinUP Popper still uses it.
   */
  private void copyPupPack(Game original, Game importedGame) {
    if (!folderLookupService.isPerTableFileStructure(importedGame)) {
      copyPupPacksToPinUPSystem(original);
      return;
    }
    try {
      PupPack pupPack = pupPacksService.getPupPack(original);
      if (pupPack != null && pupPack.getPupPackFolder() != null) {
        File source = pupPack.getPupPackFolder();
        File target = new File(new File(importedGame.getGameFolder(), "pupvideos"), source.getName());
        copyFolderIfPresent(source, target, "PUP pack");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy PUP pack for \"{}\": {}", original.getGameDisplayName(), e.getMessage(), e);
    }
  }

  /**
   * Copies the PUP packs from the pupvideos folder of a table in the VPX 10.8.1 layout into the PUPVideos folder of
   * the PinUP system, which is used by older VPX versions. Existing PUP packs are not overwritten.
   */
  private void copyPupPacksToPinUPSystem(Game original) {
    if (!folderLookupService.isPerTableFileStructure(original)) {
      return;
    }
    try {
      File[] packs = new File(original.getGameFolder(), "pupvideos").listFiles(File::isDirectory);
      if (packs == null || packs.length == 0) {
        return;
      }
      File pupVideosFolder = new File(systemService.getPinupInstallationFolder(), "PUPVideos");
      if (!pupVideosFolder.exists()) {
        LOG.warn("Skipped copying the PUP packs of \"{}\", there is no PUPVideos folder {}", original.getGameDisplayName(), pupVideosFolder.getAbsolutePath());
        return;
      }
      for (File pack : packs) {
        File target = new File(pupVideosFolder, pack.getName());
        if (!target.exists()) {
          copyFolderIfPresent(pack, target, "PUP pack");
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy the PUP packs of \"{}\": {}", original.getGameDisplayName(), e.getMessage(), e);
    }
  }

  /**
   * Deletes the folder of a moved table in the VPX 10.8.1 layout: the ROM, NVRAM, ... in it belong to this table only
   * and have been copied to the target. The games folder and folders that still hold another table are kept.
   */
  private void deleteSourceTableFolder(File tableFolder, GameEmulator emulator) {
    try {
      File gamesFolder = emulator.getGamesFolder();
      if (!tableFolder.exists() || tableFolder.getAbsoluteFile().equals(gamesFolder.getAbsoluteFile())
          || !tableFolder.toPath().toAbsolutePath().normalize().startsWith(gamesFolder.toPath().toAbsolutePath().normalize())) {
        return;
      }

      String gameExt = StringUtils.defaultIfEmpty(emulator.getGameExt(), "vpx");
      File[] otherTables = tableFolder.listFiles((dir, name) -> FilenameUtils.isExtension(name.toLowerCase(), gameExt.toLowerCase()));
      if (otherTables != null && otherTables.length > 0) {
        LOG.info("Kept table folder \"{}\", it still contains {} table file(s)", tableFolder.getAbsolutePath(), otherTables.length);
        return;
      }

      if (SystemUtil.deleteFileOrFolder(tableFolder)) {
        LOG.info("Deleted table folder \"{}\" of the moved table", tableFolder.getAbsolutePath());
      }
      else {
        LOG.warn("Failed to delete table folder \"{}\" of the moved table", tableFolder.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to delete table folder \"{}\": {}", tableFolder.getAbsolutePath(), e.getMessage(), e);
    }
  }

  private void copyAssetFile(File source, File target, String label, boolean overwrite) throws Exception {
    if (source.getAbsoluteFile().equals(target.getAbsoluteFile()) || (!overwrite && target.exists())) {
      return;
    }
    target.getParentFile().mkdirs();
    org.apache.commons.io.FileUtils.copyFile(source, target);
    LOG.info("Copied {} \"{}\" to \"{}\"", label, source.getAbsolutePath(), target.getAbsolutePath());
  }

  /**
   * Copies the FlexDMD/UltraDMD package folder, if any, plus its position/size entry in the source
   * emulator's DmdDevice.ini (restored into the target emulator's own DmdDevice.ini).
   */
  private void copyDmdPackage(Game original, Game importedGame) {
    try {
      DMDPackage dmdPackage = dmdService.getDMDPackage(original);
      if (dmdPackage != null && dmdPackage.isValid()) {
        copyFolderIfPresent(dmdService.getDmdFolder(original), dmdService.getDmdFolder(importedGame), "DMD package");
      }

      DMDBackupData dmdBackupData = dmdDeviceIniService.getBackupData(original);
      if (dmdBackupData != null) {
        dmdDeviceIniService.restore(importedGame, dmdBackupData);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to copy DMD package for \"{}\": {}", original.getGameDisplayName(), e.getMessage(), e);
    }
  }

  /**
   * Copies the music files referenced by the table script (resolved the same way {@link MusicService}
   * resolves them) from the original game's music folder to the imported game's music folder.
   */
  private void copyMusic(Game original, Game importedGame) {
    try {
      List<File> sourceFiles = musicService.getMp3Files(original);
      if (sourceFiles.isEmpty()) {
        return;
      }

      File sourceMusicFolder = folderLookupService.getGameMusicFolder(original);
      File targetMusicFolder = folderLookupService.getGameMusicFolder(importedGame);
      if (sourceMusicFolder == null || targetMusicFolder == null || sourceMusicFolder.getAbsoluteFile().equals(targetMusicFolder.getAbsoluteFile())) {
        return;
      }

      for (File sourceFile : sourceFiles) {
        String relative = sourceMusicFolder.toPath().relativize(sourceFile.toPath()).toString();
        File targetFile = new File(targetMusicFolder, relative);
        targetFile.getParentFile().mkdirs();
        org.apache.commons.io.FileUtils.copyFile(sourceFile, targetFile);
      }
      LOG.info("Copied {} music file(s) from \"{}\" to \"{}\"", sourceFiles.size(), sourceMusicFolder.getAbsolutePath(), targetMusicFolder.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Failed to copy music for \"{}\": {}", original.getGameDisplayName(), e.getMessage(), e);
    }
  }

  /**
   * Resolves the subfolder name a table is placed into when {@code createSubfolder} is enabled,
   * sanitizing it for use as a Windows folder name.
   */
  private String resolveSubfolderName(Game original, SubfolderNaming subfolderNaming) {
    String name;
    switch (subfolderNaming) {
      case TABLE_DISPLAY_NAME:
        name = original.getGameDisplayName();
        break;
      case TABLE_FILENAME:
        name = FilenameUtils.getBaseName(original.getGameFileName());
        break;
      case TABLE_NAME:
      default:
        name = original.getGameName();
        break;
    }
    return FileUtils.replaceWindowsChars(name).trim();
  }

  private void copyFolderIfPresent(@Nullable File source, @Nullable File target, String label) {
    try {
      if (source == null || target == null || !source.exists() || source.getAbsoluteFile().equals(target.getAbsoluteFile())) {
        return;
      }
      org.apache.commons.io.FileUtils.copyDirectory(source, target);
      LOG.info("Copied {} \"{}\" to \"{}\"", label, source.getAbsolutePath(), target.getAbsolutePath());
    }
    catch (Exception e) {
      LOG.error("Failed to copy {} \"{}\": {}", label, source.getAbsolutePath(), e.getMessage(), e);
    }
  }
}
