package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameDetailsRepresentation;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.util.PackageUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import de.mephisto.vpin.server.vps.VpsService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class GamesResource {
  private final static Logger LOG = LoggerFactory.getLogger(GamesResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private PreferencesService preferenceService;

  @Autowired
  private GameFilterService gameFilterService;

  @GetMapping
  public List<Game> getGames() {
    return gameService.getGames();
  }

  @PostMapping("/filter")
  public List<Integer> getGamesFiltered(@RequestBody FilterSettings filterSettings) {
    return gameFilterService.filterGames(gameService, filterSettings);
  }

  @GetMapping("/ids")
  public List<Integer> getGameIds() {
    return gameService.getGameIds();
  }

  @GetMapping("/reload")
  public boolean reload() {
    return gameService.reload();
  }

  @GetMapping("/unknowns")
  public List<Integer> getUnknownGameIds() {
    return gameService.getUnknownGames();
  }

  @GetMapping("/knowns")
  public List<Game> getKnownGames() {
    return gameService.getKnownGames();
  }

  @GetMapping("/recent/{count}")
  public ScoreSummary getRecentHighscores(@PathVariable("count") int count) {
    return gameService.getRecentHighscores(count);
  }

  @GetMapping("/recent/{count}/{gameId}")
  public ScoreSummary getRecentHighscoresForGame(@PathVariable("count") int count, @PathVariable("gameId") int gameId) {
    return gameService.getRecentHighscores(count, gameId);
  }

  @GetMapping("/{id}")
  public Game getGame(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game;
  }

  @GetMapping("/details/{id}")
  public GameDetailsRepresentation getGameDetails(@PathVariable("id") int id) {
    return gameService.getGameDetails(id);
  }

  @GetMapping("/validations/{id}")
  public List<ValidationState> getAllValidations(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      return Collections.emptyList();
    }
    return gameService.validate(game);
  }

  @GetMapping("/scorevalidation/{id}")
  public GameScoreValidation getGameScoreValidation(@PathVariable("id") int id) {
    return gameService.getGameScoreValidation(id);
  }

  @GetMapping("/scores/{id}")
  public ScoreSummary getScores(@PathVariable("id") int id) {
    return gameService.getScores(id);
  }

  @GetMapping("/highscorefiles/{id}")
  public HighscoreFiles getHighscoreFiles(@PathVariable("id") int id) {
    return gameService.getHighscoreFiles(id);
  }

  @GetMapping("/scorehistory/{id}")
  public ScoreList getScoreHistory(@PathVariable("id") int id) {
    return gameService.getScoreHistory(id);
  }

  @GetMapping("/scan/{id}")
  public Game scanGame(@PathVariable("id") int pupId) {
    LOG.info("Client initiated game scan for " + pupId);
    return gameService.scanGame(pupId);
  }

  @GetMapping("/scanscore/{id}")
  public HighscoreMetadata scanGameScore(@PathVariable("id") int pupId) {
    return gameService.scanScore(pupId);
  }

  @PostMapping("/delete")
  public boolean delete(@RequestBody DeleteDescriptor descriptor) {
    return gameService.deleteGame(descriptor);
  }

  @DeleteMapping("/reset/{gameId}")
  public boolean reset(@PathVariable("gameId") int gameId) {
    return gameService.resetGame(gameId);
  }

  @PostMapping("/save")
  public Game save(@RequestBody Game game) throws Exception {
    return gameService.save(game);
  }

  @PostMapping("/upload/rom/{emuId}")
  public Boolean uploadRom(@PathVariable("emuId") int emuId, @RequestParam(value = "file", required = false) MultipartFile file) {
    try {
      if (file == null) {
        LOG.error("Rom upload request did not contain a file object.");
        return false;
      }
      GameEmulator gameEmulator = popperService.getGameEmulator(emuId);
      File out = new File(gameEmulator.getRomFolder(), file.getOriginalFilename());
      return UploadUtil.upload(file, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ROM upload failed: " + e.getMessage());
    }
  }


  @PostMapping("/upload/table")
  public Boolean uploadTable(@RequestParam(value = "file") MultipartFile file,
                             @RequestParam(value = "gameId") int gameId,
                             @RequestParam(value = "emuId") int emuId,
                             @RequestParam(value = "mode") String modeString) {
    try {
      if (file == null) {
        LOG.error("Table upload request did not contain a file object.");
        return false;
      }

      String originalFilename = file.getOriginalFilename();
      ServerSettings serverSettings = preferenceService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
      boolean keepExistingFilename = serverSettings.isVpxKeepFileNames();
      boolean keepExistingDisplayName = serverSettings.isVpxKeepDisplayNames();

      //determine the target file depending on the selected emulator
      GameEmulator gameEmulator = popperService.getGameEmulator(emuId);
      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());

      TableUploadDescriptor mode = TableUploadDescriptor.valueOf(modeString);
      if (gameId > 0) {
        Game game = gameService.getGame(gameId);
        File gameFile = game.getGameFile();

        if (mode.equals(TableUploadDescriptor.uploadAndReplace)) {
          if (gameFile.exists()) {
            //check if backup should be created
            if (serverSettings.isBackupTableOnOverwrite()) {
              File tableBackupsFolder = gameEmulator.getTableBackupsFolder();
              tableBackupsFolder.mkdirs();
              String format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
              File backup = new File(tableBackupsFolder, FilenameUtils.getBaseName(gameFile.getName()) + "[" + format + "].vpx");
              org.apache.commons.io.FileUtils.copyFile(gameFile, backup);
              LOG.info("Created backup VPX file \"" + backup.getAbsolutePath() + "\"");
            }

            if (!gameFile.delete()) {
              LOG.error("Table upload failed: existing table could not be deleted.");
              throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table upload failed: existing table could not be deleted.");
            }
            else {
              LOG.info("Deleted existing game file \"" + gameFile.getAbsolutePath() + "\"");
            }
          }

          if (keepExistingFilename) {
            originalFilename = gameFile.getName();
          }
        }
        else if (mode.equals(TableUploadDescriptor.uploadAndClone)) {
        }
      }


      File uploadFile = null;
      if (suffix != null && (suffix.equalsIgnoreCase("zip") || (suffix.equalsIgnoreCase("rar")))) {
        try {
          File temporaryArchive = File.createTempFile(FilenameUtils.getBaseName(originalFilename), "." + suffix);
          UploadUtil.upload(file, temporaryArchive);
          String fileNameToExtract = PackageUtil.contains(temporaryArchive, ".vpx");
          if (fileNameToExtract == null) {
            throw new IOException("No VPX file found in archive file " + temporaryArchive.getAbsolutePath());
          }
          File targetFile = new File(gameEmulator.getTablesFolder(), fileNameToExtract);
          if (keepExistingFilename) {
            uploadFile = new File(gameEmulator.getTablesFolder(), FilenameUtils.getBaseName(originalFilename) + ".vpx");
            LOG.info("Kept existing filename \"" + uploadFile.getAbsolutePath() + "\"");
          }
          else {
            uploadFile = FileUtils.uniqueFile(targetFile);
            LOG.info("New target file is \"" + uploadFile.getAbsolutePath() + "\"");
          }
          LOG.info("Extracting archive file \"" + fileNameToExtract + "\" to \"" + uploadFile.getAbsolutePath() + "\"");
          uploadFile.getParentFile().mkdirs();
          PackageUtil.unpackTargetFile(temporaryArchive, uploadFile, fileNameToExtract);
        } catch (Exception e) {
          LOG.error("Upload of vpx archive file failed: " + e.getMessage(), e);
          throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Upload of packaged VPX file failed: " + e.getMessage());
        }
      }
      else {
        File originalFile = new File(gameEmulator.getTablesFolder(), originalFilename);
        uploadFile = FileUtils.uniqueFile(originalFile);
        uploadFile.getParentFile().mkdirs();
        UploadUtil.upload(file, uploadFile);
      }

      if (uploadFile.exists()) {
        switch (mode) {
          case upload: {
            //nothing, we are done here
            return true;
          }
          case uploadAndImport: {
            int importedGameId = popperService.importVPXGame(uploadFile, true, -1, gameEmulator.getId());
            if (importedGameId >= 0) {
              Game game = gameService.scanGame(importedGameId);
              if (game != null) {
                TableDetails tableDetails = vpsService.autoMatch(game, true);
                if (tableDetails != null) {
                  popperService.autoFill(game, tableDetails, true, false);
                }
              }
            }
            return true;
          }
          case uploadAndReplace: {
            //the game file has already been deleted at this point
            TableDetails tableDetails = popperService.getTableDetails(gameId);
            tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too

            //the uploaded file name is already updated here
            File oldGameFile = new File(gameEmulator.getTablesDirectory(), tableDetails.getGameFileName());
            if (!oldGameFile.getParentFile().equals(uploadFile.getParentFile())) {
              if (oldGameFile.exists()) {
                if (oldGameFile.delete()) {
                  LOG.info("Deleted existing old game file \"" + oldGameFile.getAbsolutePath() + "\"");
                }
              }
              File movingTo = new File(oldGameFile.getParentFile(), uploadFile.getName());
              org.apache.commons.io.FileUtils.moveFile(uploadFile, movingTo);
              LOG.info("Moved \"" + uploadFile.getAbsolutePath() + "\" to \"" + movingTo.getAbsolutePath() + "\"");
            }

            //if the VPX file is inside a subfolder, we have to prepend the folder name
            String name = uploadFile.getName();
            String oldName = tableDetails.getGameFileName();
            if (oldName.contains("\\")) {
              name = oldName.substring(0, oldName.lastIndexOf("\\") + 1) + uploadFile.getName();
            }

            LOG.info("Updated new filename to \"" + name + "\"");
            tableDetails.setGameFileName(name);
            if (!keepExistingDisplayName) {
              tableDetails.setGameDisplayName(FilenameUtils.getBaseName(originalFilename));
            }
            popperService.saveTableDetails(tableDetails, gameId, !keepExistingFilename);
            popperService.updateTableFileUpdated(gameId);

            Game game = gameService.scanGame(gameId);
            if (game != null) {
              gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVPX);
              gameService.resetUpdate(game.getId(), VpsDiffTypes.tableNewVersionVPX);
            }
            break;
          }
          case uploadAndClone: {
            int importedGameId = popperService.importVPXGame(uploadFile, true, -1, gameEmulator.getId());
            if (importedGameId >= 0) {
              String originalName = FilenameUtils.getBaseName(file.getOriginalFilename());
              Game importedGame = gameService.scanGame(importedGameId);

              //update table details after new entry creation, but DO NOT TOUCH the game name
              TableDetails tableDetails = popperService.getTableDetails(gameId);
              tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too
              tableDetails.setGameFileName(uploadFile.getName());
              tableDetails.setGameDisplayName(originalName);
              popperService.saveTableDetails(tableDetails, importedGameId, false);
              popperService.updateTableFileUpdated(importedGameId);
              LOG.info("Created database clone entry with game name \"" + tableDetails.getGameName() + "\"");

              //clone popper media
              Game original = getGame(gameId);
              LOG.info("Cloning Popper assets from game name \"" + original.getGameName() + "\" to \"" + importedGame.getGameName() + "\"");
              popperService.cloneGameMedia(original, importedGame);

              //clone additional files
              FileUtils.cloneFile(original.getDirectB2SFile(), uploadFile.getName());
              FileUtils.cloneFile(original.getPOVFile(), uploadFile.getName());
              FileUtils.cloneFile(original.getIniFile(), uploadFile.getName());
              FileUtils.cloneFile(original.getResFile(), uploadFile.getName());
            }
            return true;
          }
          default: {
            //ignore
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table upload failed: " + e.getMessage());
    }
    return false;
  }
}
