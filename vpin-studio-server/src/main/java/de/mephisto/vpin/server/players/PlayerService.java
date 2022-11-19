package de.mephisto.vpin.server.players;

import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
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
    Player updated = playerRepository.saveAndFlush(player);
    LOG.info("Saved " + updated);
    return getBuildInPlayer(player.getId());
  }

  public void delete(long id) {
    playerRepository.deleteById(id);
  }
}
