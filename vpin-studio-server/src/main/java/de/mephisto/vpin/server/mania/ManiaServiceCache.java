package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManiaServiceCache {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaServiceCache.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PlayerService playerService;

  private ManiaService maniaService;
  private final Map<String, Account> playerCache = new HashMap<>();
  private final Map<String, Game> gamesByVpsId = new HashMap<>();

  public boolean clear() {
    playerCache.clear();
    gamesByVpsId.clear();
    LOG.info("Mania service cache has been cleared.");
    return true;
  }

  public List<Account> getCachedPlayerAccounts() {
    return new ArrayList<>(playerCache.values());
  }

  public void preCache() {
    if (gamesByVpsId.isEmpty()) {
      List<Game> knownGames = gameService.getKnownGames(-1);
      for (Game game : knownGames) {
        if (game == null || StringUtils.isEmpty(game.getExtTableId())) {
          LOG.info("Skipped highscore sync for \"" + game.getGameDisplayName() + "\", because invalid VPS mapping");
        }
        else {
          gamesByVpsId.put(game.getExtTableId(), game);
        }
      }
    }

    //preload players
    if (playerCache.isEmpty()) {
      List<Player> buildInPlayers = playerService.getBuildInPlayers();
      for (Player buildInPlayer : buildInPlayers) {
        Account accountByUuid = maniaService.getClient().getAccountClient().getAccountByUuid(buildInPlayer.getManiaAccountUuid());
        playerCache.put(buildInPlayer.getInitials(), accountByUuid);
      }
    }
  }

  public Game getGame(String vpsTableId) {
    return gamesByVpsId.get(vpsTableId);
  }

  public boolean containsAccountForInitials(String playerInitials) {
    return playerCache.containsKey(playerInitials) && playerCache.get(playerInitials) != null;
  }

  public Account getAccountForInitials(String playerInitials) {
    return playerCache.get(playerInitials);
  }

  public void setManiaService(ManiaService maniaService) {
    this.maniaService = maniaService;
  }
}
