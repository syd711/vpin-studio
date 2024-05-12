package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.PackageUtil;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.util.UploadUtil;
import de.mephisto.vpin.server.vps.VpsService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class GamesUploadResource {
  private final static Logger LOG = LoggerFactory.getLogger(GamesUploadResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private PreferencesService preferenceService;

  @PostMapping("/upload/table")
  public TableUploadDescriptor uploadTable(@RequestParam(value = "file") MultipartFile file,
                                           @RequestParam(value = "gameId") int gameId,
                                           @RequestParam(value = "emuId") int emuId,
                                           @RequestParam(value = "mode") TableUploadType mode) {
    TableUploadDescriptor tableUploadDescriptor = new TableUploadDescriptor();
    File uploadTempFile = null;
    try {
      tableUploadDescriptor.setOriginalUploadFileName(file.getOriginalFilename());
      tableUploadDescriptor.setUploadType(mode);
      tableUploadDescriptor.setGameId(gameId);
      tableUploadDescriptor.setEmulatorId(emuId);

      String name = FilenameUtils.getBaseName(file.getOriginalFilename());
      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
      uploadTempFile = File.createTempFile(name, "." + suffix);

      tableUploadDescriptor.setTempFilename(uploadTempFile.getAbsolutePath());
      UploadUtil.upload(file, uploadTempFile);

      if (PackageUtil.isSupportedArchive(suffix)) {
        tableUploadDescriptor.setOriginalUploadedVPXFileName(PackageUtil.contains(uploadTempFile, ".vpx"));
      }
      else {
        tableUploadDescriptor.setOriginalUploadedVPXFileName(file.getOriginalFilename());
      }
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      tableUploadDescriptor.setError("Table upload failed: " + e.getMessage());
      if (uploadTempFile != null) {
        uploadTempFile.delete();
      }
    }

    return tableUploadDescriptor;
  }

  @PostMapping("/process/table")
  public TableUploadDescriptor processUploadedTable(@RequestBody TableUploadDescriptor uploadDescriptor) {
    LOG.info("*********** Importing " + uploadDescriptor.getOriginalUploadedVPXFileName() + " ************************");
    try {
      File temporaryVPXFile = resolveVPXFile(uploadDescriptor);
      importVPXFile(temporaryVPXFile, uploadDescriptor);
      //TODO process media
    }
    catch (Exception e) {
      LOG.error("Processing \"" + uploadDescriptor.getOriginalUploadedVPXFileName() + "\" failed: " + e.getMessage(), e);
      uploadDescriptor.setError("Processing \"" + uploadDescriptor.getOriginalUploadedVPXFileName() + "\" failed: " + e.getMessage());
    } finally {
      File tempFile = new File(uploadDescriptor.getTempFilename());
      if (!tempFile.delete()) {
        LOG.error("Failed to delete temporary upload table file \"" + tempFile.getAbsolutePath() + "\"");
      }
      else {
        LOG.info("Deleted temporary upload table file \"" + tempFile.getAbsolutePath() + "\"");
      }
    }
    LOG.info("****************************** /Import Finished *************************************");
    return uploadDescriptor;
  }

  private void importVPXFile(File temporaryVPXFile, TableUploadDescriptor uploadDescriptor) throws Exception {
    TableUploadType uploadType = uploadDescriptor.getUploadType();
    switch (uploadType) {
      case uploadAndImport: {
        uploadAndImport(temporaryVPXFile, uploadDescriptor);
        break;
      }
      case uploadAndReplace: {
        uploadAndReplace(temporaryVPXFile, uploadDescriptor);
        break;
      }
      case uploadAndClone: {
        uploadAndClone(temporaryVPXFile, uploadDescriptor);
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unmapped upload type " + uploadType);
      }
    }
  }

  private void uploadAndClone(File temporaryVPXFile, TableUploadDescriptor uploadDescriptor) throws Exception {
    GameEmulator gameEmulator = popperService.getGameEmulator(uploadDescriptor.getEmulatorId());
    TableDetails tableDetails = popperService.getTableDetails(uploadDescriptor.getGameId());
    tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too

    File existingVPXFile = new File(gameEmulator.getTablesDirectory(), tableDetails.getGameFileName());
    if (!existingVPXFile.exists()) {
      throw new UnsupportedOperationException("The VPX file to clone \"" + tableDetails.getGameFileName() + "\" does not exist.");
    }

    //Determine target name
    File target = new File(existingVPXFile.getParentFile(), existingVPXFile.getName());
    String popperFileName = target.getName();
    if (uploadDescriptor.isFolderBasedImport()) {
      //use the parents parent so that we are back inside the tables folder
      File targetFolder = new File(gameEmulator.getTablesFolder(), uploadDescriptor.getSubfolderName());
      targetFolder = FileUtils.uniqueFolder(targetFolder);
      targetFolder.mkdirs();
      target = new File(targetFolder, target.getName());
      popperFileName = targetFolder.getName() + "\\" + target.getName();

      LOG.info("Clone of " + existingVPXFile.getName() + " is created into subfolder \"" + targetFolder.getAbsolutePath() + "\"");
    }
    else {
      target = FileUtils.uniqueFile(target);
    }

    //copy file
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, target);
    LOG.info("Copied temporary VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" to target \"" + target.getAbsolutePath() + "\"");


    int returningGameId = popperService.importVPXGame(target, true, -1, gameEmulator.getId());
    if (returningGameId >= 0) {
      Game importedGame = gameService.scanGame(returningGameId);

      //update table details after new entry creation
      TableDetails tableDetailsClone = popperService.getTableDetails(returningGameId);
      tableDetailsClone.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too
      tableDetailsClone.setGameFileName(popperFileName);
      tableDetailsClone.setGameDisplayName(FilenameUtils.getBaseName(uploadDescriptor.getOriginalUploadedVPXFileName()));
      tableDetailsClone.setGameName(importedGame.getGameName()); //update the game name since this has changed

      popperService.saveTableDetails(tableDetailsClone, returningGameId, false);
      popperService.updateTableFileUpdated(returningGameId);
      LOG.info("Created database clone entry with game name \"" + tableDetailsClone.getGameName() + "\"");

      //clone popper media
      Game original = gameService.getGame(uploadDescriptor.getGameId());
      LOG.info("Cloning Popper assets from game name \"" + original.getGameName() + "\" to \"" + importedGame.getGameName() + "\"");
      popperService.cloneGameMedia(original, importedGame);

      //clone additional files
      FileUtils.cloneFile(original.getDirectB2SFile(), target.getName());
      FileUtils.cloneFile(original.getPOVFile(), target.getName());
      FileUtils.cloneFile(original.getIniFile(), target.getName());
      FileUtils.cloneFile(original.getResFile(), target.getName());
    }

    //update the game id to the new table
    uploadDescriptor.setGameId(returningGameId);
  }

  private void uploadAndReplace(File temporaryVPXFile, TableUploadDescriptor uploadDescriptor) throws Exception {
    GameEmulator gameEmulator = popperService.getGameEmulator(uploadDescriptor.getEmulatorId());
    TableDetails tableDetails = popperService.getTableDetails(uploadDescriptor.getGameId());
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
      }
    }
    else {
      LOG.warn("VPX file to overwrite \"" + existingVPXFile.getAbsolutePath() + "\" was not found.");
    }

    //Determine target name
    File target = new File(existingVPXFile.getParentFile(), existingVPXFile.getName());
    if (!keepExistingFilename) {
      target = new File(existingVPXFile.getParentFile(), uploadDescriptor.getOriginalUploadedVPXFileName());
    }

    //copy file
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, target);
    LOG.info("Copied temporary VPX file \"" + temporaryVPXFile.getAbsolutePath() + "\" to target \"" + target.getAbsolutePath() + "\"");

    //update Popper table database entry
    //if the VPX file is inside a subfolder, we have to prepend the folder name
    String name = target.getName();
    String oldName = tableDetails.getGameFileName();
    if (oldName.contains("\\")) {
      name = oldName.substring(0, oldName.lastIndexOf("\\") + 1) + target.getName();
    }

    LOG.info("Updated database filename to \"" + name + "\"");
    tableDetails.setGameFileName(name);
    if (!keepExistingDisplayName) {
      tableDetails.setGameDisplayName(FilenameUtils.getBaseName(uploadDescriptor.getOriginalUploadedVPXFileName()));
    }

    popperService.saveTableDetails(tableDetails, uploadDescriptor.getGameId(), !keepExistingFilename);
    popperService.updateTableFileUpdated(uploadDescriptor.getGameId());

    Game game = gameService.scanGame(uploadDescriptor.getGameId());
    if (game != null) {
      gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVPX);
      gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVersionVPX);

      tableDetails = vpsService.autoMatch(game, true);
      if (tableDetails != null && autoFill) {
        popperService.autoFill(game, tableDetails, true, false);
      }
      LOG.info("Import of \"" + game.getGameDisplayName() + "\" successful.");
    }
  }

  private void uploadAndImport(File temporaryVPXFile, TableUploadDescriptor uploadDescriptor) throws Exception {
    GameEmulator gameEmulator = popperService.getGameEmulator(uploadDescriptor.getEmulatorId());

    File tablesFolder = gameEmulator.getTablesFolder();
    if (uploadDescriptor.isFolderBasedImport()) {
      tablesFolder = new File(tablesFolder, uploadDescriptor.getSubfolderName());
    }
    File targetVPXFile = new File(tablesFolder, uploadDescriptor.getOriginalUploadedVPXFileName());
    targetVPXFile = FileUtils.uniqueFile(targetVPXFile);

    LOG.info("Resolve target VPX: " + targetVPXFile.getAbsolutePath());
    org.apache.commons.io.FileUtils.copyFile(temporaryVPXFile, targetVPXFile);
    LOG.info("Copied '" + temporaryVPXFile.getAbsolutePath() + "' to '" + targetVPXFile.getAbsolutePath() + "'");

    int returningGameId = popperService.importVPXGame(targetVPXFile, true, -1, gameEmulator.getId());
    if (returningGameId >= 0) {
      Game game = gameService.scanGame(returningGameId);
      if (game != null) {
        TableDetails tableDetails = vpsService.autoMatch(game, true);
        if (tableDetails != null && uploadDescriptor.isAutoFill()) {
          popperService.autoFill(game, tableDetails, true, false);
        }
        uploadDescriptor.setGameId(returningGameId);
        LOG.info("Import of \"" + game.getGameDisplayName() + "\" successful.");
      }
    }
  }

  private File resolveVPXFile(TableUploadDescriptor tableUploadDescriptor) throws IOException {
    File tempFile = new File(tableUploadDescriptor.getTempFilename());
    if (PackageUtil.isSupportedArchive(tempFile.getName())) {
      String vpxFile = PackageUtil.contains(tempFile, ".vpx");
      File tempVXPFile = File.createTempFile(FilenameUtils.getBaseName(tableUploadDescriptor.getOriginalUploadedVPXFileName()), ".vpx");
      PackageUtil.unpackTargetFile(tempVXPFile, tempVXPFile, vpxFile);
      return tempVXPFile;
    }
    return tempFile;
  }
}
