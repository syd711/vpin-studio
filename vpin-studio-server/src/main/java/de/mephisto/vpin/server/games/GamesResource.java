package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.tables.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.tables.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.util.UploadUtil;
import de.mephisto.vpin.server.util.ZipUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Collections;
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

  @GetMapping
  public List<Game> getGames() {
    return gameService.getGames();
  }

  @GetMapping("/ids")
  public List<Integer> getGameIds() {
    return gameService.getGameIds();
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

  @GetMapping("/{id}")
  public Game getGame(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game;
  }

  @GetMapping("/validations/rom/{id}")
  public List<ValidationState> getRomValidations(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      return Collections.emptyList();
    }
    return gameService.getRomValidations(game);
  }

  @GetMapping("/validations/{id}")
  public List<ValidationState> getAllValidations(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      return Collections.emptyList();
    }
    return gameService.validate(game);
  }

  @GetMapping("/scores/{id}")
  public ScoreSummary getScores(@PathVariable("id") int id) {
    return gameService.getScores(id);
  }

  @GetMapping("/scorehistory/{id}")
  public ScoreList getScoreHistory(@PathVariable("id") int id) {
    return gameService.getScoreHistory(id);
  }

  @GetMapping("/scan/{id}")
  public Game scanGame(@PathVariable("id") int pupId) {
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

      TableUploadDescriptor mode = TableUploadDescriptor.valueOf(modeString);
      if (gameId > 0) {
        Game game = gameService.getGame(gameId);
        File gameFile = game.getGameFile();

        if (mode.equals(TableUploadDescriptor.uploadAndReplace)) {
          if (!gameFile.delete()) {
            LOG.error("Table upload failed: existing table could not be deleted.");
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table upload failed: existing table could not be deleted.");
          }
        }
        else if (mode.equals(TableUploadDescriptor.uploadAndClone)) {
        }
      }

      //determine the target file depending on the selected emulator
      GameEmulator gameEmulator = popperService.getGameEmulator(emuId);
      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());


      File uploadFile = null;
      if (suffix.equalsIgnoreCase("zip")) {
        try {
          File tempFile = File.createTempFile(FilenameUtils.getBaseName(file.getOriginalFilename()), "." + suffix);
          UploadUtil.upload(file, tempFile);
          String name = ZipUtil.contains(tempFile, ".vpx");
          File originalFile = new File(gameEmulator.getTablesFolder(), name);
          uploadFile = FileUtils.uniqueFile(originalFile);
          ZipUtil.unzipTargetFile(tempFile, uploadFile, name);
        } catch (Exception e) {
          LOG.error("Upload of zip vpx file failed: " + e.getMessage(), e);
          throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Upload of zipped VPX file failed: " + e.getMessage());
        }
      }
      else {
        File originalFile = new File(gameEmulator.getTablesFolder(), file.getOriginalFilename());
        uploadFile = FileUtils.uniqueFile(originalFile);
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
                popperService.autofillTableDetails(game, false);
              }
            }
            return true;
          }
          case uploadAndReplace: {
            //the game file has already been deleted at this point
            String originalFilename = FilenameUtils.getBaseName(file.getOriginalFilename());
            TableDetails tableDetails = popperService.getTableDetails(gameId);
            tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too
            tableDetails.setGameFileName(uploadFile.getName());
            tableDetails.setGameDisplayName(originalFilename);
            tableDetails.setGameVersion(""); //reset version to re-apply the newer one
            popperService.saveTableDetails(tableDetails, gameId);

            Game game = gameService.scanGame(gameId);
            if (game != null) {
              popperService.autofillTableDetails(game, false);
            }
            break;
          }
          case uploadAndClone: {
            int importedGameId = popperService.importVPXGame(uploadFile, true, -1, gameEmulator.getId());
            if (importedGameId >= 0) {
              String originalName = FilenameUtils.getBaseName(file.getOriginalFilename());
              Game importedGame = gameService.scanGame(importedGameId);

              //update table details after new entry creation
              TableDetails tableDetails = popperService.getTableDetails(gameId);
              tableDetails.setEmulatorId(gameEmulator.getId()); //update emulator id in case it has changed too
              tableDetails.setGameFileName(uploadFile.getName());
              tableDetails.setGameDisplayName(originalName);
              tableDetails.setGameName(originalName);
              tableDetails.setGameVersion(""); //reset version to re-apply the newer one
              popperService.saveTableDetails(tableDetails, importedGameId);
              if (importedGame != null) {
                popperService.autofillTableDetails(importedGame, false);
              }

              //clone popper media
              Game original = getGame(gameId);
              popperService.cloneGameMedia(original, importedGame);

              //clone additional files
              FileUtils.cloneFile(original.getDirectB2SFile(), uploadFile.getName());
              FileUtils.cloneFile(original.getPOVFile(), uploadFile.getName());
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
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table upload failed: " + e.getMessage());
    }
    return false;
  }
}
