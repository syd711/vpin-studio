package de.mephisto.vpin.connectors.discord;


import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DiscordClient {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static List<String> ALLOW_LIST = Arrays.asList("bot-test-channel", "online-competitions", "offline-competitions");

  private final JDA jda;
  private final DiscordListenerAdapter listenerAdapter;
  private final String botToken;
  private final Map<Long, Guild> guilds = new HashMap<>();
  private final Map<Long, Message> messageCacheById = new HashMap<>();
  private final long botId;
  private long defaultGuildId;

  private final Map<Long, PinnedMessages> pinnedMessagesCache = new HashMap<>();

  public DiscordClient(String botToken, DiscordCommandResolver commandResolver) throws Exception {
    this.listenerAdapter = new DiscordListenerAdapter(this, commandResolver);
    this.botToken = botToken;

    jda = JDABuilder.createDefault(botToken.trim(), Arrays.asList(GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT))
        .setEventPassthrough(true)
        .setStatus(OnlineStatus.ONLINE)
        .setMemberCachePolicy(MemberCachePolicy.ALL)
        .addEventListeners(this.listenerAdapter)
        .build();
    jda.awaitReady();

    this.botId = jda.getSelfUser().getIdLong();
    this.loadMembers();
  }

  public boolean hasPermissions(long serverId, long channelId, long memberId, Permissions... permissions) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        Member member = guild.getMemberById(memberId);
        if (member != null) {
          List<Permission> permissionList = Arrays.stream(permissions).map(Permissions::toPermission).collect(Collectors.toList());
          boolean isAdmin = PermissionUtil.checkPermission(member, Permission.ADMINISTRATOR);
          return isAdmin || PermissionUtil.checkPermission(channel.getPermissionContainer(), member, permissionList.toArray(new Permission[0]));
        }
      }
    }
    return false;
  }

  public boolean hasPermissions(long serverId, long memberId, Permissions... permissions) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      Member member = guild.getMemberById(memberId);
      if (member != null) {
        List<Permission> permissionList = Arrays.stream(permissions).map(Permissions::toPermission).collect(Collectors.toList());
        boolean isAdmin = PermissionUtil.checkPermission(member, Permission.ADMINISTRATOR);
        return isAdmin || PermissionUtil.checkPermission(member, permissionList.toArray(new Permission[0]));
      }
    }
    return false;
  }

  public List<DiscordCategory> getCategories(long serverId) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      List<Category> categories = guild.getCategories();
      return categories.stream().map(category -> toCategory(category)).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public Category getCategory(long serverId, long categoryId) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      return guild.getCategoryById(categoryId);
    }
    return null;
  }

  public DiscordTextChannel createChannel(long serverId, long categoryId, String name, String topic) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      Category category = getCategory(serverId, categoryId);
      ChannelAction<TextChannel> textChannel = guild.createTextChannel(name, category);
      try {
        TextChannel channel = textChannel.submit().get();
        channel.getManager().setTopic(topic).queue();

        DiscordTextChannel t = new DiscordTextChannel();
        t.setId(channel.getIdLong());
        t.setName(channel.getName());

        long epochMilli = channel.getTimeCreated().toInstant().toEpochMilli();
        t.setCreationDate(new Date(epochMilli));

        return t;
      }
      catch (Exception e) {
        LOG.error("Failed to create text channel \"" + name + "\": " + e.getMessage(), e);
      }
    }
    return null;
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

  public long getDefaultGuildId() {
    return defaultGuildId;
  }

  public void setDefaultGuildId(long defaultGuildId) {
    this.defaultGuildId = defaultGuildId;
  }

  public GuildInfo getGuildById(long guildId) {
    Guild guild = getGuild(guildId);
    if (guild != null) {
      Member memberById = guild.getMemberById(botId);
      return new GuildInfo(guild, PermissionUtil.checkPermission(memberById, Permission.ADMINISTRATOR));
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
    return jda.getGuilds().stream().map(guild -> {
      Member memberById = guild.getMemberById(botId);
      return new GuildInfo(guild, PermissionUtil.checkPermission(memberById, Permission.ADMINISTRATOR));
    }).collect(Collectors.toList());
  }

  public List<GuildInfo> getAdministratedGuilds() {
    long botId = jda.getSelfUser().getIdLong();
    return jda.getGuilds().stream().filter(guild -> {
      Member memberById = guild.getMemberById(botId);
      return PermissionUtil.checkPermission(memberById, Permission.ADMINISTRATOR);
    }).map(guild -> new GuildInfo(guild, true)).collect(Collectors.toList());
  }

  public long getBotId() {
    return botId;
  }

  public String getBotToken() {
    return botToken;
  }

  public List<DiscordMessage> getPinnedMessages(long serverId, long channelId) {
    if (pinnedMessagesCache.containsKey(channelId)) {
      return new ArrayList<>(pinnedMessagesCache.get(channelId).getMessages());
    }
    try {
      Guild guild = getGuild(serverId);
      if (guild != null) {
        TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
        if (channel != null) {
          long start = System.currentTimeMillis();
          List<Message> complete = channel.retrievePinnedMessages().complete();
          for (Message message : complete) {
            this.messageCacheById.put(message.getIdLong(), message);
          }

          LOG.info("Pinned messages fetch for channel \"" + channel.getName() + "\" took " + (System.currentTimeMillis() - start) + "ms.");
          List<DiscordMessage> collect = complete.stream().map(this::toMessage).collect(Collectors.toList());
          pinnedMessagesCache.put(channelId, new PinnedMessages());
          pinnedMessagesCache.get(channelId).getMessages().addAll(collect);
          return collect;
        }
        else {
          LOG.error("No discord channel found for id '" + channelId + "' to read pinned messages from.");
        }
      }
    }
    catch (Exception e) {
      LOG.error("Error reading pinned messages: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public void setSlowMode(long serverId, long channelId, int seconds) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) { //TODO required?
//        channel.getManager().setSlowmode(seconds);
      }
    }
  }

  public void pinMessage(long serverId, long channelId, long messageId) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        channel.pinMessageById(messageId).complete();
        if (!this.pinnedMessagesCache.containsKey(channelId)) {
          this.pinnedMessagesCache.put(channelId, new PinnedMessages());
        }

        DiscordMessage msg = toMessage(getMessage(serverId, channelId, messageId));
        this.pinnedMessagesCache.get(channelId).getMessages().add(msg);

        try {
          MessageHistory complete = MessageHistory.getHistoryAfter(channel, String.valueOf(messageId)).complete();
          List<Message> retrievedHistory = complete.getRetrievedHistory();
          for (Message message : retrievedHistory) {
            if (message.getType().equals(MessageType.CHANNEL_PINNED_ADD)) {
              channel.deleteMessageById(message.getId()).complete();
            }
          }
          invalidateMessageCache(channelId, -1);
        }
        catch (Exception e) {
          LOG.error("Failed to cleanup pin messages: " + e.getMessage(), e);
        }
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
  }

  public void unpinMessage(long serverId, long channelId, long messageId) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        LOG.info("Unpinned message " + messageId);
        channel.unpinMessageById(messageId).complete();

        if (this.pinnedMessagesCache.containsKey(channelId)) {
          DiscordMessage msg = toMessage(getMessage(serverId, channelId, messageId));
          this.pinnedMessagesCache.get(channelId).getMessages().remove(msg);
        }
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
  }

  public void setTopic(long serverId, long channelId, String topic) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        String existingTopic = this.getTopic(serverId, channelId);
        if (!String.valueOf(existingTopic).equals(topic)) {
          LOG.info("Updating topic of '" + channel.getName() + "' (length of " + topic.length() + " characters)");
          channel.getManager().setTopic(topic).queue();
        }
        else {
          LOG.warn("Skipped topic update, the existing topic is equals to the update: '" + topic + "'");
        }
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
  }

  public void invalidateMessageCache(long channelId, long originUserId) {
    long botId = this.getBotId();
    if (botId != originUserId) {
      if (pinnedMessagesCache.containsKey(channelId)) {
        pinnedMessagesCache.remove(channelId);
        LOG.info("Invalidated Discord pinned messages cache for channel " + channelId);
      }
    }
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

  public DiscordMember getMember(long memberId) {
    User user = this.jda.getUserById(memberId);
    if (user != null) {
      return toMember(user);
    }
    return null;
  }

  public List<DiscordTextChannel> getChannels() {
    return getChannels(-1);
  }

  public void deleteChannel(long serverId, long channelId) {
    Guild guild = this.getGuild(serverId);
    if (guild != null) {
      try {
        guild.getTextChannelById(channelId).delete().complete();
      }
      catch (Exception e) {
        LOG.error("Channel deletion failed: " + e.getMessage());
      }
    }
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

          long epochMilli = channel.getTimeCreated().toInstant().toEpochMilli();
          t.setCreationDate(new Date(epochMilli));

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
        this.messageCacheById.put(complete.getIdLong(), complete);
        return complete.getIdLong();
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for default guildId '" + serverId + "'");
    }
    return -1;
  }

  public long sendMessage(long serverId, long channelId, MessageEmbed msg) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        try {
          Message complete = textChannel.sendMessage("").addEmbeds(msg).complete();
          this.messageCacheById.put(complete.getIdLong(), complete);
          return complete.getIdLong();
        }
        catch (Exception e) {
          LOG.error("Sending discord message failed: {}", e.getMessage(), e);
          return -1;
        }
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for default guildId '" + serverId + "'");
    }
    return -1;
  }

  public long sendEmbeddedMessage(long serverId, long channelId, String msg) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        Message complete = textChannel.sendMessage(msg).complete();
        this.messageCacheById.put(complete.getIdLong(), complete);
        return complete.getIdLong();
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for default guildId '" + serverId + "'");
    }
    return -1;
  }

  public long sendMessage(long serverId, long channelId, String msg, byte[] image, String name, String imageText) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedBuilder embedBuilder = embed.setImage("attachment://" + URLEncoder.encode(name, StandardCharsets.UTF_8));
        if (imageText != null) {
          embedBuilder.setDescription(imageText);
        }
        Message complete = textChannel.sendMessage(msg).addFiles(FileUpload.fromData(image, name)).setEmbeds(embed.build()).complete();
        this.messageCacheById.put(complete.getIdLong(), complete);
        return complete.getIdLong();
      }
      else {
        LOG.error("No discord channel found for id '" + channelId + "'");
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for default guildId '" + serverId + "'");
    }
    return -1;
  }

  public String getTopic(long serverId, long channelId) {
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
      if (textChannel != null) {
        String topic = textChannel.getTopic();
        if (topic == null) {
          return "";
        }
        return topic;
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

  private DiscordMessage toMessage(Message msg) {
    DiscordMessage message = new DiscordMessage();
    long epochMilli = msg.getTimeCreated().toInstant().toEpochMilli();
    Date createdAt = new Date(epochMilli);

    message.setId(msg.getIdLong());
    message.setCreatedAt(createdAt);
    message.setRaw(msg.getContentRaw());
    message.setServerId(msg.getGuild().getIdLong());

    if (msg.getMember() != null) {
      DiscordMember discordMember = toMember(msg.getMember());
      message.setMember(discordMember);
    }

    List<MessageEmbed> embeds = msg.getEmbeds();
    for (MessageEmbed embed : embeds) {
      message.setEmbedDescription(embed.getDescription());
    }
    return message;
  }

  private DiscordMember toMember(Member member) {
    String name = member.getEffectiveName();
    String initials = resolveInitials(name);

    DiscordMember discordMember = new DiscordMember();
    discordMember.setId(member.getIdLong());
    discordMember.setName(name);
    discordMember.setDisplayName(member.getEffectiveName());
    discordMember.setInitials(initials);
    discordMember.setBot(member.getUser().isBot());
    discordMember.setAvatarUrl(member.getEffectiveAvatarUrl());
    return discordMember;
  }

  private DiscordMember toMember(User member) {
    String name = member.getName();
    String initials = resolveInitials(name);

    DiscordMember discordMember = new DiscordMember();
    discordMember.setId(member.getIdLong());
    discordMember.setName(name);
    discordMember.setInitials(initials);
    discordMember.setBot(member.isBot());
    discordMember.setAvatarUrl(member.getEffectiveAvatarUrl());
    return discordMember;
  }

  private DiscordCategory toCategory(Category category) {
    DiscordCategory c = new DiscordCategory();
    c.setId(category.getIdLong());
    c.setName(category.getName());
    return c;
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
  public void setActivity(String status) {
    try {
      if (status == null) {
        this.jda.getPresence().setActivity(null);
      }
      else {
        this.jda.getPresence().setActivity(Activity.playing("\"" + status + "\""));
      }
    }
    catch (Exception e) {
      LOG.error("Discord activity update failed: " + e.getMessage());
    }
  }

  public void setCommandsAllowList(List<Long> commandsAllowList) {
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
        guilds.put(id, null);
        return null;
      }

    }

    return guilds.get(id);
  }

  public Message getMessage(long serverId, long channelId, long messageId) {
    if (messageCacheById.containsKey(messageId)) {
      return messageCacheById.get(messageId);
    }

    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
      if (channel != null) {
        Message message = channel.retrieveMessageById(messageId).complete();
        if (message != null) {
          messageCacheById.put(messageId, message);
          return message;
        }
      }
    }
    return null;
  }

  public void clearCache() {
    this.messageCacheById.clear();
    this.pinnedMessagesCache.clear();
    this.guilds.clear();
    LOG.info("Cleared Discord client cache.");
  }
}
