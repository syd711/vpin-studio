package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.popper.PinUPConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class GameResource {

  @Autowired
  private PinUPConnector connector;

  @GetMapping
  public List<Game> getGame() {
    List<Game> games = connector.getGames();
    List<Game> mappedGames = new ArrayList<>();
    for (Game game : games) {
      Game mappedGame = connector.getGame(game.getId());
      if(mappedGame != null) {
        mappedGames.add(mappedGame);
      }
    }
    return mappedGames;
  }

  @GetMapping("/{id}")
  public Game getGame(@PathVariable("id") int pupId) {
    Game game = connector.getGame(pupId);
    if(game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + pupId);
    }
    return game;
  }
}
