package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ManiaServiceCache {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaServiceCache.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PlayerService playerService;

  private ManiaService maniaService;
  private final Map<String, Account> playerCache = new HashMap<>();
  private final Map<String, List<Game>> gamesByVpsId = new HashMap<>();

  public boolean clear() {
    playerCache.clear();
    gamesByVpsId.clear();
    LOG.info("Mania service cache has been cleared.");
    return true;
  }

  public void clearGames() {
    if (!gamesByVpsId.isEmpty()) {
      gamesByVpsId.clear();
      LOG.info("Mania games cache has been cleared.");
    }
  }

  /**
   * Clears the games cache if the VPS mapping of the given game differs from the cached one.
   */
  public void invalidateIfVpsMappingChanged(@NonNull Game game) {
    if (gamesByVpsId.isEmpty()) {
      return;
    }

    Game cachedGame = null;
    for (List<Game> games : gamesByVpsId.values()) {
      for (Game g : games) {
        if (g.getId() == game.getId()) {
          cachedGame = g;
          break;
        }
      }
    }

    String cachedTableId = cachedGame != null ? cachedGame.getExtTableId() : null;
    String cachedVersionId = cachedGame != null ? cachedGame.getExtTableVersionId() : null;
    if (!Objects.equals(StringUtils.trimToNull(cachedTableId), StringUtils.trimToNull(game.getExtTableId()))
        || !Objects.equals(StringUtils.trimToNull(cachedVersionId), StringUtils.trimToNull(game.getExtTableVersionId()))) {
      LOG.info("VPS mapping of \"{}\" has changed, clearing Mania games cache.", game.getGameDisplayName());
      clearGames();
    }
  }

  public List<Account> getCachedPlayerAccounts() {
    return new ArrayList<>(playerCache.values());
  }

  public void preCache() {
    if (gamesByVpsId.isEmpty()) {
      List<Game> knownGames = new ArrayList<>(gameService.getKnownGames(-1));
      knownGames.addAll(gameService.getKnownFpGames(-1));
      for (Game game : knownGames) {
        if (game == null) {
          continue;
        }
        if (StringUtils.isEmpty(game.getExtTableId())) {
          LOG.info("Skipped highscore sync for \"{}\", because invalid VPS mapping", game.getGameDisplayName());
        }
        else {
          List<Game> games = new ArrayList<>();
          if (gamesByVpsId.containsKey(game.getExtTableId())) {
            games = gamesByVpsId.get(game.getExtTableId());
          }
          games.add(game);
          gamesByVpsId.put(game.getExtTableId(), games);
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

  public List<Game> getGamesByVpsId(String vpsTableId) {
    return gamesByVpsId.getOrDefault(vpsTableId, Collections.emptyList());
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
