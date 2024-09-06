package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class UniversalUploadResource {
  private final static Logger LOG = LoggerFactory.getLogger(UniversalUploadResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private PreferencesService preferenceService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @PostMapping("/upload/table")
  public UploadDescriptor uploadTable(@RequestParam(value = "file") MultipartFile file,
                                      @RequestParam(value = "gameId") int gameId,
                                      @RequestParam(value = "emuId") int emuId,
                                      @RequestParam(value = "mode") TableUploadType mode) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file, gameId);
    try {
      descriptor.setUploadType(mode);
      descriptor.setEmulatorId(emuId);

      descriptor.upload();
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      descriptor.setError("Table upload failed: " + e.getMessage());
      descriptor.finalizeUpload();
    }
    return descriptor;
  }

  @PostMapping("/process/table")
  public UploadDescriptor processUploadedTable(@RequestBody UploadDescriptor uploadDescriptor) {
    Thread.currentThread().setName("Universal Upload Thread");
    long start = System.currentTimeMillis();
    LOG.info("*********** Importing " + uploadDescriptor.getTempFilename() + " ************************");
    try {
      // If the file is not a real file but a pointer to an external resource, it is time to get the real file...
      universalUploadService.resolveLinks(uploadDescriptor);

      File tempFile = new File(uploadDescriptor.getTempFilename());
      UploaderAnalysis analysis = new UploaderAnalysis<>(tempFile);
      analysis.analyze();

      String vpxFileName = analysis.getVpxFileName(uploadDescriptor.getOriginalUploadFileName());
      if (StringUtils.isEmpty(vpxFileName)) {
        throw new Exception("Failed to resolve VPX filename from " + uploadDescriptor.getOriginalUploadFileName());
      }

      File temporaryVPXFile = universalUploadService.writeTableFilenameBasedEntry(uploadDescriptor, vpxFileName);
      importVPXFile(temporaryVPXFile, uploadDescriptor, analysis);

      universalUploadService.importFileBasedAssets(uploadDescriptor, analysis, AssetType.DIRECTB2S);
      universalUploadService.importFileBasedAssets(uploadDescriptor, analysis, AssetType.POV);
      universalUploadService.importFileBasedAssets(uploadDescriptor, analysis, AssetType.INI);
      universalUploadService.importFileBasedAssets(uploadDescriptor, analysis, AssetType.RES);

      universalUploadService.importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.DMD_PACK);
      universalUploadService.importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.PUP_PACK);
      universalUploadService.importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.POPPER_MEDIA);
      universalUploadService.importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.ALT_SOUND);
      universalUploadService.importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.ALT_COLOR);
      universalUploadService.importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.MUSIC);
      universalUploadService.importArchiveBasedAssets(uploadDescriptor, analysis, AssetType.ROM);
    }
    catch (Exception e) {
      LOG.error("Processing \"" + uploadDescriptor.getTempFilename() + "\" failed: " + e.getMessage(), e);
      uploadDescriptor.setError("Processing failed: " + e.getMessage());
    }
    finally {
      uploadDescriptor.finalizeUpload();
      LOG.info("Import finished, took " + (System.currentTimeMillis() - start) + " ms.");
    }
    LOG.info("****************************** /Import Finished *************************************");
    return uploadDescriptor;
  }


  private void importVPXFile(File temporaryVPXFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    TableUploadType uploadType = uploadDescriptor.getUploadType();
    switch (uploadType) {
      case uploadAndImport: {
        uploadAndImport(temporaryVPXFile, uploadDescriptor, analysis);
        break;
      }
      case uploadAndReplace: {
        uploadAndReplace(temporaryVPXFile, uploadDescriptor, analysis);
        break;
      }
      case uploadAndClone: {
        uploadAndClone(temporaryVPXFile, uploadDescriptor, analysis);
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unmapped upload type " + uploadType);
      }
    }
  }

  private void uploadAndClone(File temporaryVPXFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    GameEmulator gameEmulator = frontendStatusService.getGameEmulator(uploadDescriptor.getEmulatorId());
    TableDetails tableDetails = frontendStatusService.getTableDetails(uploadDescriptor.getGameId());
    tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too

    boolean autoFill = uploadDescriptor.isAutoFill();

    File existingVPXFile = new File(gameEmulator.getTablesDirectory(), tableDetails.getGameFileName());
    if (!existingVPXFile.exists()) {
      throw new UnsupportedOperationException("The VPX file to clone \"" + tableDetails.getGameFileName() + "\" does not exist.");
    }

    //Determine target name
    File target = new File(existingVPXFile.getParentFile(), existingVPXFile.getName());
    String fileName = target.getName();
    if (uploadDescriptor.isFolderBasedImport()) {
      //use the parents parent so that we are back inside the tables folder
      File targetFolder = new File(gameEmulator.getTablesFolder(), uploadDescriptor.getSubfolderName());
      targetFolder = FileUtils.uniqueFolder(targetFolder);
      targetFolder.mkdirs();
      target = new File(targetFolder, target.getName());
      fileName = targetFolder.getName() + "\\" + target.getName();

      LOG.info("Clone of " + existingVPXFile.getName() + " is created into subfolder \"" + targetFolder.getAbsolutePath() + "\"");
    }
    else {
      target = FileUtils.uniqueFile(target);
      fileName = target.getName();
    }

    //copy file
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, target);
    LOG.info("Copied temporary VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" to target \"" + target.getAbsolutePath() + "\"");


    int returningGameId = frontendStatusService.importVPXGame(target, true, -1, gameEmulator.getId());
    if (returningGameId >= 0) {
      Game importedGame = gameService.scanGame(returningGameId);

      //update table details after new entry creation
      TableDetails tableDetailsClone = frontendStatusService.getTableDetails(returningGameId);
      tableDetailsClone.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too
      tableDetailsClone.setGameFileName(fileName);
      tableDetailsClone.setGameDisplayName(FilenameUtils.getBaseName(analysis.getVpxFileName(uploadDescriptor.getOriginalUploadFileName())));
      tableDetailsClone.setGameName(importedGame.getGameName()); //update the game name since this has changed

      frontendStatusService.saveTableDetails(tableDetailsClone, returningGameId, false);
      frontendStatusService.updateTableFileUpdated(returningGameId);
      LOG.info("Created database clone entry with game name \"" + tableDetailsClone.getGameName() + "\"");

      //clone media
      Game original = gameService.getGame(uploadDescriptor.getGameId());
      LOG.info("Cloning assets from game name \"" + original.getGameName() + "\" to \"" + importedGame.getGameName() + "\"");
      frontendStatusService.cloneGameMedia(original, importedGame);

      //clone additional files
      FileUtils.cloneFile(original.getDirectB2SFile(), target.getName());
      FileUtils.cloneFile(original.getPOVFile(), target.getName());
      FileUtils.cloneFile(original.getIniFile(), target.getName());
      FileUtils.cloneFile(original.getResFile(), target.getName());

      frontendStatusService.autoMatch(importedGame, true, false);

      tableDetails = frontendStatusService.getTableDetails(importedGame.getId());
      if (tableDetails != null && autoFill) {
        frontendStatusService.autoFill(importedGame, tableDetails, false);
      }
      LOG.info("Cloning of \"" + importedGame.getGameDisplayName() + "\" successful.");
    }

    //update the game id to the new table
    uploadDescriptor.setGameId(returningGameId);
  }

  private void uploadAndReplace(File temporaryVPXFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    GameEmulator gameEmulator = frontendStatusService.getGameEmulator(uploadDescriptor.getEmulatorId());
    if (gameEmulator == null) {
      throw new Exception("No emulator found for id " + uploadDescriptor.getEmulatorId() + " to replace table for.");
    }

    TableDetails tableDetails = frontendStatusService.getTableDetails(uploadDescriptor.getGameId());
    if (tableDetails == null) {
      throw new Exception("No table details found for the selected game to replace (ID: " + uploadDescriptor.getGameId() + ").");
    }
    tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too

    ServerSettings serverSettings = preferenceService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    boolean keepExistingFilename = serverSettings.isVpxKeepFileNames();
    boolean keepExistingDisplayName = serverSettings.isVpxKeepDisplayNames();
    boolean keepCopy = serverSettings.isBackupTableOnOverwrite();
    boolean autoFill = uploadDescriptor.isAutoFill();

    //create backup first and delete existing table
    File existingVPXFile = new File(gameEmulator.getTablesDirectory(), tableDetails.getGameFileName());
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
    File existingVbsFile = new File(gameEmulator.getTablesDirectory(), baseName + ".vbs");
    if (existingVbsFile.exists() && !existingVbsFile.delete()) {
      LOG.error("Failed to delete existing .vbs file \"" + existingVbsFile.getAbsolutePath() + "\"");
    }

    //Determine target name
    File target = new File(existingVPXFile.getParentFile(), existingVPXFile.getName());
    if (!keepExistingFilename) {
      String vpxFileName = analysis.getVpxFileName(uploadDescriptor.getOriginalUploadFileName());
      if (vpxFileName == null) {
        vpxFileName = uploadDescriptor.getOriginalUploadFileName();
      }
      target = new File(existingVPXFile.getParentFile(), vpxFileName);
      LOG.info("Resolved target VPX file \"" + target.getAbsolutePath() + "\"");
    }

    //copy file
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, target);
    LOG.info("Copied temporary VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" to target \"" + target.getAbsolutePath() + "\"");


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
      tableDetails.setGameDisplayName(FilenameUtils.getBaseName(analysis.getVpxFileName(uploadDescriptor.getOriginalUploadFileName())));
    }

    frontendStatusService.saveTableDetails(tableDetails, uploadDescriptor.getGameId(), !keepExistingFilename);
    frontendStatusService.updateTableFileUpdated(uploadDescriptor.getGameId());

    Game game = gameService.scanGame(uploadDescriptor.getGameId());
    if (game != null) {
      gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVPX);
      gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVersionVPX);

      frontendStatusService.autoMatch(game, true, false);

      tableDetails = frontendStatusService.getTableDetails(game.getId());
      if (tableDetails != null && autoFill) {
        frontendStatusService.autoFill(game, tableDetails, false);
      }
      LOG.info("Import of \"" + game.getGameDisplayName() + "\" successful.");
    }
  }

  private void uploadAndImport(File temporaryVPXFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    GameEmulator gameEmulator = frontendStatusService.getGameEmulator(uploadDescriptor.getEmulatorId());

    File tablesFolder = gameEmulator.getTablesFolder();
    if (uploadDescriptor.isFolderBasedImport()) {
      tablesFolder = new File(tablesFolder, uploadDescriptor.getSubfolderName());
    }
    File targetVPXFile = new File(tablesFolder, uploadDescriptor.getOriginalUploadFileName());

    for (String archiveSuffix : PackageUtil.ARCHIVE_SUFFIXES) {
      if (FilenameUtils.getExtension(uploadDescriptor.getTempFilename()).equalsIgnoreCase(archiveSuffix)) {
        targetVPXFile = new File(tablesFolder, analysis.getVpxFileName(uploadDescriptor.getOriginalUploadFileName()));
        break;
      }
    }
    targetVPXFile = FileUtils.uniqueFile(targetVPXFile);

    LOG.info("Resolve target VPX: " + targetVPXFile.getAbsolutePath());
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, targetVPXFile);
    LOG.info("Copied for import '" + temporaryVPXFile.getAbsolutePath() + "' to '" + targetVPXFile.getAbsolutePath() + "'");

    int returningGameId = frontendStatusService.importVPXGame(targetVPXFile, true, -1, gameEmulator.getId());
    if (returningGameId >= 0) {
      Game game = gameService.scanGame(returningGameId);
      if (game != null) {
        frontendStatusService.autoMatch(game, true, false);

        TableDetails tableDetails = frontendStatusService.getTableDetails(game.getId());
        if (tableDetails != null && uploadDescriptor.isAutoFill()) {
          frontendStatusService.autoFill(game, tableDetails, false);
        }

        uploadDescriptor.setGameId(returningGameId);
        LOG.info("Import of \"" + game.getGameDisplayName() + "\" successful.");
      }
    }
  }

}
