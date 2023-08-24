package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.HighscoreBackup;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "highscorebackups")
public class HighscoreBackupResources {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreBackupResources.class);

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private GameService gameService;

  @GetMapping("{rom}")
  public List<HighscoreBackup> get(@PathVariable("rom") String rom) {
    return highscoreBackupService.getBackups(rom);
  }

  @PutMapping("backup/{gameId}")
  public boolean backup(@PathVariable("gameId") int gameId) {
    try {
      Game game = gameService.getGame(gameId);
      return highscoreBackupService.backup(game);
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Creating backup failed: " + e.getMessage());
    }
  }

  @PutMapping("restore/{rom}/{filename}")
  public boolean restore(@PathVariable("rom") String rom, @PathVariable("filename") String filename) {
    return highscoreBackupService.restore(rom, filename);
  }

  @DeleteMapping("/{rom}/{filename}")
  public boolean delete(@PathVariable("rom") String rom, @PathVariable("filename") String filename) {
    return highscoreBackupService.delete(rom, filename);
  }
}
