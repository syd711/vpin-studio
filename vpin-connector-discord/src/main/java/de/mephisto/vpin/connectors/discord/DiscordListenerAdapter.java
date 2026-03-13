package de.mephisto.vpin.connectors.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class DiscordListenerAdapter extends ListenerAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private List<Long> commandsAllowList = new ArrayList<>();
  private final DiscordClient discordClient;
  private final DiscordCommandResolver commandResolver;

  public DiscordListenerAdapter(DiscordClient discordClient, DiscordCommandResolver commandResolver) {
    this.discordClient = discordClient;
    this.commandResolver = commandResolver;
  }

  public void setCommandsAllowList(List<Long> commandsAllowList) {
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

      long id = event.getAuthor().getIdLong();
      boolean isAllowed = commandsAllowList.contains(id);
      if (isAllowed) {
        return true;
      }
    }

    LOG.info("Discord message has been filtered by the allow list.");
    return false;
  }

  @Override
  public void onMessageDelete(MessageDeleteEvent event) {
    super.onMessageDelete(event);
    long channelId = event.getChannel().getIdLong();
    //don't! discordClient.invalidateMessageCache(channelId, -1);
  }


  @Override
  public void onReady(ReadyEvent event) {

  }

  @Override
  public void onGuildReady(GuildReadyEvent event) {
//    Guild guild = event.getGuild();
//    List<CommandData> commandData = new ArrayList<>();
//    OptionData option1 = new OptionData(OptionType.STRING, "name", "The name of the table or a matching string.", true);
//    commandData.add(Commands.slash("hs", "List the current highscore for a table.").addOptions(option1));
//    guild.updateCommands().addCommands(commandData).queue();
//    RestAction<List<Command>> listRestAction = guild.retrieveCommands();
//    listRestAction.queue((commands -> {
//      for (Command command : commands) {
//        guild.deleteCommandById(command.getId()).complete();
//      }
//    }));
//    LOG.info("Added commands for \"" + guild.getName() + "\"");
  }

  /******************** Listener Methods ******************************************************************************/


  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      //a message from another(?) bot was posted, invalidate the message history to detect a new highscore
      long channelId = event.getMessage().getChannel().getIdLong();
      long originUserId = event.getAuthor().getIdLong();
      discordClient.invalidateMessageCache(channelId, originUserId);
      return;
    }

    Message message = event.getMessage();
    String content = message.getContentRaw();
    if (content.startsWith("/") && isValidChannel(event)) {
      if (commandResolver == null) {
        MessageChannel channel = event.getChannel();
        channel.sendMessage("No command resolver found.").queue();
        return;
      }

      BotCommand command = new BotCommand(discordClient.getDefaultGuildId(), content, commandResolver);
      BotCommandResponse response = command.execute();
      if (response != null) {
        String result = response.toDiscordMarkup();
        if (result != null && result.trim().length() > 0) {
          MessageChannel channel = event.getChannel();
          channel.sendMessage(result).queue();
        }
      }
      else {
        LOG.error("Unknown bot command '" + content + "'");
      }
    }
  }
}
