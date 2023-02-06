package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscordService implements InitializingBean, PreferenceChangedListener, DiscordCommandResolver, DiscordStatusListener {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordService.class);

  private DiscordClient discordClient;

  @Autowired
  private PreferencesService preferencesService;

  private DiscordBotCommandListener botCommandListener;


  @NonNull
  public DiscordBotStatus getStatus() {
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    String defaultChannelId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_CHANNEL_ID);
    long botId = this.getBotId();

    DiscordBotStatus status = new DiscordBotStatus();
    status.setBotId(botId);
    status.setValid(this.discordClient != null && this.discordClient.getChannels().size() > 0);
    try {
      long channelId = Long.parseLong(defaultChannelId);
      long serverId = Long.parseLong(guildId);
      status.setValidDefaultChannel(!StringUtils.isEmpty(defaultChannelId) && this.discordClient != null && this.getChannel(serverId, channelId) != null);
    } catch (Exception e) {
      status.setValidDefaultChannel(false);
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
      return toPlayer(this.discordClient.getMember(serverId, memberId));
    }
    return null;
  }

  @Nullable
  public String getStartMessageId(long serverId, long channelId) {
    if (this.discordClient != null) {
      String topic = this.discordClient.getTopic(serverId, channelId);
      return CompetitionDataHelper.getStartMessageId(topic);
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

  public List<Player> getCompetitionPlayers(long serverId, long channelId) {
    if (this.discordClient != null) {
      DiscordCompetitionData competitionData = getCompetitionData(serverId, channelId);
      if (competitionData != null) {
        List<DiscordMember> competitionMembers = this.discordClient.getCompetitionMembers(serverId, channelId, competitionData.getStartMessageId(), competitionData.getUuid());
        DiscordMember owner = this.discordClient.getMember(serverId, this.getBotId());
        if (!competitionMembers.contains(owner)) {
          competitionMembers.add(0, owner);
        }
        return competitionMembers.stream().map(this::toPlayer).collect(Collectors.toList());
      }
      else {
        LOG.warn("No competition found for channel " + channelId);
      }
    }
    return Collections.emptyList();
  }

  public String sendMessage(long serverId, long channelId, String message) {
    if (this.discordClient != null) {
      return this.discordClient.sendMessage(serverId, channelId, message);
    }
    return null;
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

  /**
   * Returns the list of scores posted on the given channel.
   * If no message was found, the initial score is taken as single value.
   *
   * @param uuid      the uuid of the competition
   * @param serverId  the discord server id
   * @param channelId the discord channel id
   */
  @NonNull
  public ScoreList getScoreList(@NonNull HighscoreParser highscoreParser, @NonNull String uuid, long serverId, long channelId) {
    ScoreList result = new ScoreList();
    if (this.discordClient != null) {
      DiscordChannel channel = this.getChannel(serverId, channelId);
      DiscordCompetitionData data = this.getCompetitionData(serverId, channelId);
      if (data != null) {
        List<DiscordMessage> competitionUpdates = discordClient.getCompetitionUpdates(serverId, channelId, data.getStartMessageId(), uuid);
        LOG.info("Discord message search for " + data.getUuid() + " returned " + competitionUpdates.size() + " messages.");
        List<ScoreSummary> scores = competitionUpdates.stream().map(message -> toScoreSummary(highscoreParser, message)).collect(Collectors.toList());
        if (scores.isEmpty()) {
          ScoreSummary initialScore = CompetitionDataHelper.getDiscordCompetitionScore(this, serverId, data);
          result.setLatestScore(initialScore);
          result.getScores().add(initialScore);
          LOG.info("Because no score updated was found, only the initial competition highscore of channel '" + channel.getName() + "' was read.");
        }
        else {
          result.setScores(scores);
          result.setLatestScore(scores.get(0));
          LOG.info("Using latest message from channel for the update of '" + data.getName() + "', found " + result.getScores().size() + " highscore messages.");
        }
      }
    }
    return result;
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
      if (competitionData != null) {
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
        this.discordClient = DiscordClient.create(this, botToken, guildId, this);
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

  public Player getPlayerByInitials(long serverId, String initials) {
    for (DiscordMember member : this.getMembers(serverId)) {
      if (!StringUtils.isEmpty(member.getInitials()) && member.getInitials().equalsIgnoreCase(initials.toUpperCase())) {
        return toPlayer(member);
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
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.DISCORD_GUILD_ID) || propertyName.equals(PreferenceNames.DISCORD_BOT_TOKEN) || propertyName.equals(PreferenceNames.DISCORD_BOT_ALLOW_LIST)) {
      LOG.info("Detected Discord config change, updating BOT.");
      String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);
      String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);

      if (!StringUtils.isEmpty(botToken) && !StringUtils.isEmpty(guildId)) {
        LOG.info("Re-creating discord client because of preference changes.");
        this.discordClient = recreateDiscordClient();
      }
    }
  }

  private Player toPlayer(@NonNull DiscordMember member) {
    Player player = new Player();
    player.setId(member.getId());
    player.setName(member.getName());
    player.setInitials(member.getInitials());
    player.setAvatarUrl(member.getAvatarUrl());
    player.setDomain(PlayerDomain.DISCORD.name());
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

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    this.recreateDiscordClient();
  }

  @Override
  public void onDisconnect() {
    if(this.discordClient != null) {
      this.discordClient.shutdown();
      this.discordClient = null;
    }
  }
}
