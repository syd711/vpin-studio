package de.mephisto.vpin.server.players;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
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
    if (domain.equals(PlayerDomain.DISCORD)) {
      List<Player> players = new ArrayList<>();
      List<DiscordMember> members = discordPlayerService.getMembers();
      for (DiscordMember member : members) {
        Player player = toPlayer(member);
        players.add(player);
      }
      players.sort(Comparator.comparing(Player::getName));
      return players;
    }
    return Collections.emptyList();
  }

  public boolean invalidateDomain(PlayerDomain domain) {
    if (domain.equals(PlayerDomain.DISCORD)) {
      discordPlayerService.getMembers();
    }
    return false;
  }

  public Player save(Player player) {
    Player model = new Player();
    if(player.getId() > 0) {
      model = playerRepository.findById(player.getId()).get();
    }

    if(player.getAvatar() != null) {
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
    if(byId.isPresent()) {
      Player player = byId.get();
      Asset avatar = player.getAvatar();
      if(avatar != null) {
        assetRepository.delete(avatar);
        LOG.info("Deleted asset " + avatar);
      }
    }
    playerRepository.deleteById(id);
    LOG.info("Deleted player " + id);
  }

  private Player toPlayer(DiscordMember member) {
    Player player = new Player();
    player.setName(member.getName());
    player.setInitials(member.getInitials());
    player.setAvatarUrl(member.getAvatarUrl());
    player.setDomain(PlayerDomain.DISCORD.name());
    return player;
  }
}
