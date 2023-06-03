package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
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
  public DiscordBotStatus getStatus(long serverId) {
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    String defaultChannelId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_CHANNEL_ID);
    long botId = this.getBotId();

    DiscordBotStatus status = new DiscordBotStatus();
    status.setServerId(serverId);
    status.setBotId(botId);
    status.setValid(botId != -1 && this.discordClient != null && this.discordClient.getGuilds().size() > 0);
    if (botId != -1) {
      try {
        DiscordMember member = this.discordClient.getMember(serverId, botId);
        if (member != null) {
          status.setBotInitials(member.getInitials());
        }
      } catch (Exception e) {
        LOG.warn("Failed to set BOT initials: " + e.getMessage());
      }

      try {
        long channelId = Long.parseLong(defaultChannelId);
        status.setValidDefaultChannel(!StringUtils.isEmpty(defaultChannelId) && this.discordClient != null && this.getChannel(Long.parseLong(guildId), channelId) != null);
      } catch (Exception e) {
        status.setValidDefaultChannel(false);
      }
    }

    return status;
  }

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
      List<DiscordChannel> collect = this.discordClient.getChannels(serverId).stream().filter(c -> c.getId() != channelId).map(c -> {
        DiscordChannel ct = new DiscordChannel();
        ct.setName(c.getName());
        ct.setId(c.getId());
        return ct;
      }).collect(Collectors.toList());
      if (!collect.isEmpty()) {
        return collect.get(0);
      }
    }
    return null;
  }

  public List<DiscordChannel> getChannels() {
    if (this.discordClient != null) {
      return this.discordClient.getChannels().stream().map(c -> {
        DiscordChannel ct = new DiscordChannel();
        ct.setName(c.getName());
        ct.setId(c.getId());
        return ct;
      }).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public List<DiscordChannel> getChannels(long guildId) {
    if (this.discordClient != null) {
      return this.discordClient.getChannels(guildId).stream().map(c -> {
        DiscordChannel ct = new DiscordChannel();
        ct.setName(c.getName());
        ct.setId(c.getId());
        return ct;
      }).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public boolean hasJoinPermissions(long serverId, long channelId, long memberId) {
    if (this.discordClient != null) {
      return this.discordClient.hasPermissions(serverId, channelId, memberId,
          VIEW_CHANNEL,
          MESSAGE_SEND,
          MESSAGE_MANAGE,
          MESSAGE_EMBED_LINKS,
          MESSAGE_ATTACH_FILES,
          MESSAGE_HISTORY);
    }
    return false;
  }


  public boolean hasManagePermissions(long serverId, long channelId, long memberId) {
    if (this.discordClient != null) {
      return this.discordClient.hasPermissions(serverId, channelId, memberId,
          MANAGE_CHANNEL,
          VIEW_CHANNEL,
          MESSAGE_SEND,
          MESSAGE_MANAGE,
          MESSAGE_EMBED_LINKS,
          MESSAGE_ATTACH_FILES,
          MESSAGE_HISTORY);
    }
    return false;
  }

  public List<Player> getCompetitionPlayers(long serverId, long channelId) {
    List<Player> result = new ArrayList<>();
    if (this.discordClient != null) {
      List<DiscordMessage> pinnedMessages = discordClient.getPinnedMessages(serverId, channelId);
      for (DiscordMessage pinnedMessage : pinnedMessages) {
        if (pinnedMessage.getMember() != null) {
          Player player = toPlayer(pinnedMessage.getMember());
          if (!result.contains(player)) {
            result.add(player);
          }
        }
        else {
          LOG.warn("Unable to determine member of pinned message: '" + pinnedMessage.getRaw() + "' (" + pinnedMessage.getId() + ")");
        }
      }
    }
    return result;
  }

  public long sendMessage(long serverId, long channelId, String message) {
    if (this.discordClient != null) {
      return this.discordClient.sendMessage(serverId, channelId, message);
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
      long serverId = Long.parseLong(String.valueOf(preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID, -1)));
      long channelId = Long.parseLong(String.valueOf(preferencesService.getPreferenceValue(PreferenceNames.DISCORD_CHANNEL_ID, -1)));
      if (serverId > 0 && channelId > 0) {
        this.sendMessage(serverId, channelId, message);
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

  /**
   * Returns the list of scores posted on the given channel.
   * If no message was found, the initial score is taken as single value.
   *
   * @param uuid      the uuid of the competition
   * @param serverId  the discord server id
   * @param channelId the discord channel id
   */
  @NonNull
  public ScoreSummary getScoreSummary(@NonNull HighscoreParser highscoreParser, @NonNull String uuid, long serverId, long channelId) {
    if (this.discordClient != null) {
      List<DiscordMessage> competitionUpdates = discordClient.getPinnedMessages(serverId, channelId);
      for (DiscordMessage pinnedMessage : competitionUpdates) {
        if (pinnedMessage.getRaw().contains(DiscordChannelMessageFactory.HIGHSCORE_INDICATOR) && pinnedMessage.getRaw().contains(uuid)) {
          return toScoreSummary(highscoreParser, pinnedMessage);
        }
      }
    }
    return new ScoreSummary(Collections.emptyList(), new Date());
  }

  public boolean isEnabled() {
    return this.discordClient != null;
  }

  public DiscordCompetitionData getCompetitionData(long serverId, long channelId) {
    if (this.discordClient != null) {
      List<DiscordMessage> pinnedMessages = discordClient.getPinnedMessages(serverId, channelId);
      for (DiscordMessage pinnedMessage : pinnedMessages) {
        if (pinnedMessage.getRaw().contains(DiscordChannelMessageFactory.START_INDICATOR)) {
          return CompetitionDataHelper.getCompetitionData(pinnedMessage);
        }
      }
    }
    return null;
  }

  private DiscordClient recreateDiscordClient() throws Exception {
    String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);

    try {
      if (this.discordClient != null && (StringUtils.isEmpty(botToken) || !botToken.equals(this.discordClient.getBotToken()))) {
        this.discordClient.shutdown();
        LOG.info("Discord client has been shutdown, because the token was changed.");
      }
      this.discordClient = null;
    } catch (Exception e) {
      LOG.warn("Error in JDA shutdown: " + e.getMessage());
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
    } catch (Exception e) {
      LOG.error("Failed to create discord client: " + e.getMessage() + ". Try to update your settings to create a valid client.");
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
        return toServer(guild);
      }
    }
    return null;
  }

  public List<DiscordServer> getServers() {
    List<DiscordServer> result = new ArrayList<>();
    if (this.discordClient != null) {
      List<GuildInfo> guilds = this.discordClient.getGuilds();
      for (GuildInfo guild : guilds) {
        result.add(toServer(guild));
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
        return toPlayer(results.get(0));
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
    if (propertyName.equals(PreferenceNames.DISCORD_BOT_TOKEN)) {
      LOG.info("Detected Discord config change, updating BOT.");
      this.discordClient = recreateDiscordClient();
    }
    else if (propertyName.equals(PreferenceNames.DISCORD_GUILD_ID) || propertyName.equals(PreferenceNames.DISCORD_BOT_ALLOW_LIST)) {
      this.applyDefaultDiscordSettings();
    }
  }

  private void applyDefaultDiscordSettings() {
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    String whiteList = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_ALLOW_LIST);

    if (this.discordClient != null) {
      if (!StringUtils.isEmpty(whiteList)) {
        String[] split = whiteList.split(",");
        this.discordClient.setCommandsAllowList(Arrays.asList(split));
      }

      if (!StringUtils.isEmpty(guildId)) {
        this.discordClient.setDefaultGuildId(Long.parseLong(guildId));
      }
    }
  }

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

  private DiscordServer toServer(@NonNull GuildInfo guild) {
    DiscordServer s = new DiscordServer();
    s.setOwnerId(guild.getOwnerId());
    s.setId(guild.getId());
    s.setName(guild.getName());
    s.setAvatarUrl(guild.getAvatarUrl());
    return s;
  }

  private ScoreSummary toScoreSummary(@NonNull HighscoreParser highscoreParser, @NonNull DiscordMessage message) {
    List<Score> scores = new ArrayList<>();
    ScoreSummary summary = new ScoreSummary(scores, message.getCreatedAt());
    String raw = message.getRaw();
    scores.addAll(highscoreParser.parseScores(message.getCreatedAt(), raw, -1, message.getServerId()));
    return summary;
  }

  public void initCompetition(long serverId, long channelId, long messageId) {
    //delete existing pins for new competition starts
    clearPinnedMessages(serverId, channelId);
    //use the slow mode to avoid concurrent pinning of new highscores
    discordClient.setSlowMode(serverId, channelId, 5);
    discordClient.pinMessage(serverId, channelId, messageId);
  }

  public void updateHighscoreMessage(long serverId, long channelId, long msgId) {
    List<DiscordMessage> pinnedMessages = discordClient.getPinnedMessages(serverId, channelId);
    for (DiscordMessage pinnedMessage : pinnedMessages) {
      if (pinnedMessage.getRaw().contains(DiscordChannelMessageFactory.HIGHSCORE_INDICATOR)) {
        discordClient.unpinMessage(serverId, channelId, pinnedMessage.getId());
      }
    }
    discordClient.pinMessage(serverId, channelId, msgId);
  }

  public void finishCompetition(long serverId, long channelId, long msgId) {
    this.discordClient.pinMessage(serverId, channelId, msgId);
    List<DiscordMessage> pinnedMessages = discordClient.getPinnedMessages(serverId, channelId);
    for (DiscordMessage pinnedMessage : pinnedMessages) {
      if (pinnedMessage.getRaw().contains(DiscordChannelMessageFactory.JOIN_INDICATOR)) {
        discordClient.unpinMessage(serverId, channelId, pinnedMessage.getId());
      }
    }
  }

  public void clearPinnedMessages(long serverId, long channelId) {
    List<DiscordMessage> pinnedMessages = discordClient.getPinnedMessages(serverId, channelId);
    for (DiscordMessage pinnedMessage : pinnedMessages) {
      discordClient.unpinMessage(serverId, channelId, pinnedMessage.getId());
    }
  }

  public boolean isCompetitionActive(long serverId, long channelId, String uuid) {
    if (this.discordClient == null) {
      return true;
    }

    //well, we abuse this as a connection check.
    String topic = this.discordClient.getTopic(serverId, channelId);
    //null means there are problem during reading from Discord, so let's assume everything is ok
    if (topic == null) {
      return true;
    }

    List<DiscordMessage> pinnedMessages = this.discordClient.getPinnedMessages(serverId, channelId);
    if (pinnedMessages.isEmpty()) {
      return false;
    }

    for (DiscordMessage pinnedMessage : pinnedMessages) {
      if (pinnedMessage.getRaw().contains(DiscordChannelMessageFactory.FINISHED_INDICATOR)
          || pinnedMessage.getRaw().contains(DiscordChannelMessageFactory.CANCEL_INDICATOR)) {
        LOG.info("Found finished or canceled message indicator for competition " + uuid);
        return false;
      }
    }

    DiscordCompetitionData competitionData = this.getCompetitionData(serverId, channelId);
    return competitionData != null && competitionData.getUuid().equals(uuid);
  }

  public void addCompetitionPlayer(long serverId, long channelId, long msgId) {
    if (this.discordClient != null) {
      List<DiscordMessage> pinnedMessages = discordClient.getPinnedMessages(serverId, channelId);
      if (pinnedMessages.size() < 50) {
        discordClient.pinMessage(serverId, channelId, msgId);
      }
      else {
        LOG.warn("Player could not be added to the player list for channel " + channelId + ", pin limit has been reached.");
      }
    }
  }

  public void removeCompetitionPlayer(long serverId, long channelId) {
    if (this.discordClient != null) {
      long botId = getBotId();
      List<DiscordMessage> pinnedMessages = discordClient.getPinnedMessages(serverId, channelId);
      for (DiscordMessage pinnedMessage : pinnedMessages) {
        if (pinnedMessage.getMember().getId() != botId) {
          continue;
        }

        if (pinnedMessage.getRaw().contains(DiscordChannelMessageFactory.JOIN_INDICATOR)) {
          discordClient.unpinMessage(serverId, channelId, pinnedMessage.getId());
          LOG.info("Removed bot from list of players in channel " + channelId);
        }
      }
    }
  }

  public boolean clearCache() {
    if (this.discordClient != null) {
      this.discordClient.clearCache();
    }
    return true;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    this.recreateDiscordClient();
  }
}
