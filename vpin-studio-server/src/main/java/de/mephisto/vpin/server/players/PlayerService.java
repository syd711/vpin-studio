package de.mephisto.vpin.server.players;

import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.popper.PopperScreen;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlayerService {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerService.class);

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private DiscordPlayerService discordPlayerService;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private GameService gameService;

  public List<Player> getBuildInPlayers() {
    List<Player> all = playerRepository.findAll();
    all.sort(Comparator.comparing(Player::getName));
    return all;
  }

  public Player getBuildInPlayer(long id) {
    Optional<Player> player = playerRepository.findById(id);
    return player.orElse(null);
  }

  public List<Player> getPlayersForDomain(PlayerDomain domain) {
    if (domain.equals(PlayerDomain.DISCORD) && discordPlayerService.isEnabled()) {
      return discordPlayerService.getPlayers();
    }
    return Collections.emptyList();
  }

  public boolean invalidateDomain(PlayerDomain domain) {
    if (domain.equals(PlayerDomain.DISCORD)) {
      return discordPlayerService.refreshMembers();
    }
    return false;
  }

  public Player save(Player player) {
    Player model = new Player();
    if (player.getId() > 0) {
      model = playerRepository.findById(player.getId()).get();
    }

    if (player.getAvatar() != null) {
      Asset asset = assetRepository.findByUuid(player.getAvatar().getUuid()).get();
      model.setAvatar(asset);
    }

    model.setDomain(player.getDomain());
    model.setName(player.getName());
    model.setInitials(player.getInitials());

    Player updated = playerRepository.saveAndFlush(model);
    LOG.info("Saved " + updated);
    return updated;
  }

  public void delete(long id) {
    Optional<Player> byId = playerRepository.findById(id);
    if (byId.isPresent()) {
      Player player = byId.get();
      Asset avatar = player.getAvatar();
      if (avatar != null) {
        assetRepository.delete(avatar);
        LOG.info("Deleted asset " + avatar);
      }
    }
    playerRepository.deleteById(id);
    LOG.info("Deleted player " + id);
  }

  public List<PlayerScore> getHighscores(String initials) {
    List<PlayerScore> filtered = new ArrayList<>();

    Optional<Player> player = playerRepository.findByInitials(initials);
    if(player.isEmpty() && discordPlayerService.isEnabled()) {
      player = discordPlayerService.getPlayerByInitials(initials);
    }

    if (player.isPresent() && !StringUtils.isEmpty(player.get().getInitials())) {
      Player p = player.get();
      List<Game> games = gameService.getGames();
      for (Game game : games) {
        List<Score> scores = game.getScores();
        for (Score score : scores) {
          if (score.getUserInitials().equals(p.getInitials())) {
            String uri = null;
            GameMediaItem gameMediaItem = game.getEmulator().getGameMedia().get(PopperScreen.Wheel);
            if(gameMediaItem != null) {
              uri = gameMediaItem.getUri();
            }
            filtered.add(new PlayerScore(score, game.getScoresChangedDate(), game.getGameDisplayName(), uri));
          }
        }
      }
    }
    else {
      LOG.info("No player found with initials '" + initials + "'");
    }
    return filtered;
  }
}
