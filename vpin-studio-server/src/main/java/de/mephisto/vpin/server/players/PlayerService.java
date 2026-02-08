package de.mephisto.vpin.server.players;

import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.highscores.Score;
import edu.umd.cs.findbugs.annotations.NonNull;
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

  private final List<PlayerLifecycleListener> lifecycleListeners = new ArrayList<>();

  public List<Player> getBuildInPlayers() {
    List<Player> all = null;
    try {
      all = playerRepository.findAll();
      all.sort(new Comparator<Player>() {
        @Override
        public int compare(Player o1, Player o2) {
          if (o1.getName() != null && o2.getName() != null) {
            return o1.getName().compareTo(o2.getName());
          }
          return 0;
        }
      });
    }
    catch (Exception e) {
      LOG.error("Failed to load all players: {}", e.getMessage(), e);
      all = new ArrayList<>();
    }

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

  public String getAdminPlayerInitials() {
    Player adminPlayer = getAdminPlayer();
    if (adminPlayer != null) {
      return adminPlayer.getInitials();
    }
    return null;
  }

  @Nullable
  public Player getAdminPlayer() {
    List<Player> buildInPlayers = getBuildInPlayers();
    return buildInPlayers.stream().filter(Player::isAdministrative).findFirst().orElse(null);
  }

  @Nullable
  public synchronized Player getPlayerForInitials(long serverId, @Nullable String initials) {
    if (StringUtils.isEmpty(initials)) {
      return null;
    }

    List<Player> players = new ArrayList<>();
    try {
      players = playerRepository.findByInitials(initials.toUpperCase());
      if (players.size() > 1) {
        LOG.warn("Found duplicate player for initials '{}', using first one.", initials);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to find players by initials: {}", e.getMessage(), e);
    }

    if (!players.isEmpty()) {
      return players.get(0);
    }

    Player discordPlayer = discordService.getPlayerByInitials(serverId, initials);
    if (discordPlayer != null) {
      return discordPlayer;
    }

    return null;
  }

  public Player save(Player player) {
    Player model = new Player();
    boolean existingPlayer = player.getId() != null && player.getId() > 0;
    if (existingPlayer) {
      model = playerRepository.findById(player.getId()).get();
    }

    if (player.getAvatar() != null) {
      Optional<Asset> asset = assetRepository.findByUuid(player.getAvatar().getUuid());
      if (asset.isPresent()) {
        model.setAvatar(asset.get());
      }
    }

    model.setDomain(player.getDomain());
    model.setName(player.getName());
    model.setInitials(player.getInitials());
    model.setAdministrative(player.isAdministrative());
    model.setManiaAccountUuid(player.getManiaAccountUuid());

    Player updated = playerRepository.saveAndFlush(model);
    LOG.info("Saved " + updated);

    if (existingPlayer) {
      notifyPlayerUpdated(player);
    }
    else {
      notifyPlayerCreated(player);
    }
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

      playerRepository.deleteById(id);
      LOG.info("Deleted player " + id);
      notifyPlayerDeleted(player);
    }
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

  public void validateInitials(Score newScore) {
    String defaultInitials = getAdminPlayerInitials();
    if (String.valueOf(newScore.getPlayerInitials()).equals("???") && defaultInitials != null && newScore.getScore() > 0) {
      newScore.setPlayerInitials(defaultInitials);
    }
  }

  public void addPlayerLifecycleListener(@NonNull PlayerLifecycleListener lifecycleListener) {
    this.lifecycleListeners.add(lifecycleListener);
  }

  private void notifyPlayerCreated(@NonNull Player player) {
    for (PlayerLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.playerCreated(player);
    }
  }

  private void notifyPlayerUpdated(@NonNull Player player) {
    for (PlayerLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.playerUpdated(player);
    }
  }

  private void notifyPlayerDeleted(@NonNull Player player) {
    for (PlayerLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.playerDeleted(player);
    }
  }
}
