package de.mephisto.vpin.server.res;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ResService {
  private final static Logger LOG = LoggerFactory.getLogger(ResService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  public boolean delete(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      File resFile = game.getResFile();
      if (resFile.exists() && resFile.delete()) {
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.RES, null);
        return true;
      }
    }
    return false;
  }
}
