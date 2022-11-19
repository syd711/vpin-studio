package de.mephisto.vpin.connectors.discord;


import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BOT link:
 * https://discord.com/api/oauth2/authorize?client_id=1043190061165989979&permissions=2048&scope=bot
 */
public class DiscordClient extends ListenerAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordClient.class);

  private final JDA jda;
  private final String guildId;
  private List<DiscordMember> members;

  public DiscordClient(String botToken, String guildId) throws Exception {
    this.guildId = guildId;
//    String url = "https://discord.com/api/webhooks/1043202008133423186/zWtLf7jqPi5C8GrNqB-svt9xQSJ6QFzQmPrEGBZ3ugezYy6NqvgK03EvZyJJTOTUlkR0";

    jda = JDABuilder.createDefault(botToken, Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS))
        .setEventPassthrough(true)
        .build();
    jda.awaitReady();
    jda.addEventListener(this);
    members = new ArrayList<>();
  }

  public void shutdown() {
    this.jda.shutdownNow();
  }

  public void sendMessage(@NonNull String textChannelName, @NonNull String message) {
    Guild guild = jda.getGuildById(guildId);
    if (guild != null) {
      List<GuildChannel> channels = guild.getChannels();
      for (GuildChannel channel : channels) {
        if (channel.getName().equals(textChannelName)) {
          if (!(channel instanceof TextChannel)) {
            throw new UnsupportedOperationException("Channel '" + textChannelName + "' is not a text channel.");
          }
          TextChannel textChannel = (TextChannel) channel;
          textChannel.sendMessage(message).queue();
        }
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for guildId '" + this.guildId + "'");
    }
  }

  public List<DiscordMember> getMembers() {
    return this.members;
  }

  public void refreshMembers() {
    Guild guild = jda.getGuildById(guildId);
    if (guild == null) {
      throw new UnsupportedOperationException("No guild found for id '" + guildId + "'");
    }

    guild.loadMembers().onSuccess(members -> {
      this.members.clear();
      for (Member member : members) {
        DiscordMember discordMember = new DiscordMember();
        discordMember.setName(member.getEffectiveName());
        discordMember.setInitials(resolveInitials(member.getEffectiveName()));
        discordMember.setAvatarUrl(member.getEffectiveAvatarUrl());

        this.members.add(discordMember);
      }
    }).onError(throwable -> LOG.error("Failed to load members from guildId {}: {}", guildId, throwable.getMessage(), throwable));
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

  public void callWebHook(@NonNull String url, @NonNull String message, @Nullable String avatarUrl) throws IOException {
    DiscordWebhook hook = new DiscordWebhook(url);
    hook.setTts(false);
    hook.setAvatarUrl(avatarUrl);
    hook.setContent(message);
    hook.execute();
  }

  /******************** Listener Methods ******************************************************************************/
  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
    LOG.info("Guild member join event " + event);
    this.refreshMembers();
  }

  @Override
  public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
    super.onGuildMemberUpdate(event);
    this.refreshMembers();
  }

  @Override
  public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
    super.onGuildMemberRemove(event);
    this.refreshMembers();
  }

  @Override
  public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
    super.onGuildMemberUpdateNickname(event);
    this.refreshMembers();
  }
}
