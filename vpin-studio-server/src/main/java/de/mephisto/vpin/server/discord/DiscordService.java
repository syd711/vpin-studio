package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
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

@Service
public class DiscordService implements InitializingBean, PreferenceChangedListener, DiscordCommandResolver {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordService.class);

  private DiscordClient discordClient;

  @Autowired
  private PreferencesService preferencesService;

  private DiscordBotCommandListener botCommandListener;

  public List<DiscordMember> getMembers() {
    if (this.discordClient != null) {
      return this.discordClient.getMembers();
    }
    return Collections.emptyList();
  }


  public Player getPlayer(long serverId, long memberId) {
    if (this.discordClient != null) {
      return toPlayer(this.discordClient.getMember(serverId, memberId));
    }
    return null;
  }

  public String getStartMessageId(long serverId, long channelId) {
    if (this.discordClient != null) {
      String topic = this.discordClient.getTopic(serverId, channelId);
      return CompetitionDataHelper.getStartMessageId(topic);
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

  public List<Player> getCompetitionPlayers(long serverId, long channelId) {
    if (this.discordClient != null) {
      DiscordCompetitionData competitionData = getCompetitionData(serverId, channelId);
      List<DiscordMember> competitionMembers = this.discordClient.getCompetitionMembers(serverId, channelId, competitionData.getStartMessageId(), competitionData.getUuid());
      competitionMembers.add(0, this.discordClient.getMember(serverId, this.getBotId()));
      return competitionMembers.stream().map(this::toPlayer).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public String sendMessage(long serverId, long channelId, String message) {
    if (this.discordClient != null) {
      return this.discordClient.sendMessage(serverId, channelId, message);
    }
    return null;
  }

  public long getBotId() {
    return discordClient.getBotId();
  }

  public void saveCompetitionData(@NonNull Competition competition, @NonNull Game game, @NonNull ScoreSummary scoreSummary, @NonNull String messageId) {
    String topic = CompetitionDataHelper.toDataString(competition, game, scoreSummary, messageId);
    if (this.discordClient != null) {
      long discordServerId = competition.getDiscordServerId();
      long discordChannelId = competition.getDiscordChannelId();
      this.discordClient.setTopic(discordServerId, discordChannelId, topic);
    }
    else {
      throw new UnsupportedOperationException("No Discord client found.");
    }
  }

  @Nullable
  public ScoreSummary getScoreSummary(long serverId, long channelId) {
    if (this.discordClient != null) {
      String topicText = discordClient.getTopic(serverId, channelId);
      if (topicText != null) {
        return CompetitionDataHelper.getScores(this, topicText);
      }
    }
    return null;
  }

  public void resetCompetition(long serverId, long channelId) {
    if (this.discordClient != null) {
      this.discordClient.setTopic(serverId, channelId, "No active competition.");
    }
    else {
      throw new UnsupportedOperationException("No Discord client found.");
    }
  }

  public boolean isEnabled() {
    return this.discordClient != null;
  }

  public DiscordCompetitionData getCompetitionData(long serverId, long channelId) {
    if (this.discordClient != null) {
      String topic = this.discordClient.getTopic(serverId, channelId);
      DiscordCompetitionData competitionData = CompetitionDataHelper.getCompetitionData(topic);
      if(competitionData != null) {
        return competitionData;
      }
    }
    return null;
  }

  private DiscordClient recreateDiscordClient() {
    String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    String whiteList = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_ALLOW_LIST);
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
        if (!StringUtils.isEmpty(whiteList)) {
          String[] split = whiteList.split(",");
          this.discordClient.setCommandsAllowList(Arrays.asList(split));
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
    if (this.botCommandListener != null) {
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

  public Optional<Player> getPlayerByInitials(String initials) {
    for (DiscordMember member : this.getMembers()) {
      if (!StringUtils.isEmpty(member.getInitials()) && member.getInitials().equalsIgnoreCase(initials.toUpperCase())) {
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
    if (propertyName.equals(PreferenceNames.DISCORD_GUILD_ID) || propertyName.equals(PreferenceNames.DISCORD_BOT_TOKEN) || propertyName.equals(PreferenceNames.DISCORD_BOT_ALLOW_LIST)) {
      LOG.info("Detected Discord config change, updating BOT.");
      String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);
      String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
      String whiteList = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_ALLOW_LIST);

      if (!StringUtils.isEmpty(botToken) && !StringUtils.isEmpty(guildId)) {
        LOG.info("Re-creating discord client because of preference changes.");
        this.discordClient = recreateDiscordClient();
      }
    }
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

  private DiscordServer toServer(GuildInfo guild) {
    DiscordServer s = new DiscordServer();
    s.setOwnerId(guild.getOwnerId());
    s.setId(guild.getId());
    s.setName(guild.getName());
    s.setAvatarUrl(guild.getAvatarUrl());
    return s;
  }
}
