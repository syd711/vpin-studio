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

  private final JDA jda;
  private final String guildId;
  private final DiscordListenerAdapter listenerAdapter;
  private final Map<Long, Guild> guilds = new HashMap<>();
  private final long botId;

  private final DiscordCache<List<DiscordMessage>> messageCache = new DiscordCache<>();

  public DiscordClient(String botToken, String guildId, DiscordCommandResolver commandResolver) throws Exception {
    this.guildId = guildId.trim();
    jda = JDABuilder.createDefault(botToken.trim(), Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT))
        .setEventPassthrough(true)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .build();
    this.listenerAdapter = new DiscordListenerAdapter(this, commandResolver);
    jda.awaitReady();
    jda.addEventListener(this.listenerAdapter);
    this.botId = jda.getSelfUser().getIdLong();
  }

  public GuildInfo getGuildById(long guildId) {
    Guild guild = getGuild(guildId);
    if (guild != null) {
      return new GuildInfo(guild);
    }
    return null;
  }

  public List<DiscordMember> getMembers() {
    return this.getMembers(Long.parseLong(guildId));
  }

  public synchronized List<DiscordMember> getMembers(long serverId) {
    Guild guild = getGuild(serverId);
    List<Member> members = guild.loadMembers().get();
    return members.stream().map(this::toMember).collect(Collectors.toList());
  }

  public List<GuildInfo> getGuilds() {
    return jda.getGuilds().stream().map(GuildInfo::new).collect(Collectors.toList());
  }

  public long getBotId() {
    return botId;
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

  public List<DiscordMember> getCompetitionMembers(long serverId, long channelId, String afterMessageId, String competitionUuid) {
    List<DiscordMember> result = new ArrayList<>();
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        MessageHistory history = MessageHistory.getHistoryAfter(channel, afterMessageId).complete();
        List<Message> messages = history.getRetrievedHistory();
        List<Message> botMessages = messages.stream().filter(m -> m.getAuthor().isBot()).collect(Collectors.toList());
        for (Message botMessage : botMessages) {
          if (botMessage.getContentRaw().contains(competitionUuid)) {
            Member member = botMessage.getMember();
            if (member != null) {
              DiscordMember discordMember = toMember(member);
              if (!result.contains(discordMember)) {
                result.add(discordMember);
              }
            }
          }
        }
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    return result;
  }

  public List<DiscordMessage> getCompetitionUpdates(long serverId, long channelId, String afterMessageId, String competitionUuid) {
    List<DiscordMessage> result = new ArrayList<>();
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        MessageHistory history = MessageHistory.getHistoryAfter(channel, afterMessageId).complete();
        List<Message> messages = new ArrayList<>(history.getRetrievedHistory());
        messages.sort((o1, o2) -> o2.getTimeCreated().compareTo(o1.getTimeCreated()));

        List<Message> botMessages = messages.stream().filter(m -> m.getAuthor().isBot()).collect(Collectors.toList());
        for (Message botMessage : botMessages) {
          if (botMessage.getContentRaw().contains(competitionUuid)) {
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
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    return result;
  }

  public void invalidateMessageCache(long channelId) {
    messageCache.invalidate(channelId);
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
          PermissionOverride po = channel.getPermissionContainer().getPermissionOverride((IPermissionHolder) guild.getRolesByName("@everyone", true).toArray()[0]);
          if (po != null && po.getDenied().contains(Permission.VIEW_CHANNEL)) {
            continue;
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

  public String sendMessage(long serverId, long channelId, String msg) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        Message complete = textChannel.sendMessage(msg).complete();
        return complete.getId();
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for guildId '" + this.guildId + "'");
    }
    return null;
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
    long id = Long.parseLong(guildId);
    if (serverId > 0) {
      id = serverId;
    }

    if (!guilds.containsKey(id)) {
      Guild guildById = jda.getGuildById(id);
      if(guildById != null) {
        LOG.info("Cached guild '" + guildById.getName() + "'");
        guilds.put(id, guildById);
      }
      else {
        throw new UnsupportedOperationException("No guild found for id " + serverId);
      }

    }

    return guilds.get(id);
  }
}
