package de.mephisto.vpin.connectors.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DiscordListenerAdapter extends ListenerAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordListenerAdapter.class);

  private List<String> commandsAllowList = new ArrayList<>();
  private final DiscordCommandResolver commandResolver;

  public DiscordListenerAdapter(DiscordCommandResolver commandResolver) {
    this.commandResolver = commandResolver;
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
    DiscordMemberCache.invalidateAll();
    DiscordMembersCache.invalidate(event.getGuild().getIdLong());
  }

  @Override
  public void onGuildMemberUpdate(GuildMemberUpdateEvent event) {
    super.onGuildMemberUpdate(event);
    DiscordMemberCache.invalidateAll();
    DiscordMembersCache.invalidate(event.getGuild().getIdLong());
  }

  @Override
  public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
    super.onGuildMemberRemove(event);
    DiscordMemberCache.invalidateAll();
    DiscordMembersCache.invalidate(event.getGuild().getIdLong());
  }

  @Override
  public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
    super.onGuildMemberUpdateNickname(event);
    DiscordMemberCache.invalidateAll();
    DiscordMembersCache.invalidate(event.getGuild().getIdLong());
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
