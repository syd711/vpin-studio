package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.jpa.GameDetails;
import de.mephisto.vpin.server.jpa.GameDetailsRepository;
import de.mephisto.vpin.server.util.SqliteConnector;
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
  private SqliteConnector connector;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

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
    GameDetails details = gameDetailsRepository.findByPupId(pupId);
//
//    String rom = romScanner.getRomName(id);
//    File romFile = null;
//    File nvRamFile = null;
//    File nvRamFolder = new File(systemInfo.getMameFolder(), "nvram");
//    if (!StringUtils.isEmpty(rom)) {
//      romFile = new File(systemInfo.getMameRomFolder(), rom + ".zip");
//      nvRamFile = new File(nvRamFolder, rom + ".nv");
//    }
//    else if (!romScanner.wasScanned(id)) {
//      rom = romScanner.scanRom(info);
//    }
//
//    info.setRom(rom);
    return game;
  }
}
