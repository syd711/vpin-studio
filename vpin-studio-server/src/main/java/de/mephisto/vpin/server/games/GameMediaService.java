package de.mephisto.vpin.server.games;

import de.mephisto.vpin.connectors.vps.matcher.VpsMatch;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.VpaArchiveUtil;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaService;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.frontend.WheelIconDelete;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.pinvol.PinVolService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameMediaService {
  private final static Logger LOG = LoggerFactory.getLogger(GameMediaService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private DMDService dmdService;

  @Autowired
  private PinVolService pinVolService;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private MameService mameService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private MusicService musicService;

  @Autowired
  private CardService cardService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private VpaService vpaService;

  /**
   * moved from VpsService to break circular dependency.
   */
  public VpsMatch autoMatch(Game game, boolean overwrite, boolean simulate) {
    VpsMatch vpsMatch = vpsService.autoMatch(game, overwrite);
    if (vpsMatch != null && !simulate) {
      gameService.vpsLink(game.getId(), vpsMatch.getExtTableId(), vpsMatch.getExtTableVersionId());
      //immediately re-apply the new match since the post processing access it
      game.setExtTableId(vpsMatch.getExtTableId());
      game.setExtTableVersionId(vpsMatch.getExtTableVersionId());
      if (StringUtils.isNotEmpty(vpsMatch.getVersion())) {
        fixGameVersion(game.getId(), vpsMatch.getVersion(), false);
      }
    }
    return vpsMatch;
  }

  public void fixGameVersion(int gameId, String version, boolean overwrite) {
    // keep track of the version  in the internal database
    if (gameService.fixVersion(gameId, version, overwrite)) {
      // update the table in the frontend
      TableDetails tableDetails = getTableDetails(gameId);
      if (tableDetails != null) {
        tableDetails.setGameVersion(version);
        saveTableDetails(tableDetails, gameId, false);
      }
    }
  }

  public TableDetails getTableDetails(int gameId) {
    return frontendService.getTableDetails(gameId);
  }


  public TableDetails saveTableDetails(TableDetails updatedTableDetails, int gameId, boolean renamingChecks) {
    //fetch existing data first
    TableDetails originalTableDetails = getTableDetails(gameId);
    Game game = frontendService.getOriginalGame(gameId);

    //fix input and save input
    String gameFilename = updatedTableDetails.getGameFileName();
    if (game.isVpxGame() && !gameFilename.endsWith(".vpx")) {
      gameFilename = gameFilename + ".vpx";
      updatedTableDetails.setGameFileName(gameFilename);
    }
    else if (game.isFpGame() && !gameFilename.endsWith(".fpt")) {
      gameFilename = gameFilename + ".fpt";
      updatedTableDetails.setGameFileName(gameFilename);
    }

    gameService.fixVersion(gameId, updatedTableDetails.getGameVersion(), true);
    frontendService.saveTableDetails(gameId, updatedTableDetails);

    //for upload and replace, we do not need any renaming
    if (game.isVpxGame() || game.isFpGame()) {
      if (!renamingChecks) {
        if (game.isVpxGame()) {
          runHighscoreRefreshCheck(game, originalTableDetails, updatedTableDetails);
        }
        return updatedTableDetails;
      }

      //rename game filename which results in renaming VPX related files
      if (!updatedTableDetails.getGameFileName().equals(originalTableDetails.getGameFileName())) {
        String name = FilenameUtils.getBaseName(updatedTableDetails.getGameFileName());
        String existingName = FilenameUtils.getBaseName(game.getGameFile().getName());
        if (!existingName.equalsIgnoreCase(name)) {
          if (game.getGameFile().exists()) {
            de.mephisto.vpin.restclient.util.FileUtils.renameToBaseName(game.getGameFile(), name);
          }

          if (game.getDirectB2SFile().exists()) {
            de.mephisto.vpin.restclient.util.FileUtils.renameToBaseName(game.getDirectB2SFile(), name);
          }

          if (game.getPOVFile().exists()) {
            de.mephisto.vpin.restclient.util.FileUtils.renameToBaseName(game.getPOVFile(), name);
          }

          if (game.getResFile().exists()) {
            de.mephisto.vpin.restclient.util.FileUtils.renameToBaseName(game.getResFile(), name);
          }

          if (game.getIniFile().exists()) {
            de.mephisto.vpin.restclient.util.FileUtils.renameToBaseName(game.getIniFile(), name);
          }

          if (game.getBAMCfgFile().exists()) {
            de.mephisto.vpin.restclient.util.FileUtils.renameToBaseName(game.getBAMCfgFile(), name);
          }

          if (game.getVBSFile().exists()) {
            de.mephisto.vpin.restclient.util.FileUtils.renameToBaseName(game.getVBSFile(), name);
          }
          LOG.info("Finished game file renaming from \"" + originalTableDetails.getGameFileName() + "\" to \"" + updatedTableDetails.getGameFileName() + "\"");
        }
        else {
          //revert to old value
          updatedTableDetails.setGameFileName(originalTableDetails.getGameFileName());
          frontendService.saveTableDetails(gameId, updatedTableDetails);
          LOG.info("Renaming game file from \"" + originalTableDetails.getGameFileName() + "\" to \"" + updatedTableDetails.getGameFileName() + "\" failed, game file renaming failed.");
        }
      }
    }


    //rename the game name, which results in renaming all assets
    if (!updatedTableDetails.getGameName().equals(originalTableDetails.getGameName())) {
      renameGameMedia(game, originalTableDetails.getGameName(), updatedTableDetails.getGameName());
    }

    if (game.isVpxGame()) {
      runHighscoreRefreshCheck(game, originalTableDetails, updatedTableDetails);
    }

    gameLifecycleService.notifyGameDataChanged(game.getId(), originalTableDetails, updatedTableDetails);

    return updatedTableDetails;
  }


  public void runHighscoreRefreshCheck(Game game, TableDetails oldDetails, TableDetails newDetails) {
    boolean romChanged = !StringUtils.equalsIgnoreCase(oldDetails.getRomName(), newDetails.getRomName());
    boolean hsChanged = !StringUtils.equalsIgnoreCase(oldDetails.getHsFilename(), newDetails.getHsFilename());

    if (romChanged || hsChanged) {
      LOG.info("Game highscore data fields have been changed, triggering score check.");
      highscoreService.scanScore(game, EventOrigin.USER_INITIATED);
      cardService.generateCard(game);
    }
  }

  public void uploadAndReplace(File temporaryVPXFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());
    if (gameEmulator == null) {
      throw new Exception("No emulator found for id " + uploadDescriptor.getEmulatorId() + " to replace table for.");
    }

    TableDetails tableDetails = getTableDetails(uploadDescriptor.getGameId());
    if (tableDetails == null) {
      throw new Exception("No table details found for the selected game to replace (ID: " + uploadDescriptor.getGameId() + ").");
    }
    tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too

    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    boolean keepExistingFilename = serverSettings.isVpxKeepFileNames();
    boolean keepExistingDisplayName = serverSettings.isVpxKeepDisplayNames();
    boolean keepCopy = serverSettings.isBackupTableOnOverwrite();
    boolean keepModificationDate = serverSettings.isKeepModificationDate();
    boolean autoFill = uploadDescriptor.isAutoFill();

    //create backup first and delete existing table
    File existingVPXFile = new File(gameEmulator.getGamesDirectory(), tableDetails.getGameFileName());
    long existingModifiationDate = existingVPXFile.lastModified();
    if (existingVPXFile.exists()) {
      if (keepCopy) {
        File tableBackupsFolder = gameEmulator.getTableBackupsFolder();
        tableBackupsFolder.mkdirs();
        String format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        File backup = new File(tableBackupsFolder, FilenameUtils.getBaseName(existingVPXFile.getName()) + "[" + format + "].vpx");
        org.apache.commons.io.FileUtils.copyFile(existingVPXFile, backup);
        LOG.info("Created backup VPX file \"" + backup.getAbsolutePath() + "\"");
      }

      if (!existingVPXFile.delete()) {
        LOG.error("Failed to delete existing old game file \"" + existingVPXFile.getAbsolutePath() + "\"");
        throw new Exception("Failed to delete existing old game file \"" + existingVPXFile.getName() + "\"");
      }
    }
    else {
      LOG.warn("VPX file to overwrite \"" + existingVPXFile.getAbsolutePath() + "\" was not found.");
    }

    //delete existing .vbs file
    String baseName = FilenameUtils.getBaseName(tableDetails.getGameFileName());
    File existingVbsFile = new File(gameEmulator.getGamesDirectory(), baseName + ".vbs");
    if (existingVbsFile.exists() && !existingVbsFile.delete()) {
      LOG.error("Failed to delete existing .vbs file \"" + existingVbsFile.getAbsolutePath() + "\"");
    }

    //Determine target name
    File target = new File(existingVPXFile.getParentFile(), existingVPXFile.getName());
    if (!keepExistingFilename) {
      String vpxFileName = analysis.getTableFileName(uploadDescriptor.getOriginalUploadFileName());
      if (vpxFileName == null) {
        vpxFileName = uploadDescriptor.getOriginalUploadFileName();
      }
      target = new File(existingVPXFile.getParentFile(), vpxFileName);
      LOG.info("Resolved target VPX file \"" + target.getAbsolutePath() + "\"");
    }

    //copy file
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, target);
    LOG.info("Copied temporary VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" to target \"" + target.getAbsolutePath() + "\"");

    //keep modification date
    if (keepModificationDate) {
      boolean b = target.setLastModified(existingModifiationDate);
      if (b) {
        LOG.info("Reverted modification of VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" to \"" + new Date(existingModifiationDate) + "\"");
      }
      else {
        LOG.warn("Revetring modification of VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" failed.");
      }
    }


    //delete possibly existing .vbs file that matches with the new name
    baseName = FilenameUtils.getBaseName(target.getName());
    existingVbsFile = new File(target.getParentFile(), baseName + ".vbs");
    if (existingVbsFile.exists() && !existingVbsFile.delete()) {
      LOG.error("Failed to delete .vbs file \"" + existingVbsFile.getAbsolutePath() + "\"");
    }

    //update frontend table database entry
    //if the VPX file is inside a subfolder, we have to prepend the folder name
    String name = target.getName();
    String oldName = tableDetails.getGameFileName();
    if (oldName.contains("\\")) {
      name = oldName.substring(0, oldName.lastIndexOf("\\") + 1) + target.getName();
    }

    LOG.info("Updated database filename to \"" + name + "\"");
    tableDetails.setGameFileName(name);
    if (!keepExistingDisplayName) {
      tableDetails.setGameDisplayName(FilenameUtils.getBaseName(analysis.getTableFileName(uploadDescriptor.getOriginalUploadFileName())));
    }

    saveTableDetails(tableDetails, uploadDescriptor.getGameId(), !keepExistingFilename);
    frontendService.updateTableFileUpdated(uploadDescriptor.getGameId());

    Game game = gameService.scanGame(uploadDescriptor.getGameId());
    if (game != null) {
      gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVPX);
      gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVersionVPX);

      if (uploadDescriptor.isAutoFill()) {
        autoMatch(game, true, false);
      }

      tableDetails = getTableDetails(game.getId());
      if (tableDetails != null && autoFill) {
        frontendService.autoFill(game, tableDetails, false);
      }

      TableDataUtil.setMappedFieldValue(tableDetails, serverSettings.getMappingPatchVersion(), uploadDescriptor.getPatchVersion());
      frontendService.saveTableDetails(game.getId(), tableDetails);
      LOG.info("Import of \"" + game.getGameDisplayName() + "\" successful.");
    }
  }

  public void uploadAndImport(File temporaryVPXFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());

    File tablesFolder = gameEmulator.getGamesFolder();
    if (uploadDescriptor.isFolderBasedImport()) {
      LOG.info("Using folder based import.");
      tablesFolder = new File(tablesFolder, uploadDescriptor.getSubfolderName().trim());
    }
    File targetVPXFile = new File(tablesFolder, uploadDescriptor.getOriginalUploadFileName());

    for (String archiveSuffix : PackageUtil.ARCHIVE_SUFFIXES) {
      if (FilenameUtils.getExtension(uploadDescriptor.getTempFilename()).equalsIgnoreCase(archiveSuffix)) {
        targetVPXFile = new File(tablesFolder, analysis.getTableFileName(uploadDescriptor.getOriginalUploadFileName()));
        break;
      }
    }
    targetVPXFile = FileUtils.uniqueFile(targetVPXFile);

    LOG.info("Resolve target VPX: " + targetVPXFile.getAbsolutePath());
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, targetVPXFile);
    LOG.info("Copied for import '" + temporaryVPXFile.getAbsolutePath() + "' to '" + targetVPXFile.getAbsolutePath() + "'");

    int returningGameId = frontendService.importGame(targetVPXFile, true, -1, gameEmulator.getId());
    if (returningGameId >= 0) {
      Game game = gameService.scanGame(returningGameId);
      if (game != null) {
        if (!uploadDescriptor.isBackupRestoreMode()) {
          if (uploadDescriptor.isAutoFill()) {
            autoMatch(game, true, false);
          }

          TableDetails tableDetails = getTableDetails(game.getId());
          if (tableDetails != null && uploadDescriptor.isAutoFill()) {
            tableDetails = frontendService.autoFill(game, tableDetails, false);
          }

          TableDataUtil.setMappedFieldValue(tableDetails, serverSettings.getMappingPatchVersion(), uploadDescriptor.getPatchVersion());
          frontendService.saveTableDetails(game.getId(), tableDetails);
        }
        else {
          //we have read the table details, including the mapping from the VPA file.
          TableDetails tableDetails = VpaArchiveUtil.readTableDetails(analysis.getFile());
          tableDetails.setEmulatorId(uploadDescriptor.getEmulatorId());
          frontendService.saveTableDetails(game.getId(), tableDetails);
        }


        uploadDescriptor.setGameId(returningGameId);
        LOG.info("Import of \"" + game.getGameDisplayName() + "\" successful.");
      }
    }
  }

  public void uploadAndClone(File temporaryVPXFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    LOG.info("Starting cloning for {}", temporaryVPXFile.getAbsolutePath());
    GameEmulator gameEmulator = emulatorService.getGameEmulator(uploadDescriptor.getEmulatorId());
    TableDetails tableDetails = getTableDetails(uploadDescriptor.getGameId());
    tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too

    boolean autoFill = uploadDescriptor.isAutoFill();

    File existingVPXFile = new File(gameEmulator.getGamesDirectory(), tableDetails.getGameFileName());
    if (!existingVPXFile.exists()) {
      throw new UnsupportedOperationException("The VPX file to clone \"" + tableDetails.getGameFileName() + "\" does not exist.");
    }

    //Determine target name
    File target = new File(existingVPXFile.getParentFile(), existingVPXFile.getName());
    File targetSubFolder = null;
    String fileName = target.getName();
    if (uploadDescriptor.isFolderBasedImport()) {
      //use the parents parent so that we are back inside the tables folder
      targetSubFolder = new File(gameEmulator.getGamesFolder(), uploadDescriptor.getSubfolderName());
      targetSubFolder = FileUtils.uniqueFolder(targetSubFolder);
      targetSubFolder.mkdirs();
      target = new File(targetSubFolder, target.getName());
      fileName = targetSubFolder.getName() + "\\" + target.getName();

      LOG.info("Clone of " + existingVPXFile.getName() + " is created into subfolder \"" + targetSubFolder.getAbsolutePath() + "\"");
    }
    else {
      target = FileUtils.uniqueFile(target);
      fileName = target.getName();
    }

    //copy file
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, target);
    LOG.info("Copied temporary VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" to target \"" + target.getAbsolutePath() + "\"");


    int returningGameId = frontendService.importGame(target, true, -1, gameEmulator.getId());
    if (returningGameId >= 0) {
      Game importedGame = gameService.scanGame(returningGameId);
      Game original = gameService.getGame(uploadDescriptor.getGameId());

      //update table details after new entry creation
      TableDetails tableDetailsClone = getTableDetails(returningGameId);
      tableDetailsClone.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too
      tableDetailsClone.setGameFileName(fileName);
      tableDetailsClone.setGameDisplayName(original.getGameDisplayName() + " (cloned)");
      tableDetailsClone.setGameName(importedGame.getGameName()); //update the game name since this has changed
      TableDataUtil.setMappedFieldValue(tableDetails, serverSettings.getMappingPatchVersion(), uploadDescriptor.getPatchVersion());

      saveTableDetails(tableDetailsClone, returningGameId, false);
      frontendService.updateTableFileUpdated(returningGameId);
      LOG.info("Created database clone entry with game name \"" + tableDetailsClone.getGameName() + "\"");

      //clone media
      LOG.info("Cloning assets from game name \"" + original.getGameName() + "\" to \"" + importedGame.getGameName() + "\"");
      cloneGameMedia(original, importedGame);

      //clone additional files
      FileUtils.cloneFile(original.getDirectB2SFile(), targetSubFolder, target.getName());
      FileUtils.cloneFile(original.getPOVFile(), targetSubFolder, target.getName());
      FileUtils.cloneFile(original.getIniFile(), targetSubFolder, target.getName());
      FileUtils.cloneFile(original.getResFile(), targetSubFolder, target.getName());

      if (uploadDescriptor.isAutoFill()) {
        autoMatch(importedGame, true, false);
      }

      tableDetails = getTableDetails(importedGame.getId());
      if (tableDetails != null && autoFill) {
        frontendService.autoFill(importedGame, tableDetails, false);
      }
      else {
        frontendService.vpsLink(importedGame.getId(), original.getExtTableId(), original.getExtTableVersionId());
      }
      LOG.info("Cloning of \"" + importedGame.getGameDisplayName() + "\" successful.");
    }

    //update the game id to the new table
    uploadDescriptor.setGameId(returningGameId);
  }

  public void cloneGameMedia(Game original, Game clone) {
    VPinScreen[] values = VPinScreen.values();
    for (VPinScreen originalScreenValue : values) {
      try {
        List<FrontendMediaItem> frontendMediaItems = frontendService.getGameMedia(original).getMediaItems(originalScreenValue);
        for (FrontendMediaItem frontendMediaItem : frontendMediaItems) {
          if (frontendMediaItem.getFile().exists()) {
            File mediaFile = frontendMediaItem.getFile();
            String suffix = FilenameUtils.getExtension(mediaFile.getName());
            File cloneTarget = new File(frontendService.getMediaFolder(clone, originalScreenValue, suffix, true), clone.getGameName() + "." + suffix);
            if (mediaFile.getName().equals(cloneTarget.getName())) {
              LOG.warn("Source name and target name of media asset " + mediaFile.getAbsolutePath() + " are identical, skipping cloning.");
              return;
            }

            if (cloneTarget.exists() && !cloneTarget.delete()) {
              LOG.error("Failed to clone media asset " + cloneTarget.getAbsolutePath() + ": deletion of existing asset failed.");
              return;
            }
            org.apache.commons.io.FileUtils.copyFile(mediaFile, cloneTarget);
            LOG.info("Cloned media asset: " + mediaFile.getAbsolutePath() + " to " + cloneTarget.getAbsolutePath());
          }
        }
      }
      catch (IOException e) {
        LOG.info("Failed to clone media asset: " + e.getMessage(), e);
      }
    }
  }

  public void renameGameMedia(Game game, String oldBaseName, String newBaseName) {
    VPinScreen[] values = VPinScreen.values();
    int assetRenameCounter = 0;
    for (VPinScreen screen : values) {
      List<FrontendMediaItem> frontendMediaItems = frontendService.getGameMedia(game).getMediaItems(screen);
      for (FrontendMediaItem frontendMediaItem : frontendMediaItems) {
        File gameMediaFile = frontendMediaItem.getFile();
        if (gameMediaFile.exists()) {
          if (screen.equals(VPinScreen.Wheel)) {
            new WheelAugmenter(gameMediaFile).deAugment();
            new WheelIconDelete(gameMediaFile).delete();
          }

          if (de.mephisto.vpin.restclient.util.FileUtils.assetRename(gameMediaFile, oldBaseName, newBaseName)) {
            assetRenameCounter++;
            LOG.info("[" + screen + "] Renamed media asset from \"" + gameMediaFile.getName() + "\" to name \"" + newBaseName + "\"");
          }
          else {
            LOG.warn("[" + screen + "] Renaming media asset from \"" + gameMediaFile.getName() + "\" to name \"" + newBaseName + "\" failed.");
          }
        }
      }
    }
    LOG.info("Finished asset renaming for \"" + oldBaseName + "\" to \"" + newBaseName + "\", renamed " + assetRenameCounter + " assets.");
  }

  public void installMediaPack(@NonNull UploadDescriptor uploadDescriptor, @NonNull UploaderAnalysis analysis) throws Exception {
    File tempFile = new File(uploadDescriptor.getTempFilename());

    Game game = frontendService.getOriginalGame(uploadDescriptor.getGameId());
    List<VPinScreen> values = frontendService.getFrontend().getSupportedScreens();
    for (VPinScreen screen : values) {
      List<String> filesForScreen = analysis.getPopperMediaFiles(screen);

      int maxAssets = 3;
      for (String mediaFile : filesForScreen) {
        if (mediaFile.toLowerCase().contains("macosx")) {
          continue;
        }

        String suffix = FilenameUtils.getExtension(mediaFile);
        File out = uniqueMediaAsset(game, screen, suffix);
        if (uploadDescriptor.getUploadType() != null && uploadDescriptor.getUploadType().equals(UploadType.uploadAndReplace)) {
          out = new File(frontendService.getMediaFolder(game, screen, suffix, true), game.getGameName() + "." + suffix);
          if (out.exists() && !out.delete()) {
            out = uniqueMediaAsset(game, screen, suffix);
          }
        }

        if (PackageUtil.unpackTargetFile(tempFile, out, mediaFile)) {
          LOG.info("Created \"" + out.getAbsolutePath() + "\" for screen \"" + screen.name() + "\" from archive file \"" + mediaFile + "\"");
        }
        else {
          LOG.error("Failed to unpack " + out.getAbsolutePath() + " from " + tempFile.getAbsolutePath());
        }

        maxAssets--;
        if (maxAssets == 0) {
          break;
        }
      }
    }
  }

  public boolean deleteGameFile(int emulatorId, String fileName) {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
    if (gameEmulator != null) {
      File gameFile = new File(gameEmulator.getGamesDirectory(), fileName);
      if (gameFile.exists() && gameFile.delete()) {
        LOG.info("Delete game file {}", gameFile.getAbsolutePath());
        return true;
      }
    }
    return false;
  }

  public boolean deleteGame(@NonNull DeleteDescriptor descriptor) {
    LOG.info("************* Game Deletion ************");
    boolean success = false;
    try {
      List<Integer> gameIds = descriptor.getGameIds();
      success = true;

      for (Integer gameId : gameIds) {
        Game game = gameService.getGame(gameId);
        if (game == null) {
          return false;
        }

        if (descriptor.isDeleteHighscores()) {
          highscoreService.deleteHighscore(game);
        }

        if (descriptor.isDeleteTable()) {
          if (!FileUtils.delete(game.getGameFile())) {
            success = false;
          }
        }

        if (descriptor.isDeleteDirectB2s()) {
          if (!defaultPictureService.deleteAllPictures(game)) {
            success = false;
          }
          if (!FileUtils.delete(game.getDirectB2SFile())) {
            success = false;
          }
        }

        if (descriptor.isDeleteIni()) {
          if (!FileUtils.delete(game.getIniFile())) {
            success = false;
          }
        }

        if (descriptor.isDeleteBAMCfg()) {
          File BAMCfgFile = game.getBAMCfgFile();
          if (!FileUtils.delete(BAMCfgFile)) {
            success = false;
          }
        }

        if (descriptor.isDeleteRes()) {
          if (!FileUtils.delete(game.getResFile())) {
            success = false;
          }
        }

        if (descriptor.isDeleteVbs()) {
          if (!FileUtils.delete(game.getVBSFile())) {
            success = false;
          }
        }

        if (descriptor.isDeletePov()) {
          if (!FileUtils.delete(game.getPOVFile())) {
            success = false;
          }
        }

        if (descriptor.isDeletePupPack()) {
          if (!pupPacksService.delete(game)) {
            success = false;
          }
        }

        if (descriptor.isDeletePinVol()) {
          pinVolService.delete(game);
        }

        if (descriptor.isDeleteDMDs()) {
          DMDPackage dmdPackage = dmdService.getDMDPackage(game);
          if (dmdPackage != null) {
            if (!dmdService.delete(game)) {
              success = false;
            }
          }
        }

        if (descriptor.isDeleteAltSound()) {
          if (!altSoundService.delete(game)) {
            success = false;
          }
        }

        if (descriptor.isDeleteAltColor()) {
          if (!altColorService.delete(game)) {
            success = false;
          }
        }

        //cfg files belong to MAME
        if (descriptor.isDeleteCfg()) {
          if (!mameService.deleteCfg(game)) {
            success = false;
          }

          if (!StringUtils.isEmpty(game.getRom())) {
            if (!mameService.deleteOptions(game.getRom())) {
              success = false;
            }
          }
        }

        if (descriptor.isDeleteMusic()) {
          if (!musicService.delete(game)) {
            success = false;
          }
        }

        if (descriptor.isDeleteFromFrontend()) {
          GameDetails byPupId = gameDetailsRepository.findByPupId(game.getId());
          if (byPupId != null) {
            gameDetailsRepository.delete(byPupId);
          }

          highscoreService.deleteScores(game.getId(), true);

          Optional<Asset> byId = assetRepository.findByExternalId(String.valueOf(gameId));
          byId.ifPresent(asset -> assetRepository.delete(asset));

          if (!frontendService.deleteGame(gameId)) {
            success = false;
          }

          if (!descriptor.isKeepAssets()) {
            //only delete the assets, if there is no other game with the same "Game Name".
            List<Game> allOtherTables = this.frontendService.getGamesByEmulator(game.getEmulatorId())
                .stream().filter(g -> g.getId() != game.getId())
                .collect(Collectors.toList());
            List<Game> duplicateGameNameTables = allOtherTables
                .stream().filter(t -> t.getGameName().equalsIgnoreCase(game.getGameName()))
                .collect(Collectors.toList());

            if (duplicateGameNameTables.isEmpty()) {
              LOG.info("Deleting screen assets for \"" + game.getGameDisplayName() + "\"");
              VPinScreen[] values = VPinScreen.values();
              for (VPinScreen originalScreenValue : values) {
                List<FrontendMediaItem> frontendMediaItem = frontendService.getMediaItems(game, originalScreenValue);
                for (FrontendMediaItem mediaItem : frontendMediaItem) {
                  File mediaFile = mediaItem.getFile();

                  if (originalScreenValue.equals(VPinScreen.Wheel)) {
                    new WheelAugmenter(mediaFile).deAugment();
                    new WheelIconDelete(mediaFile).delete();
                  }

                  if (mediaFile.exists() && !mediaFile.delete()) {
                    success = false;
                    LOG.warn("Failed to delete media asset \"" + mediaFile.getAbsolutePath() + "\" for \"" + game.getGameDisplayName() + "\"");
                  }
                }
              }
            }
            else {
              LOG.info("Deletion of assets has been skipped, because there are " + duplicateGameNameTables.size() + " tables with the same GameName \"" + game.getGameName() + "\"");
            }
          }

          LOG.info("Deleted \"" + game.getGameDisplayName() + "\" from frontend.");
          gameLifecycleService.notifyGameDeleted(game.getId());
        }

        //delete the game folder if it is empty
        File gameFolder = game.getGameFile().getParentFile();
        if (gameFolder.exists() && !gameFolder.equals(game.getEmulator().getGamesFolder())) {
          String[] list = gameFolder.list();
          if (list == null || list.length == 0) {
            if (gameFolder.delete()) {
              LOG.info("Deleted table folder " + gameFolder.getAbsolutePath());
            }
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Game deletion failed: " + e.getMessage(), e);
    }
    LOG.info("*********** /Game Deletion End **********");
    return success;
  }

  public File uniqueMediaAsset(Game game, VPinScreen screen) {
    return buildMediaAsset(game, screen, true);
  }

  public File uniqueMediaAsset(Game game, VPinScreen screen, String suffix) {
    File mediaFolder = frontendService.getMediaFolder(game, screen, suffix, false);
    return buildMediaAsset(mediaFolder, game, suffix, true);
  }

  public File buildMediaAsset(Game game, VPinScreen screen, boolean append) {
    String suffix = "mp4";
    if (screen.equals(VPinScreen.AudioLaunch) || screen.equals(VPinScreen.Audio)) {
      suffix = "mp3";
    }
    File mediaFolder = frontendService.getMediaFolder(game, screen, suffix, false);
    return buildMediaAsset(mediaFolder, game, suffix, append);
  }

  public static File buildMediaAsset(File mediaFolder, Game game, String suffix, boolean append) {
    File out = new File(mediaFolder, game.getGameName() + "." + suffix);
    if (append) {
      int index = 1;
      while (out.exists()) {
        String nameIndex = index <= 9 ? "0" + index : String.valueOf(index);
        out = new File(out.getParentFile(), game.getGameName() + nameIndex + "." + suffix);
        index++;
      }
    }
    return out;
  }
}
