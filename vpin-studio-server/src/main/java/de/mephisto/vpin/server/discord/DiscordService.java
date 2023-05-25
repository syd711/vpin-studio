package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.*;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.*;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.dv8tion.jda.api.entities.Message;
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
  public DiscordBotStatus getStatus() {
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    String defaultChannelId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_CHANNEL_ID);
    long botId = this.getBotId();

    DiscordBotStatus status = new DiscordBotStatus();
    status.setBotId(botId);
    status.setValid(botId != -1 && this.discordClient != null && this.discordClient.getGuilds().size() > 0);
    if (botId != -1) {
      status.setBotInitials(this.discordClient.getBot().getInitials());
      try {
        long channelId = Long.parseLong(defaultChannelId);
        long serverId = Long.parseLong(guildId);
        status.setValidDefaultChannel(!StringUtils.isEmpty(defaultChannelId) && this.discordClient != null && this.getChannel(serverId, channelId) != null);
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
      return toPlayer(this.discordClient.getMember(serverId, memberId));
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

  public List<Player> getCompetitionPlayers(long serverId, long channelId, String uuid) {
    if (this.discordClient != null) {
      DiscordCompetitionData competitionData = getCompetitionData(serverId, channelId, uuid);
      if (competitionData != null) {
        List<DiscordMember> competitionMembers = this.discordClient.getCompetitionMembers(serverId, channelId, competitionData.getMsgId(), competitionData.getUuid(), Long.parseLong(competitionData.getOwner()));
        DiscordMember owner = this.discordClient.getMember(serverId, this.getBotId());
        if (!competitionMembers.contains(owner)) {
          competitionMembers.add(0, owner);
        }
        return competitionMembers.stream().map(this::toPlayer).collect(Collectors.toList());
      }
      else {
        LOG.warn("No competition found for channel " + channelId + " during player search.");
      }
    }
    return Collections.emptyList();
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
  public ScoreList getScoreList(@NonNull HighscoreParser highscoreParser, @NonNull String uuid, long serverId, long channelId) {
    ScoreList result = new ScoreList();
    if (this.discordClient != null) {
      DiscordCompetitionData data = this.getCompetitionData(serverId, channelId, uuid);
      if (data != null) {
        List<DiscordMessage> competitionUpdates = discordClient.getCompetitionUpdates(serverId, channelId, data.getMsgId(), uuid);
        for (DiscordMessage competitionUpdate : competitionUpdates) {
          ScoreSummary scoreSummary = toScoreSummary(highscoreParser, competitionUpdate);
          if(!scoreSummary.getScores().isEmpty())  {
            result.getScores().add(scoreSummary);
          }
        }

        if (!result.getScores().isEmpty()) {
          result.setLatestScore(result.getScores().get(0));
        }
        else {
          LOG.info("No record highscore for " + uuid + " found, so this seems to be the first one.");
        }
      }
      else {
        LOG.error("No competition data found for '" + uuid + "', unable to calculated highscore updates for this competition.");
      }
    }
    return result;
  }

  public boolean isEnabled() {
    return this.discordClient != null;
  }

  public DiscordCompetitionData getLatestCompetitionData(long serverId, long channelId, String uuid) {
    if (this.discordClient != null) {
      ChannelTopic channelTopic = this.getTopicData(serverId, channelId);
      if (channelTopic != null) {
        if (channelTopic.getMessageId() > 0) {
          Message message = discordClient.getMessage(serverId, channelId, channelTopic.getMessageId());
          if (message != null) {
            DiscordCompetitionData competitionData = CompetitionDataHelper.getCompetitionData(message);
            if(competitionData.getUuid().equals(uuid)) {
              return competitionData;
            }
          }
        }

        List<DiscordMessage> messageHistory = this.discordClient.getMessageHistoryAfter(serverId, channelId, channelTopic.getMessageId(), uuid);
        Collections.sort(messageHistory, Comparator.comparing(DiscordMessage::getCreatedAt));

        LOG.info("Competition search returned " + messageHistory.size() + " messages, using last messageId '" + channelTopic.getMessageId() + "'");
        DiscordMessage lastCompetitionStartMessage = null;
        for (DiscordMessage discordMessage : messageHistory) {
          if (discordMessage.getRaw().contains(DiscordChannelMessageFactory.START_INDICATOR)) {
            lastCompetitionStartMessage = discordMessage;
          }
        }

        //check if there was a cancellation or finished method posted for the last started competition
        if (lastCompetitionStartMessage != null) {
          DiscordCompetitionData competitionData = CompetitionDataHelper.getCompetitionData(lastCompetitionStartMessage);
          if (competitionData != null) {
            Optional<DiscordMessage> abortMessage = messageHistory
                .stream()
                .filter(m -> m.getRaw().toLowerCase().contains(DiscordChannelMessageFactory.FINISHED_INDICATOR) ||
                    m.getRaw().toLowerCase().contains(DiscordChannelMessageFactory.CANCEL_INDICATOR))
                .filter(m -> m.getRaw().contains(competitionData.getUuid())).findFirst();
            if (abortMessage.isPresent()) {
              LOG.info("No competition data found searching for " + competitionData.getUuid() + ", this competition has already been canceled.");
              return null;
            }
            return competitionData;
          }
        }
      }
    }
    return null;
  }

  public DiscordCompetitionData getCompetitionData(long serverId, long channelId, String uuid) {
    if (this.discordClient != null) {
      DiscordCompetitionData competitionData = getLatestCompetitionData(serverId, channelId, uuid);
      if (competitionData == null) {
        LOG.info("No competition data found for " + uuid);
      }
      else {
        if (!competitionData.getUuid().equals(uuid)) {
          LOG.warn("The last competition data found was for '" + competitionData.getName() + "', no matching UUID " + uuid);
        }
        return competitionData;
      }
    }
    return null;
  }

  private ChannelTopic getTopicData(long serverId, long channelId) {
    if (this.discordClient != null) {
      String topic = this.discordClient.getTopic(serverId, channelId);
      return ChannelTopic.toChannelTopic(topic);
    }
    return null;
  }

  private DiscordClient recreateDiscordClient() {
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
      for (DiscordMember member : this.getMembers(serverId)) {
        if (!StringUtils.isEmpty(member.getInitials()) && member.getInitials().equalsIgnoreCase(initials.toUpperCase())) {
          return toPlayer(member);
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
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
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

  public void updateTopicTimestamp(long serverId, long channelId, long messageId) {
    String topic = discordClient.getTopic(serverId, channelId);
    ChannelTopic channelTopic = ChannelTopic.toChannelTopic(topic);
    if (channelTopic == null || channelTopic.isOlderThanToday()) {
      channelTopic = new ChannelTopic();
      channelTopic.setTimestamp(new Date().getTime());
      channelTopic.setMessageId(messageId);
      discordClient.setTopic(serverId, channelId, channelTopic.toTopic());
    }
  }

  public boolean isCompetitionActive(long serverId, long channelId, String uuid) {
    return this.getCompetitionData(serverId, channelId, uuid) != null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    this.recreateDiscordClient();
  }
}
