package de.mephisto.vpin.server.players;

import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerService.class);

  @Autowired
  private PlayerRepository playerRepository;

  @Autowired
  private AssetRepository assetRepository;

  public List<Player> getBuildInPlayers() {
    return playerRepository.findAll();
  }

  public Player getBuildInPlayer(long id) {
    Optional<Player> player = playerRepository.findById(id);
    return player.orElse(null);
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
}
