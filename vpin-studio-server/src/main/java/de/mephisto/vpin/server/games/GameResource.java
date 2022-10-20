package de.mephisto.vpin.server.games;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class GameResource {

  @Autowired
  private GameService gameService;

  @GetMapping
  public List<Game> getGame() {
    return gameService.getGames();
  }

  @GetMapping("/{id}")
  public Game getGame(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if(game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game;
  }

  @PutMapping("/dismiss/{id}")
  public boolean put(@PathVariable("id") int id, @RequestBody Map<String,String> values) {
    Game game = gameService.getGame(id);
    return true;
  }

  @GetMapping("/scan/{id}")
  public boolean scanGame(@PathVariable("id") int pupId) {
    return gameService.scanGame(pupId);
  }

  @PostMapping("/save")
  public Game save(@RequestBody Game game) {
    return gameService.save(game);
  }
}
