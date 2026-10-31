package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.*;
import de.mephisto.vpin.restclient.discord.DiscordCategory;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.connectors.discord.Permissions.*;

@Service
public class DiscordService implements InitializingBean, PreferenceChangedListener, DiscordCommandResolver {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordService.class);

  private DiscordClient discordClient;

  @Autowired
  private PreferencesService preferencesService;

  private DiscordBotCommandListener botCommandListener;

  @NonNull
  public List<DiscordMember> getMembers() {
    if (this.discordClient != null) {
      return this.discordClient.getMembers();
    }
    return Collections.emptyList();
  }

  @NonNull
  public List<DiscordMember> getMembers(long serverId) {
    if (this.discordClient != null) {
      return this.discordClient.getMembers(serverId);
    }
    return Collections.emptyList();
  }

  @Nullable
  public Player getPlayer(long serverId, long memberId) {
    if (this.discordClient != null) {
      DiscordMember member = this.discordClient.getMember(serverId, memberId);
      if (member != null) {
        return toPlayer(member);
      }
    }
    return null;
  }

  @Nullable
  public DiscordChannel getChannel(long serverId, long channelId) {
    if (this.discordClient != null) {
      List<DiscordChannel> collect = this.discordClient.getChannels(serverId).stream().filter(c -> c.getId() == channelId).map(c -> {
        return toChannel(c);
      }).toList();
      if (!collect.isEmpty()) {
        return collect.getFirst();
      }
    }
    return null;
  }

  public List<DiscordChannel> getChannels() {
    if (this.discordClient != null) {
      return this.discordClient.getChannels().stream().map(c -> {
        return toChannel(c);
      }).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public List<DiscordChannel> getChannels(long guildId) {
    if (this.discordClient != null) {
      return this.discordClient.getChannels(guildId).stream().map(c -> {
        return toChannel(c);
      }).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }


  public boolean hasManageChannelPermissions(long serverId, long channelId, long memberId) {
    if (this.discordClient != null) {
      return this.discordClient.hasPermissions(serverId, channelId, memberId,
//          MANAGE_CHANNEL,
          VIEW_CHANNEL,
          MESSAGE_SEND,
          MESSAGE_MANAGE,
          MESSAGE_EMBED_LINKS,
          MESSAGE_ATTACH_FILES,
          MESSAGE_HISTORY);
    }
    return false;
  }



  public boolean hasManageChannelPermissions(long serverId, long memberId) {
    if (this.discordClient != null) {
      return this.discordClient.hasPermissions(serverId, memberId,
//          MANAGE_CHANNEL,
          VIEW_CHANNEL,
          MESSAGE_SEND,
          MESSAGE_MANAGE,
          MESSAGE_EMBED_LINKS,
          MESSAGE_ATTACH_FILES,
          MESSAGE_HISTORY);
    }
    return false;
  }

  public long sendMessage(long serverId, long channelId, String message) {
    if (this.discordClient != null) {
      try {
        SLOG.info("Sending discord message to channel " + channelId);
        return this.discordClient.sendMessage(serverId, channelId, message);
      }
      catch (Exception e) {
        LOG.error("Failed to send Discord message: {}", e.getMessage(), e);
      }
    }
    return -1;
  }

  public long sendMessage(long serverId, long channelId, MessageEmbed message) {
    if (this.discordClient != null) {
      try {
        SLOG.info("Sending discord message to channel " + channelId);
        return this.discordClient.sendMessage(serverId, channelId, message);
      }
      catch (Exception e) {
        LOG.error("Failed to send Discord message: {}", e.getMessage(), e);
      }
    }
    return -1;
  }

  public long sendMessage(long serverId, long channelId, String message, byte[] data, String name, String imageText) {
    if (this.discordClient != null) {
      return this.discordClient.sendMessage(serverId, channelId, message, data, name, imageText);
    }
    return -1;
  }

  public void sendDefaultHighscoreMessage(String message) {
    if (this.discordClient != null) {
      String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
      String defaultChannelId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_CHANNEL_ID);
      if (!StringUtils.isEmpty(guildId) && !StringUtils.isEmpty(defaultChannelId)) {
        this.sendMessage(Long.parseLong(guildId), Long.parseLong(defaultChannelId), message);
      }
    }
  }

  public long getBotId() {
    if (discordClient != null) {
      return discordClient.getBotId();
    }
    return -1;
  }

  @Nullable
  public DiscordMember getBot() {
    if (discordClient != null) {
      return discordClient.getBot();
    }
    return null;
  }

  public boolean isEnabled() {
    return this.discordClient != null;
  }

  private DiscordClient recreateDiscordClient() throws Exception {
    String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);

    try {
      if (this.discordClient != null && (StringUtils.isEmpty(botToken) || !botToken.equals(this.discordClient.getBotToken()))) {
        this.discordClient.shutdown();
        LOG.info("Discord client has been shutdown, because the token was changed.");
      }
      this.discordClient = null;
    }
    catch (Exception e) {
      LOG.warn("Error in JDA shutdown: {}", e.getMessage());
    }

    try {
      if (StringUtils.isEmpty(botToken)) {
        LOG.info("Skipped discord client creation, no botId set.");
        return null;
      }

      if (this.discordClient == null) {
        this.discordClient = new DiscordClient(botToken, this);
        this.applyDefaultDiscordSettings();
        LOG.info("Recreated Discord client.");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to create discord client: {}. Try to update your settings to create a valid client.", e.getMessage());
      throw e;
    }
    return this.discordClient;
  }

  public void setBotCommandListener(DiscordBotCommandListener listener) {
    this.botCommandListener = listener;
  }

  private BotCommandResponse notifyBotCommandListener(BotCommand botCommand) {
    if (this.botCommandListener != null) {
      return this.botCommandListener.onBotCommand(botCommand);
    }
    return null;
  }

  @Override
  public BotCommandResponse resolveCommand(BotCommand cmd) {
    return notifyBotCommandListener(cmd);
  }

  public void setActivity(@Nullable String status) {
    if (this.discordClient != null) {
      this.discordClient.setActivity(status);
    }
  }

  public DiscordServer getServer(long serverId) {
    if (this.discordClient != null) {
      GuildInfo guild = this.discordClient.getGuildById(serverId);
      if (guild != null) {
        List<de.mephisto.vpin.connectors.discord.DiscordCategory> categories = this.discordClient.getCategories(serverId);
        return toServer(guild, categories);
      }
    }
    return null;
  }

  public List<DiscordServer> getServers() {
    List<DiscordServer> result = new ArrayList<>();
    if (this.discordClient != null) {
      List<GuildInfo> guilds = this.discordClient.getGuilds();
      for (GuildInfo guild : guilds) {
        List<de.mephisto.vpin.connectors.discord.DiscordCategory> categories = this.discordClient.getCategories(guild.getId());
        result.add(toServer(guild, categories));
      }
    }
    return result;
  }

  public List<DiscordServer> getMyServers() {
    List<DiscordServer> result = new ArrayList<>();
    if (this.discordClient != null) {
      List<GuildInfo> guilds = this.discordClient.getGuilds();
      for (GuildInfo guild : guilds) {
        List<de.mephisto.vpin.connectors.discord.DiscordCategory> categories = this.discordClient.getCategories(guild.getId());
        result.add(toServer(guild, categories));
      }
    }
    return result;
  }

  public Player getPlayerByInitials(long serverId, String initials) {
    if (serverId > 0) {
      List<DiscordMember> results = new ArrayList<>();
      for (DiscordMember member : this.getMembers(serverId)) {
        if (!StringUtils.isEmpty(member.getInitials()) && member.getInitials().equalsIgnoreCase(initials.toUpperCase())) {
          results.add(member);
        }
      }

      if (results.size() == 1) {
        return toPlayer(results.getFirst());
      }
      else if (results.size() > 1) {
        Optional<DiscordMember> realPlayer = results.stream().filter(member -> !member.isBot()).findFirst();
        if (realPlayer.isPresent()) {
          return toPlayer(realPlayer.get());
        }

        Optional<DiscordMember> bot = results.stream().filter(DiscordMember::isBot).findFirst();
        if (bot.isPresent()) {
          return toPlayer(bot.get());
        }
      }
    }
    return null;
  }


  public List<Player> getPlayers(long serverId) {
    List<Player> players = new ArrayList<>();
    for (DiscordMember member : this.getMembers(serverId)) {
      Player player = toPlayer(member);
      players.add(player);
    }
    players.sort(Comparator.comparing(Player::getName));
    return players;
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
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    try {
      if (propertyName.equals(PreferenceNames.DISCORD_BOT_TOKEN)) {
        LOG.info("Detected Discord config change, updating BOT.");
        this.discordClient = recreateDiscordClient();
      }
      else if (propertyName.equals(PreferenceNames.DISCORD_GUILD_ID) || propertyName.equals(PreferenceNames.DISCORD_BOT_ALLOW_LIST)) {
        this.applyDefaultDiscordSettings();
        LOG.info("Re-applied discord settings.");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to update discord preferences: {}", e.getMessage());
    }
  }

  private void applyDefaultDiscordSettings() {
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    if (this.discordClient != null) {
      List<Long> allowList = getAllowList().stream().map(player -> player.getId()).collect(Collectors.toList());
      this.discordClient.setCommandsAllowList(allowList);

      if (!StringUtils.isEmpty(guildId)) {
        this.discordClient.setDefaultGuildId(Long.parseLong(guildId));
      }
    }
  }

  public void shutdown() {
    if (this.discordClient != null) {
      this.discordClient.close();
    }
  }

  //------------------------------ Model Helper ------------------------------------------------------------------------

  private Player toPlayer(@NonNull DiscordMember member) {
    DiscordPlayer player = new DiscordPlayer();
    player.setId(member.getId());
    player.setName(member.getName());
    player.setInitials(member.getInitials());
    player.setAvatarUrl(member.getAvatarUrl());
    player.setDomain(PlayerDomain.DISCORD.name());
    player.setBot(member.isBot());
    return player;
  }

  private DiscordServer toServer(@NonNull GuildInfo guild, List<de.mephisto.vpin.connectors.discord.DiscordCategory> categories) {
    DiscordServer s = new DiscordServer();
    s.setOwnerId(guild.getOwnerId());
    s.setId(guild.getId());
    s.setName(guild.getName());
    s.setAvatarUrl(guild.getAvatarUrl());
    s.setCategories(categories.stream().map(this::toCategory).collect(Collectors.toList()));
    return s;
  }

  private DiscordChannel toChannel(@NonNull DiscordTextChannel channel) {
    DiscordChannel s = new DiscordChannel();
    s.setId(channel.getId());
    s.setName(channel.getName());
    s.setCreationDate(channel.getCreationDate());
    return s;
  }

  private DiscordCategory toCategory(de.mephisto.vpin.connectors.discord.DiscordCategory c) {
    DiscordCategory category = new DiscordCategory();
    category.setId(c.getId());
    category.setName(c.getName());
    return category;
  }

  public List<DiscordCategory> getCategories(long serverId) {
    if (this.discordClient != null) {
      List<de.mephisto.vpin.connectors.discord.DiscordCategory> categories = this.discordClient.getCategories(serverId);
      return categories.stream().map(c -> toCategory(c)).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public List<Player> getAllowList() {
    List<Player> result = new ArrayList<>();
    String allowList = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_ALLOW_LIST);
    if (!StringUtils.isEmpty(allowList)) {
      String[] split = allowList.split(",");

      if (this.discordClient != null) {
        for (String item : split) {
          try {
            long id = Long.parseLong(item);
            DiscordMember member = this.discordClient.getMember(id);
            if (member != null) {
              result.add(toPlayer(member));
            }
          }
          catch (Exception e) {
            //ignore
          }
        }
      }
    }
    return result;
  }

  public boolean clearCache() {
    if (this.discordClient != null) {
      this.discordClient.clearCache();
    }
    return true;
  }

  public DiscordBotStatus validateSettings() {
    DiscordBotStatus status = new DiscordBotStatus();
    try {
      if (this.discordClient == null) {
        this.recreateDiscordClient();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to initialize Discord client: {}", e.getMessage(), e);
      status.setError("Failed to initialize Discord client: " + e.getMessage());
      return status;
    }

    String serverId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    String channelId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_CHANNEL_ID);

    try {
      if (this.discordClient != null) {
        status.setName(discordClient.getBot().getName());
        status.setBotId(this.discordClient.getBotId());

        if (!StringUtils.isEmpty(serverId)) {
          GuildInfo guild = this.discordClient.getGuildById(Long.parseLong(serverId));
          if (guild == null) {
            preferencesService.savePreference(PreferenceNames.DISCORD_GUILD_ID, null, false);
            preferencesService.savePreference(PreferenceNames.DISCORD_CHANNEL_ID, null, false);
            status.setError("No valid matching Discord server found.");
            return status;
          }
        }

        if (!StringUtils.isEmpty(channelId)) {
          DiscordChannel channel = this.getChannel(Long.parseLong(serverId), Long.parseLong(channelId));
          if (channel == null) {
            preferencesService.savePreference(PreferenceNames.DISCORD_CHANNEL_ID, null, false);
            status.setError("No valid matching channel found.");
            return status;
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to validate Discord settings: {}", e.getMessage(), e);
      status.setError("Failed to validate Discord settings: " + e.getMessage());
    }

    return status;
  }

  @Override
  public void afterPropertiesSet() {
    preferencesService.addChangeListener(this);
    //async discord init
    new Thread(() -> {
      try {
        Thread.currentThread().setName("Discord Initializer");
        this.recreateDiscordClient();
        this.clearCache();
      }
      catch (Exception e) {
        LOG.error("Failed to initialize Discord Service: {}", e.getMessage());
      }
    }).start();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
