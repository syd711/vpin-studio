package de.mephisto.vpin.connectors.discord;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DiscordClient {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordClient.class);

  private final static List<String> ALLOW_LIST = Arrays.asList("bot-test-channel", "online-competitions", "offline-competitions");

  private final JDA jda;
  private final DiscordListenerAdapter listenerAdapter;
  private final String botToken;
  private final Map<Long, Guild> guilds = new HashMap<>();
  private final long botId;
  private long defaultGuildId;

  private final DiscordCache<List<DiscordMessage>> messageCache = new DiscordCache<>();

  private DiscordClient(JDA jda, DiscordCommandResolver commandResolver, String botToken) {
    this.jda = jda;
    this.botId = jda.getSelfUser().getIdLong();
    this.listenerAdapter = new DiscordListenerAdapter(this, commandResolver);
    this.botToken = botToken;
    this.loadMembers();
    jda.addEventListener(this.listenerAdapter);
  }

  public DiscordMember getBot() {
    DiscordMember member = new DiscordMember();
    member.setName(jda.getSelfUser().getName());
    member.setInitials(resolveInitials(jda.getSelfUser().getName()));
    member.setId(this.botId);
    member.setAvatarUrl(jda.getSelfUser().getAvatarUrl());
    return member;
  }

  public void loadMembers() {
    List<Guild> guilds = this.jda.getGuilds();
    for (Guild guild : guilds) {
      guild.loadMembers().onSuccess(members -> {
        LOG.info("Loaded " + members.size() + " members for " + guild.getName());
      });
    }
  }

  public static DiscordClient create(String botToken, DiscordCommandResolver commandResolver) throws Exception {
    JDA jda = JDABuilder.createDefault(botToken.trim(), Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT))
        .setEventPassthrough(true)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .build();
    jda.awaitReady();
    return new DiscordClient(jda, commandResolver, botToken);
  }

  public long getDefaultGuildId() {
    return defaultGuildId;
  }

  public void setDefaultGuildId(long defaultGuildId) {
    this.defaultGuildId = defaultGuildId;
  }

  public GuildInfo getGuildById(long guildId) {
    Guild guild = getGuild(guildId);
    if (guild != null) {
      return new GuildInfo(guild);
    }
    return null;
  }

  public List<DiscordMember> getMembers() {
    if (defaultGuildId > 0) {
      return this.getMembers(defaultGuildId);
    }
    return Collections.emptyList();
  }

  public synchronized List<DiscordMember> getMembers(long serverId) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      List<Member> members = guild.getMembers();
      return members.stream().map(this::toMember).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public List<GuildInfo> getGuilds() {
    return jda.getGuilds().stream().map(GuildInfo::new).collect(Collectors.toList());
  }

  public long getBotId() {
    return botId;
  }

  public String getBotToken() {
    return botToken;
  }

  public void setTopic(long serverId, long channelId, String topic) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        LOG.info("Updating topic of '" + channel.getName() + "' (length of " + topic.length() + " characters)");
        channel.getManager().setTopic(topic).queue();
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
  }

  public List<DiscordMember> getCompetitionMembers(long serverId, long channelId, long afterMessageId, String competitionUuid) {
    List<DiscordMessage> messageHistory = getMessageHistory(serverId, channelId, afterMessageId, competitionUuid);
    List<DiscordMember> result = new ArrayList<>();
    for (DiscordMessage discordMessage : messageHistory) {
      DiscordMember member = discordMessage.getMember();
      if (member != null) {
        if (!result.contains(member)) {
          result.add(member);
        }
      }
    }
    return result;
  }

  public List<DiscordMessage> getCompetitionUpdates(long serverId, long channelId, long afterMessageId, String competitionUuid) {
    return getMessageHistory(serverId, channelId, afterMessageId, competitionUuid);
  }

  public void invalidateMessageCache(long channelId) {
    messageCache.invalidate(channelId);
    LOG.info("Invalidated Discord competition message cache for " + channelId);
  }

  public DiscordMember getMember(long serverId, long memberId) {
    Guild guild = this.getGuild(serverId);
    if (guild != null) {
      Member member = guild.getMemberById(memberId);
      if (member != null) {
        return toMember(member);
      }
    }

    return null;
  }

  public List<DiscordTextChannel> getChannels() {
    return getChannels(-1);
  }

  public List<DiscordTextChannel> getChannels(long serverId) {
    Guild guild = this.getGuild(serverId);
    List<DiscordTextChannel> channelList = new ArrayList<>();
    if (guild != null) {
      List<GuildChannel> channels = guild.getChannels();
      for (GuildChannel channel : channels) {
        if (channel instanceof TextChannel) {
          if (!ALLOW_LIST.contains(channel.getName())) {
            PermissionOverride po = channel.getPermissionContainer().getPermissionOverride((IPermissionHolder) guild.getRolesByName("@everyone", true).toArray()[0]);
            if (po != null && po.getDenied().contains(Permission.VIEW_CHANNEL)) {
              continue;
            }
          }
          DiscordTextChannel t = new DiscordTextChannel();
          t.setId(channel.getIdLong());
          t.setName(channel.getName());
          channelList.add(t);
        }
      }
    }
    return channelList;
  }

  public void shutdown() {
    guilds.clear();
    this.jda.shutdownNow();
  }

  public long sendMessage(long serverId, long channelId, String msg) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        Message complete = textChannel.sendMessage(msg).complete();
        return complete.getIdLong();
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for default guildId '" + this.defaultGuildId + "'");
    }
    return -1;
  }

  public String getTopic(long serverId, long channelId) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        return textChannel.getTopic();
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    else {
      LOG.warn("Unable to retrieve topic from channel '" + channelId + "', no connection.");
    }
    return null;
  }

  private List<DiscordMessage> getMessageHistory(long serverId, long channelId, long afterMessageId, String uuid) {
    if (!messageCache.contains(channelId)) {
      Guild guild = getGuild(serverId);
      if (guild != null) {
        TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
        if (channel != null) {
          MessageHistory history = MessageHistory.getHistoryAfter(channel, String.valueOf(afterMessageId)).complete();
          List<Message> messages = new ArrayList<>(history.getRetrievedHistory());
          messages.sort((o1, o2) -> o2.getTimeCreated().compareTo(o1.getTimeCreated()));
          List<Message> botMessages = messages.stream().filter(m -> m.getAuthor().isBot()).collect(Collectors.toList());

          List<DiscordMessage> result = new ArrayList<>();
          for (Message botMessage : botMessages) {
            if (botMessage.getContentRaw().contains(uuid)) {
              Member member = botMessage.getMember();
              if (member != null) {
                DiscordMessage message = new DiscordMessage();
                DiscordMember discordMember = toMember(member);
                long epochMilli = botMessage.getTimeCreated().toInstant().toEpochMilli();
                Date createdAt = new Date(epochMilli);

                message.setMember(discordMember);
                message.setCreatedAt(createdAt);
                message.setRaw(botMessage.getContentRaw());
                message.setServerId(serverId);
                result.add(message);
              }
            }
          }

          LOG.info("Discord message search for " + uuid + " returned " + result.size() + " messages.");
          messageCache.put(channelId, result);
        }
      }
    }
    return messageCache.get(channelId);
  }

  private DiscordMember toMember(Member member) {
    String name = member.getEffectiveName();
    String initials = resolveInitials(name);

    DiscordMember discordMember = new DiscordMember();
    discordMember.setId(member.getIdLong());
    discordMember.setName(name);
    discordMember.setInitials(initials);
    discordMember.setAvatarUrl(member.getEffectiveAvatarUrl());
    return discordMember;
  }

  /**
   * The pattern to match the user to initials
   *
   * @param effectiveName the name of the Discord User.
   */
  private String resolveInitials(String effectiveName) {
    String initials = null;
    if (effectiveName.contains("|") && effectiveName.contains("[")) {
      initials = effectiveName.substring(effectiveName.indexOf("[") + 1);
      if (initials.contains("]")) {
        initials = initials.substring(0, initials.indexOf("]"));
      }
      if (initials.length() > 3) {
        initials = initials.substring(0, 3);
      }
    }
    return initials;
  }

  /**
   * Updates the online status with the active game info.
   */
  public void setStatus(String status) {
    if (status == null) {
      this.jda.getPresence().setActivity(null);
    }
    else {
      this.jda.getPresence().setActivity(Activity.playing("\"" + status + "\""));
    }
  }

  public void setCommandsAllowList(List<String> commandsAllowList) {
    this.listenerAdapter.setCommandsAllowList(commandsAllowList);
  }

  private Guild getGuild(long serverId) {
    long id = defaultGuildId;
    if (serverId > 0) {
      id = serverId;
    }

    if (!guilds.containsKey(id)) {
      Guild guildById = jda.getGuildById(id);
      if (guildById != null) {
        LOG.info("Cached guild '" + guildById.getName() + "'");
        guilds.put(id, guildById);
      }
      else {
        LOG.error("No guild found for id \"" + serverId + "\"");
        return null;
      }

    }

    return guilds.get(id);
  }

}
