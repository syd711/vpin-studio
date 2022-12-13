package de.mephisto.vpin.connectors.discord;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 */
public class DiscordClient extends ListenerAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordClient.class);

  private final JDA jda;
  private final String guildId;
  private List<DiscordMember> members;

  public DiscordClient(String botToken, String guildId) throws Exception {
    this.guildId = guildId;
    jda = JDABuilder.createDefault(botToken, Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS))
        .setEventPassthrough(true)
        .setMemberCachePolicy(MemberCachePolicy.NONE)
        .build();
    jda.awaitReady();
    jda.addEventListener(this);
    members = new ArrayList<>();
    this.refreshMembers();
  }

  public void shutdown() {
    this.jda.shutdownNow();
  }

  public void sendMessage(String textChannelName, File file) {
    Guild guild = jda.getGuildById(guildId);
    if (guild != null) {
      List<GuildChannel> channels = guild.getChannels();
      for (GuildChannel channel : channels) {
        if (channel.getName().equals(textChannelName)) {
          if (!(channel instanceof TextChannel)) {
            continue;
          }
          TextChannel textChannel = (TextChannel) channel;
          textChannel.sendMessage("bubu").addFiles(FileUpload.fromData(file)).queue();
          return;
        }
      }
      throw new UnsupportedOperationException("No matching channel found for name " + textChannelName);
    }
    else {
      throw new UnsupportedOperationException("No guild found for guildId '" + this.guildId + "'");
    }
  }

  public void sendMessage(String textChannelName, String message) {
    Guild guild = jda.getGuildById(guildId);
    if (guild != null) {
      List<GuildChannel> channels = guild.getChannels();
      for (GuildChannel channel : channels) {
        if (channel.getName().equals(textChannelName)) {
          if (!(channel instanceof TextChannel)) {
            continue;
          }
          TextChannel textChannel = (TextChannel) channel;
          textChannel.sendMessage(message).queue();
          return;
        }
      }
      throw new UnsupportedOperationException("No matching channel found for name " + textChannelName);
    }
    else {
      throw new UnsupportedOperationException("No guild found for guildId '" + this.guildId + "'");
    }
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

  public void testMemberList(Consumer<List<DiscordMember>> c, Consumer<Throwable> t) {
    this.refreshMembers(c, t);
  }

  private List<DiscordMember> createMemberList(List<Member> members) {
    List<DiscordMember> result = new ArrayList<>();
    for (Member member : members) {
      String name = member.getEffectiveName();
      String initials = resolveInitials(name);

      DiscordMember discordMember = new DiscordMember();
      discordMember.setId(member.getIdLong());
      discordMember.setName(name);
      discordMember.setInitials(initials);
      discordMember.setAvatarUrl(member.getEffectiveAvatarUrl());
      result.add(discordMember);
    }
    return result;
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
}
