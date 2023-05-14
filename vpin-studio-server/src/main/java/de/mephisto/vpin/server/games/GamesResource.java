package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.restclient.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.descriptors.ResetHighscoreDescriptor;
import de.mephisto.vpin.restclient.descriptors.TableUploadDescriptor;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
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
  private SystemService systemService;

  @Autowired
  private PopperService popperService;

  @GetMapping
  public List<Game> getGame() {
    return gameService.getGames();
  }

  @GetMapping("/count")
  public int getGameCount() {
    return gameService.getGameCount();
  }

  @GetMapping("/ids")
  public List<Integer> getGameId() {
    return gameService.getGameId();
  }

  @GetMapping("/recent/{count}")
  public ScoreSummary getRecentHighscores(@PathVariable("count") int count) {
    return gameService.getRecentHighscores(count);
  }

  @GetMapping("/scoredgames")
  public List<Game> getGamesWithScore() {
    return gameService.getGamesWithScore();
  }

  @GetMapping("/{id}")
  public Game getGame(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game;
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

  @PostMapping("/reset")
  public boolean reset(@RequestBody ResetHighscoreDescriptor descriptor) {
    return gameService.resetGame(descriptor.getGameId());
  }

  @PostMapping("/save")
  public Game save(@RequestBody Game game) throws Exception {
    return gameService.save(game);
  }

  @GetMapping("/rom/{rom}")
  public List<Game> findByRom(@PathVariable("rom") String rom) {
    return gameService.getGamesByRom(rom);
  }

  @PostMapping("/upload/rom")
  public Boolean uploadRom(@RequestParam(value = "file", required = false) MultipartFile file) {
    try {
      if (file == null) {
        LOG.error("Rom upload request did not contain a file object.");
        return false;
      }
      File out = new File(systemService.getMameRomFolder(), file.getOriginalFilename());
      return UploadUtil.upload(file, out);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ROM upload failed: " + e.getMessage());
    }
  }

  @PostMapping("/upload/table")
  public Boolean uploadTable(@RequestParam(value = "file") MultipartFile file,
                             @RequestParam(value = "gameId") int gameId,
                             @RequestParam(value = "mode") String modeString) {
    try {
      if (file == null) {
        LOG.error("Table upload request did not contain a file object.");
        return false;
      }
      File uploadFile = new File(systemService.getVPXTablesFolder(), file.getOriginalFilename());
      uploadFile = FileUtils.uniqueFile(uploadFile);

      TableUploadDescriptor mode = TableUploadDescriptor.valueOf(modeString);
      if (gameId > 0) {
        if (mode.equals(TableUploadDescriptor.uploadAndReplace)) {
          if (!uploadFile.delete()) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table upload failed: existing table could not be deleted.");
          }
        }
        else if (mode.equals(TableUploadDescriptor.uploadAndClone)) {
        }
      }

      if (UploadUtil.upload(file, uploadFile)) {
        switch (mode) {
          case upload: {
            //nothing, we are done here
            return true;
          }
          case uploadAndImport: {
            int importedGameId = popperService.importVPXGame(uploadFile, true, -1);
            if (importedGameId >= 0) {
              gameService.scanGame(importedGameId);
            }
            return true;
          }
          case uploadAndReplace: {
            //we only have to rename the filename for the selected entry with the new filename
            TableDetails tableDetails = popperService.getTableDetails(gameId);
            tableDetails.setGameFileName(uploadFile.getName());
            popperService.saveTableDetails(tableDetails, gameId);

            gameService.scanGame(gameId);
            break;
          }
          case uploadAndClone: {
            int importedGameId = popperService.importVPXGame(uploadFile, true, -1);
            if (importedGameId >= 0) {
              Game importedGame = gameService.scanGame(importedGameId);

              //update table details after new entry creation
              TableDetails tableDetails = popperService.getTableDetails(gameId);
              tableDetails.setGameFileName(uploadFile.getName());
              tableDetails.setGameDisplayName(FilenameUtils.getBaseName(uploadFile.getName()));
              tableDetails.setGameName(FilenameUtils.getBaseName(uploadFile.getName()));
              popperService.saveTableDetails(tableDetails, importedGameId);

              //clone popper media
              Game original = getGame(gameId);
              popperService.cloneGameMedia(original, importedGame);
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
