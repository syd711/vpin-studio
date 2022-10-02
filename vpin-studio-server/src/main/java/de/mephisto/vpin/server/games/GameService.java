package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.fx.OverlayWindowFX;
import de.mephisto.vpin.server.util.PinUPConnector;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(GameService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();
  }


  @SuppressWarnings("unused")
  public Game getGame(int id) {
    return pinUPConnector.getGame(id);
  }

  @SuppressWarnings("unused")
  public List<Game> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.pinUPConnector.getGameIdsFromPlaylists();
    List<Game> games = pinUPConnector.getGames();
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public List<Game> getGameInfos() {
    return pinUPConnector.getGames();
  }
  @SuppressWarnings("unused")
  @Nullable
  public Game getGameByVpxFilename(@NonNull String filename) {
    List<Game> games = pinUPConnector.getGames();
    for (Game game : games) {
      if (game.getGameFile().getName().equals(filename)) {
        return game;
      }
    }
    return null;
  }

  @Nullable
  public Game getGameByRom(@NonNull String romName) {
    List<Game> games = pinUPConnector.getGames();
    for (Game game : games) {
      if (game.getRom() != null && game.getRom().equals(romName)) {
        return game;
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  public Game getGameByName(String table) {
    return this.pinUPConnector.getGameByName(table);
  }

  public Game getGameByFile(File file) {
    return this.pinUPConnector.getGameByFilename(file.getName());
  }
}
