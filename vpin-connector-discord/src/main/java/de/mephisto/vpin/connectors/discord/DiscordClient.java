package de.mephisto.vpin.connectors.discord;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 */
public class DiscordClient extends ListenerAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordClient.class);

  private final JDA jda;
  private final DiscordCommandResolver commandResolver;
  private final String guildId;
  private final List<DiscordMember> members;

  private List<String> commandsAllowList = new ArrayList<>();

  public DiscordClient(String botToken, String guildId, DiscordCommandResolver commandResolver) throws Exception {
    this.guildId = guildId.trim();
    jda = JDABuilder.createDefault(botToken.trim(), Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT))
        .setEventPassthrough(true)
        .setMemberCachePolicy(MemberCachePolicy.NONE)
        .build();
    this.commandResolver = commandResolver;
    jda.awaitReady();
    jda.addEventListener(this);
    members = new ArrayList<>();
    this.refreshMembers();
  }

  public long getBotId() {
    return jda.getSelfUser().getIdLong();
  }

  public void setTopic(long discordChannelId, String topic) {
    Guild guild = jda.getGuildById(guildId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, discordChannelId);
      if (channel != null) {
        channel.getManager().setTopic(topic).queue();
        LOG.info("Updated topic of '" + channel.getName() + "' to: " + topic);
      }
      else {
        LOG.error("No discord channel found for id '" + discordChannelId + "'");
      }
    }
  }

  public List<DiscordMember> getCompetitionMembers(long channelId, String afterMessageId, String competitionUuid) {
    List<DiscordMember> result = new ArrayList<>();
    Guild guild = jda.getGuildById(guildId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        MessageHistory history = MessageHistory.getHistoryAfter(channel, afterMessageId).complete();
        List<Message> messages = history.getRetrievedHistory();
        List<Message> botMessages = messages.stream().filter(m -> m.getAuthor().isBot()).collect(Collectors.toList());
        for (Message botMessage : botMessages) {
          if (botMessage.getContentRaw().contains(competitionUuid)) {
            Member member = botMessage.getMember();
            if(member != null) {
              DiscordMember discordMember = createMember(member);
              if(!result.contains(discordMember)) {
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

  public List<DiscordTextChannel> getChannels() {
    Guild guild = jda.getGuildById(guildId);
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
    this.jda.shutdownNow();
  }

  public String sendMessage(long channelId, String msg) {
    Guild guild = jda.getGuildById(guildId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        Message complete = textChannel.sendMessage(msg).complete();//.addFiles(FileUpload.fromData(file)).queue();
        LOG.info("Sent message '" + msg + "' to '" + textChannel.getName() + "'");
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

  public String getTopic(long channelId) {
    Guild guild = jda.getGuildById(guildId);
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

  public List<DiscordMember> getMembers() {
    return this.members;
  }

  public void refreshMembers() {
    this.refreshMembers(null, null);
  }

  public void refreshMembers(Consumer<List<DiscordMember>> c, Consumer<Throwable> t) {
    Guild guild = jda.getGuildById(guildId);
    if (guild == null) {
      throw new UnsupportedOperationException("No guild found for id '" + guildId + "'");
    }

    guild.loadMembers().onSuccess(members -> {
      this.members.clear();
      this.members.addAll(createMemberList(members));
      LOG.info("Successfully loaded " + this.members.size() + " members from " + guild.getName());

      if (c != null) {
        c.accept(this.members);
      }
    }).onError(throwable -> {
      LOG.error("Failed to load members from guildId {}: {}", guildId, throwable.getMessage(), throwable);
      if (t != null) {
        t.accept(throwable);
      }
    });
  }

  private List<DiscordMember> createMemberList(List<Member> members) {
    List<DiscordMember> result = new ArrayList<>();
    for (Member member : members) {
      if (member.getUser().isBot()) {
        continue;
      }

      result.add(createMember(member));
    }
    return result;
  }

  private DiscordMember createMember(Member member) {
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
    this.commandsAllowList = commandsAllowList;
  }


  /**
   * Checks if the given channel is configured for returning bot commands.
   */
  private boolean isValidChannel(MessageReceivedEvent event) {
    MessageChannelUnion channel = event.getChannel();
    if (channel instanceof PrivateChannel) {
      if (commandsAllowList.isEmpty()) {
        return true;
      }

      String name = event.getAuthor().getName();
      String id = event.getAuthor().getId();
      return commandsAllowList.contains(name) || commandsAllowList.contains(id);
    }

    return false;
  }

  /******************** Listener Methods ******************************************************************************/
  @Override
  public void onGuildMemberJoin(GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    LOG.info("Guild member join event " + event);
    this.refreshMembers();
  }

  @Override
  public void onGuildMemberUpdate(GuildMemberUpdateEvent event) {
    super.onGuildMemberUpdate(event);
    this.refreshMembers();
  }

  @Override
  public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
    super.onGuildMemberRemove(event);
    this.refreshMembers();
  }

  @Override
  public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
    super.onGuildMemberUpdateNickname(event);
    this.refreshMembers();
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }

    Message message = event.getMessage();
    String content = message.getContentRaw();
    if (content.startsWith("/") && isValidChannel(event)) {
      if (content.startsWith("/commands")) {
        MessageChannel channel = event.getChannel();
        channel.sendMessage("List of available commands:\n" +
            "**/competitions **: Returns the list and status of active competitions.\n" +
            "**/hs <TABLE NAME>**: Returns the highscore for the table matching the give name.\n" +
            "**/ranks **: Returns the overall player ranking.\n" +
            "**/player <PLAYER_INITIALS> **: Returns all data of this player.\n" +
            "").queue();
      }
      else if (commandResolver != null) {
        BotCommand command = new BotCommand(content, commandResolver);
        BotCommandResponse response = command.execute();
        if (response != null) {
          String result = response.toDiscordMarkup();
          if (result != null) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage(result).queue();
          }
        }
        else {
          LOG.info("Unknown bot command '" + content + "'");
        }
      }
    }
  }
}
