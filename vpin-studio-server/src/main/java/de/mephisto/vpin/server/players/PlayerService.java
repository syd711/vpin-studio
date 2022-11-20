package de.mephisto.vpin.server.players;

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
    model.setDomain(player.getDomain());
    model.setName(player.getName());
    model.setInitials(player.getInitials());

    if(player.getAvatar() != null) {
      model.setAvatar(player.getAvatar());
    }

    Player updated = playerRepository.saveAndFlush(model);
    LOG.info("Saved " + updated);
    return updated;
  }

  public void delete(long id) {
    playerRepository.deleteById(id);
  }
}
