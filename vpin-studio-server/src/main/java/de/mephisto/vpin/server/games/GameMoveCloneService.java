package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.dmd.DMDBackupData;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.SubfolderNaming;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.dmd.DMDDeviceIniService;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.apache.commons.io.FilenameUtils;
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
 * Moves or clones a VPX table between VPX emulators, see {@link #moveOrCloneGame(int, int, boolean, boolean, SubfolderNaming)}.
 */
@Service
public class GameMoveCloneService {
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

  /**
   * Moves or clones a VPX table to a different VPX emulator, copying the table file, its directly
   * associated files (.directb2s/.pov/.ini/.res), frontend media, ROM, NVRAM/highscore file, ALT
   * sound/color packages, FlexDMD/UltraDMD package and music files, whenever present. For a move, the
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
   */
  public Game moveOrCloneGame(int gameId, int targetEmulatorId, boolean move, boolean createSubfolder, SubfolderNaming subfolderNaming) throws Exception {
    Game original = gameService.getGame(gameId);
    if (original == null) {
      throw new Exception("No game found for id " + gameId);
    }
    if (!original.isVpxGame()) {
      throw new Exception("Only VPX tables can be moved or cloned between emulators.");
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

    LOG.info("Starting {} of \"{}\" from emulator \"{}\" to \"{}\"", move ? "move" : "clone",
        original.getGameDisplayName(), original.getEmulator().getName(), targetEmulator.getName());

    //resolve the target file, preserving a possible subfolder structure
    String relativeName = original.getGameFileName();
    File target;
    File targetSubFolder = null;
    if (createSubfolder) {
      String subFolderName = resolveSubfolderName(original, subfolderNaming);
      targetSubFolder = FileUtils.uniqueFolder(new File(targetEmulator.getGamesFolder(), subFolderName));
      targetSubFolder.mkdirs();
      target = new File(targetSubFolder, sourceFile.getName());
    }
    else if (relativeName.contains("\\")) {
      String subFolderName = relativeName.substring(0, relativeName.lastIndexOf("\\"));
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

    String targetFileName = targetSubFolder != null ? targetSubFolder.getName() + "\\" + target.getName() : target.getName();
    TableDetails clonedTableDetails = gameMediaService.getTableDetails(returningGameId);
    clonedTableDetails.setEmulatorId(targetEmulator.getId());
    clonedTableDetails.setGameFileName(targetFileName);
    clonedTableDetails.setGameName(importedGame.getGameName());
    clonedTableDetails.setGameDisplayName(move ? original.getGameDisplayName() : original.getGameDisplayName() + " (cloned)");
    gameMediaService.saveTableDetails(clonedTableDetails, returningGameId, false);
    frontendService.updateTableFileUpdated(returningGameId);

    //clone media and directly associated files
    gameMediaService.cloneGameMedia(original, importedGame);
    File targetParent = target.getParentFile();
    FileUtils.cloneFile(original.getDirectB2SFile(), targetParent, target.getName());
    FileUtils.cloneFile(original.getPOVFile(), targetParent, target.getName());
    FileUtils.cloneFile(original.getIniFile(), targetParent, target.getName());
    FileUtils.cloneFile(original.getResFile(), targetParent, target.getName());

    //carry over ROM/NVRAM, ALT sound/color, DMD package and music assets, see method javadoc
    copyRomAndNvRam(original, importedGame);
    copyFolderIfPresent(altSoundService.getAltSoundFolder(original), altSoundService.getAltSoundFolder(importedGame), "ALT sound package");
    copyFolderIfPresent(altColorService.getAltColorFolder(original), altColorService.getAltColorFolder(importedGame), "ALT color package");
    copyDmdPackage(original, importedGame);
    copyMusic(original, importedGame);

    frontendService.vpsLink(importedGame.getId(), original.getExtTableId(), original.getExtTableVersionId());

    if (move) {
      //clean up the original table now that the copy at the target emulator succeeded;
      //assets not covered by cloning above are intentionally left in place, see method javadoc
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
    }

    LOG.info("{} of \"{}\" to emulator \"{}\" successful.", move ? "Move" : "Clone", importedGame.getGameDisplayName(), targetEmulator.getName());
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
