package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

@Service
public class DiscordService implements InitializingBean, PreferenceChangedListener, DiscordCommandResolver {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordService.class);

  private DiscordClient discordClient;

  @Autowired
  private PreferencesService preferencesService;

  private List<DiscordMember> lastMembers;

  private DiscordBotCommandListener botCommandListener;

  public List<DiscordMember> getMembers() {
    if (this.discordClient != null) {
      return this.discordClient.getMembers();
    }
    return Collections.emptyList();
  }

  public boolean isEnabled() {
    return this.discordClient != null;
  }

  private DiscordClient recreateDiscordClient() {
    String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    String whiteList = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_WHITELIST);
    try {
      if (this.discordClient != null) {
        this.discordClient.shutdown();
      }
      this.discordClient = null;
    } catch (Exception e) {
      LOG.warn("Error in JDA shutdown: " + e.getMessage());
    }

    try {
      if (!StringUtils.isEmpty(botToken) && !StringUtils.isEmpty(guildId)) {
        this.discordClient = new DiscordClient(botToken, guildId, this);
        if(!StringUtils.isEmpty(whiteList)) {
          String[] split = whiteList.split(",");
          this.discordClient.setChannelWhitelist(Arrays.asList(split));
        }
      }
      else {
        LOG.info("Skipped discord client creation, no botId or guildId set.");
      }
    } catch (Exception e) {
      LOG.error("Failed to create discord client: " + e.getMessage() + ". Try to update your settings to create a valid client.");
    }
    return this.discordClient;
  }

  public void setBotCommandListener(DiscordBotCommandListener listener) {
    this.botCommandListener = listener;
  }

  private BotCommandResponse notifyBotCommandListener(BotCommand botCommand) {
    if(this.botCommandListener != null) {
      return this.botCommandListener.onBotCommand(botCommand);
    }
    return null;
  }

  @Override
  public BotCommandResponse resolveCommand(BotCommand cmd) {
    return notifyBotCommandListener(cmd);
  }

  public void setStatus(@Nullable String status) {
    if (this.discordClient != null) {
      this.discordClient.setStatus(status);
    }
  }

  public Optional<Player> getPlayerByInitials(String initials) {
    for (DiscordMember member : this.getMembers()) {
      if (!StringUtils.isEmpty(member.getInitials()) && member.getInitials().toUpperCase().equals(initials.toUpperCase())) {
        return Optional.of(toPlayer(member));
      }
    }
    return Optional.empty();
  }

  public List<Player> getPlayers() {
    List<Player> players = new ArrayList<>();
    for (DiscordMember member : this.getMembers()) {
      Player player = toPlayer(member);
      players.add(player);
    }
    players.sort(Comparator.comparing(Player::getName));
    return players;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.DISCORD_GUILD_ID) || propertyName.equals(PreferenceNames.DISCORD_BOT_TOKEN)  || propertyName.equals(PreferenceNames.DISCORD_BOT_WHITELIST)) {
      LOG.info("Detected Discord config change, updating BOT.");
      String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);
      String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
      String whiteList = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_WHITELIST);

      if (!StringUtils.isEmpty(botToken) && !StringUtils.isEmpty(guildId)) {
        LOG.info("Re-creating discord client because of preference changes.");
        this.discordClient = recreateDiscordClient();
      }
    }
  }

  public boolean refreshMembers() {
    this.lastMembers = null;
    if (this.discordClient != null) {
      Consumer<List<DiscordMember>> consumer = discordMembers -> {
        lastMembers = discordMembers;
      };

      this.discordClient.refreshMembers(consumer, throwable -> notify());
      return this.lastMembers != null;
    }
    return false;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    this.recreateDiscordClient();
  }

  private Player toPlayer(DiscordMember member) {
    Player player = new Player();
    player.setId(member.getId());
    player.setName(member.getName());
    player.setInitials(member.getInitials());
    player.setAvatarUrl(member.getAvatarUrl());
    player.setDomain(PlayerDomain.DISCORD.name());
    return player;
  }
}
