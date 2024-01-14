package de.mephisto.vpin.server.players;

import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.discord.DiscordService;
import edu.umd.cs.findbugs.annotations.Nullable;
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
  private DiscordService discordService;

  @Autowired
  private AssetRepository assetRepository;

  public List<Player> getBuildInPlayers() {
    List<Player> all = playerRepository.findAll();
    all.sort(Comparator.comparing(Player::getName));

    PlayerDomain[] domains = PlayerDomain.values();
    List<Player> allPlayers = new ArrayList<>();
    allPlayers.addAll(all);
    for (PlayerDomain domain : domains) {
      allPlayers.addAll(getPlayersForDomain(domain));
    }

    duplicatesCheck(allPlayers);
    return all;
  }

  public Player getBuildInPlayer(long id) {
    Optional<Player> player = playerRepository.findById(id);
    return player.orElse(null);
  }

  public List<Player> getPlayersForDomain(PlayerDomain domain) {
    if (domain.equals(PlayerDomain.DISCORD) && discordService.isEnabled()) {
      List<Player> players = discordService.getPlayers();
      duplicatesCheck(players);
      return players;
    }
    return Collections.emptyList();
  }

  @Nullable
  public Player getPlayerForInitials(long serverId, @Nullable String initials) {
    if (StringUtils.isEmpty(initials)) {
      return null;
    }

    Player discordPlayer = discordService.getPlayerByInitials(serverId, initials);
    if (discordPlayer != null) {
      return discordPlayer;
    }

    List<Player> players = playerRepository.findByInitials(initials.toUpperCase());
    if (players.size() > 1) {
      LOG.warn("Found duplicate player for initials '{}', using first one.", initials);
    }

    if (!players.isEmpty()) {
      return players.get(0);
    }

    return null;
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
    model.setDisplayName(player.getDisplayName());
    model.setInitials(player.getInitials());
    model.setAdministrative(player.isAdministrative());
    model.setTournamentUserUuid(player.getTournamentUserUuid());

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

  private void duplicatesCheck(List<Player> players) {
    Map<String, Player> initials = new HashMap<>();
    for (Player player : players) {
      if (StringUtils.isEmpty(player.getInitials())) {
        continue;
      }

      if (initials.containsKey(player.getInitials())) {
        Player duplicate = initials.get(player.getInitials());

        if ((duplicate.getDomain() == null && player.getDomain() == null) ||
          duplicate.getDomain() != null && player.getDomain() != null) {
          duplicate.setDuplicatePlayerName(player.getName());
          player.setDuplicatePlayerName(duplicate.getName());
        }
      }
      else {
        initials.put(player.getInitials(), player);
      }
    }
  }
}
