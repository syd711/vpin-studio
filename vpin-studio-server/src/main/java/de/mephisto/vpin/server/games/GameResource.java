package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.util.PinUPConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/games")
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
    return connector.getGame(pupId);
  }
}
